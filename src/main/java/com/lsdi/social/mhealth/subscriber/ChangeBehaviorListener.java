package com.lsdi.social.mhealth.subscriber;

import com.lsdi.social.mhealth.enums.WeekEnum;
import com.lsdi.social.mhealth.handler.EventHandler;
import com.lsdi.social.mhealth.model.NotifyChangeBehavior;
import com.lsdi.social.mhealth.model.NotifyNewPattern;
import com.lsdi.social.mhealth.util.StreamReceiver;
import com.lsdi.social.mhealth.util.ExtractPatternUtil;
import com.lsdi.social.mhealth.util.FileUtil;
import com.lsdi.social.mhealth.util.ParametersUtil;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.google.gson.Gson;
import net.sourceforge.jFuzzyLogic.FIS;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChangeBehaviorListener implements UpdateListener {

    private String context;
    private int countPattern =0;
    private List<List<Integer>> sociabilityPatterns = new ArrayList<>();
    private EventHandler eventHandler = EventHandler.getInstance();
    private Double sensitivityOfChange;
    private String topic;
    private Boolean isChangeBehavior;

    public ChangeBehaviorListener(String context, Double sensitivityOfChange, String topic, boolean changeBehavior) {
        this.context = context;
        this.sensitivityOfChange = sensitivityOfChange;
        this.topic = topic;
        this.isChangeBehavior = changeBehavior;
    }

    @Override
    public void update(EventBean[] newData, EventBean[] oldData) {
        int numObs = eventHandler.getNumObs(context, WeekEnum.THREE.toString());
        String context = newData[0].get("label").toString();
        Gson gson = new Gson();

        //Extrai o padrão de sociabilidade (identifica os intervalos de sociabilidade)
        try {
            sociabilityPatterns = ExtractPatternUtil.extractPattern(newData, numObs,ParametersUtil.THETA,
                    ParametersUtil.NUM_SLOTS, ParametersUtil.T,false);
        }catch (Exception e){
            e.printStackTrace();
        }

        countPattern += 1;
        if (countPattern >= 2 && isChangeBehavior) {
            detectDrift(sociabilityPatterns);
        }

        //grava o primeiro padrão social
        if (countPattern <2){
            ExtractPatternUtil.writePattern(sociabilityPatterns, context, "patternsHistory\\currentPattern.json");
            //Publica o padrão identificado
            Date actualDate = eventHandler.getActualDate();
            NotifyNewPattern notifyNewPattern = new NotifyNewPattern(
                    actualDate,
                    NotifyNewPattern.class.getSimpleName(),
                    context,
                    sociabilityPatterns);

            try {
                StreamReceiver.client.publish(topic+"/"+context+"/newPattern", new MqttMessage(gson.toJson(notifyNewPattern).getBytes()));
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }


    }

    private void detectDrift(List<List<Integer>> sociabilityPatterns){
        // Load from 'FCL' file
        String fileName = "fcls\\FIS.fcl";
        double similarity = getSimilarity(sociabilityPatterns);
        Gson gson = new Gson();
        FIS fis = FIS.load(fileName,true);

        if( fis == null ) {
            System.err.println("Can't load file: '" + fileName + "'");
            return;
        }

        // Set inputs
        fis.setVariable("sensibility", sensitivityOfChange);
        fis.setVariable("similarity", similarity*100);

        // Evaluate
        fis.evaluate();

        try {
            //System.out.println(" ----------------- Detect Drift ----------------");
            double defuzzyFiedValue = fis.getFunctionBlock("drift").getVariable("drift").getValue();
            double changeValue = fis.getFunctionBlock("drift").getVariable("drift").getMembership("change");
            double noChangeValue = fis.getFunctionBlock("drift").getVariable("drift").getMembership("no_change");
            double moderateValue = fis.getFunctionBlock("drift").getVariable("drift").getMembership("moderate_change");

            String message = getMenssage(changeValue, noChangeValue, moderateValue);
            NotifyChangeBehavior changeBehavior = new NotifyChangeBehavior(
                    eventHandler.getActualDate(),
                    NotifyChangeBehavior.class.getSimpleName(),
                    context,
                    (similarity*100),
                    defuzzyFiedValue,
                    changeValue,
                    noChangeValue,
                    moderateValue,
                    message
            );

            //verifica se houve mudança e extrai e publica um novo padrão
            if (isChange(changeValue, moderateValue, noChangeValue)) {

                ExtractPatternUtil.writePattern(sociabilityPatterns, context, "patternsHistory\\currentPattern.json");

                //Publica o padrão identificado
                Date actualDate = eventHandler.getActualDate();
                NotifyNewPattern notifyNewPattern = new NotifyNewPattern(
                        actualDate,
                        NotifyNewPattern.class.getSimpleName(),
                        context,
                        sociabilityPatterns);
                try {
                    StreamReceiver.client.publish(topic+"/"+context+"/newPattern", new MqttMessage(gson.toJson(notifyNewPattern).getBytes()));
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }else{
                //changeBehavior.setContext("Não teve mudanaça");
            }

            String jsonChangeNotify = gson.toJson(changeBehavior);
            StreamReceiver.client.publish(topic+"/"+context+"/ChangeBehavior",new MqttMessage(jsonChangeNotify.getBytes()));

           // System.out.println(" -------------------------------------");
        }catch (Exception e){
            e.printStackTrace();
        }
        // Gpr.debug("drift[change]: " + fis.getFunctionBlock("drift").getVariable("drift").getMembership("change"));
        // System.out.println(fis.getFunctionBlock("drift").getVariables());
        // System.out.println( fis.getFunctionBlock("drift").getVariable("drift").getLinguisticTerms());
    }

    //verifica se houve mudança
    private boolean isChange(double change, double moderate, double noChange) {
        return (noChange < change) || (noChange < moderate);
    }

    //calcula a similaridade entre o padrão encontrado e o antigo
    private double getSimilarity(List<List<Integer>> actualPattern){
        //String oldPath = "week"+(countPattern-1);
        List<List<Integer>> oldPattern = FileUtil.getCurrentPattern("patternsHistory\\currentPattern.json").get(context);
        HashMap<Integer, Boolean> actualPatternBool = FileUtil.patternToBoolean(actualPattern);
        HashMap<Integer, Boolean> oldPatternBool = FileUtil.patternToBoolean(oldPattern);
        double countIntersection = 0;
        double countUnion = 0;
        double similarity;

        for (Integer key : actualPatternBool.keySet()){
            if (actualPatternBool.get(key) && oldPatternBool.get(key)){
                countIntersection++;
                countUnion++;
            }else if ((!actualPatternBool.get(key) && oldPatternBool.get(key)) ||
                    (actualPatternBool.get(key) && !oldPatternBool.get(key))){
                countUnion++;
            }
        }
        similarity = countIntersection/countUnion;
        return similarity;
    }

    private String getMenssage(Double change, Double noChange, Double moderateChange){
        if ((noChange >= change) & (noChange >= moderateChange) ){
            return "No changes in social routine";
        }
        return change >= moderateChange ? "Change in social routine" :
                "Moderate change in social routine";
    }

}