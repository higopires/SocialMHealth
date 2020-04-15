package com.lsdi.social.mhealth.subscriber;

import com.lsdi.social.mhealth.enums.WeekEnum;
import com.lsdi.social.mhealth.handler.EventHandler;
import com.lsdi.social.mhealth.model.NotifyAbnormalBehavior;
import com.lsdi.social.mhealth.util.StreamReceiver;
import com.lsdi.social.mhealth.util.ExtractPatternUtil;
import com.lsdi.social.mhealth.util.FileUtil;
import com.lsdi.social.mhealth.util.ParametersUtil;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.google.gson.Gson;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Rule;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;


public class AbnormalBehaviorListener implements UpdateListener {

    /** Logger */
    private static Logger LOG = LoggerFactory.getLogger(AbnormalBehaviorListener.class);

    /** The EventHandler - wraps the Esper engine and processes the Events  */
    private String context;
    private String actualDatePattern ="";
    private int countPattern =0;
    private EventHandler eventHandler = EventHandler.getInstance();
    private Double sensitivityOfChange;
    private String topic;

    public AbnormalBehaviorListener(String context, Double sensitivityOfChange, String topic) {
        this.context = context;
        this.sensitivityOfChange = sensitivityOfChange;
        this.topic = topic+"/"+context+"/AbnormalBehavior";

    }

    /**
     * The update method that receives the new and old events
     * @param newData An event bean with the new events
     * @param oldData An event bean with the old events
     */
    @Override
    public void update(EventBean[] newData, EventBean[] oldData) {
        int numObs = eventHandler.getNumObs(context, WeekEnum.THREE.toString());
        String context = newData[0].get("label").toString();
        Gson gson = new Gson();
        List<List<Integer>> sociabilityPatterns = null;
        //Extrai o padrão de sociabilidade (identifica os intervalos de sociabilidade)
        try {
            sociabilityPatterns = ExtractPatternUtil.extractPattern(newData, numObs,ParametersUtil.THETA,
                    ParametersUtil.NUM_SLOTS, ParametersUtil.T,false);
        }catch (Exception e){
            e.printStackTrace();
        }
        // Salva o padrão de sociabilidade reconhecido
        List<List<Integer>> currentPattern = FileUtil.getCurrentPattern("patternsHistory\\currentPattern.json").get(context);

        if (currentPattern != null) {
            detectDrift(sociabilityPatterns, currentPattern);
        }


    }

    private void detectDrift(List<List<Integer>> sociabilityPattern, List<List<Integer>> currentPattern){
        // Load from 'FCL' file
        String fileName = "fcls\\FIS.fcl";
        double similarity = getSimilarity(sociabilityPattern, currentPattern);
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

        // Print ruleSet
        //System.out.println("Output value:" + fis.getVariable("drift").getValue());
        //System.out.println(fis.getFunctionBlock("drift").getVariables());
        for( Rule r : fis.getFunctionBlock("drift").getFuzzyRuleBlock("No1").getRules() ) {
            //  System.out.println(r);
        }

        try {


            //System.out.println(" ----------------- Detect Drift ----------------");
            double defuzzyFiedValue = fis.getFunctionBlock("drift").getVariable("drift").getValue();
            double changeValue = fis.getFunctionBlock("drift").getVariable("drift").getMembership("change");
            double noChangeValue = fis.getFunctionBlock("drift").getVariable("drift").getMembership("no_change");
            double moderateValue = fis.getFunctionBlock("drift").getVariable("drift").getMembership("moderate_change");
            String message = getMenssage(changeValue, noChangeValue, moderateValue);

            NotifyAbnormalBehavior changeBehavior = new NotifyAbnormalBehavior(
                    eventHandler.getActualDate(),
                    NotifyAbnormalBehavior.class.getSimpleName(),
                    context,
                    (similarity*100),
                    defuzzyFiedValue,
                    changeValue,
                    noChangeValue,
                    moderateValue,
                    message
            );
            String jsonChangeNotify = gson.toJson(changeBehavior);

            StreamReceiver.client.publish(topic,new MqttMessage(jsonChangeNotify.getBytes()));

            //System.out.println(fis.getFunctionBlock("drift").getVariables());
            //System.out.println("Abnormal Behavior");
            //System.out.println(jsonChangeNotify);
            //System.out.println(" -------------------------------------");
        }catch (Exception e){
            e.printStackTrace();
        }
        // Gpr.debug("drift[change]: " + fis.getFunctionBlock("drift").getVariable("drift").getMembership("change"));
        // System.out.println(fis.getFunctionBlock("drift").getVariables());
        // System.out.println( fis.getFunctionBlock("drift").getVariable("drift").getLinguisticTerms());
    }

    private double getSimilarity(List<List<Integer>> sociabilityPattern, List<List<Integer>> currentPattern){

        HashMap<Integer, Boolean> actualPatternBool = FileUtil.patternToBoolean(sociabilityPattern);
        HashMap<Integer, Boolean> oldPatternBool = FileUtil.patternToBoolean(currentPattern);
        double countIntersection = 0;
        double countUnion = 0;
        double countOld=0;
        double similarity;
        for (Integer key : actualPatternBool.keySet()){
            if (oldPatternBool.get(key)){
                countOld++;
            }
            if (actualPatternBool.get(key) && oldPatternBool.get(key)){
                countIntersection++;
                countUnion++;
            }else if ((!actualPatternBool.get(key) && oldPatternBool.get(key)) ||
                    (actualPatternBool.get(key) && !oldPatternBool.get(key))){
                countUnion++;
            }
        }

        similarity = countIntersection/countOld;
        return similarity;
    }

    private String getMenssage(Double change, Double noChange, Double moderateChange){
        if ((noChange >= change) & (noChange >= moderateChange) ){
            return "No abnormal social behavior";
        }
        return change >= moderateChange ? "Abnormal Behavior Detected" :
                "Abnormal Behavior Warning";
    }

}