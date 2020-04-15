package com.lsdi.social.mhealth.components;

import com.lsdi.social.mhealth.enums.WeekEnum;
import com.lsdi.social.mhealth.handler.EventHandler;
import com.lsdi.social.mhealth.subscriber.AbnormalBehaviorListener;
import com.lsdi.social.mhealth.util.ExtractPatternUtil;
import com.lsdi.social.mhealth.util.ParametersUtil;
import com.espertech.esper.client.EventBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//@Component
public class ExtractPattern {

    private static Logger LOG = LoggerFactory.getLogger(AbnormalBehaviorListener.class);
    private int day = 50;
    private String context;
    private int count =0;
    //@Autowired

    private EventHandler eventHandler = EventHandler.getInstance();

    private int typeContext = 0;

    public ExtractPattern() {
    }

    /**
     * Extrai a rotina de sociabilidade
     */
    public void detectPatterns(Date datePattern){
        int numObs = eventHandler.getNumObs(context, WeekEnum.THREE.toString());

        int[] patternAux = new int[97];
        for (int i =0; i<patternAux.length;i++){
            patternAux[i] = 0;
        }

        //Array de slots candidatos
        EventBean[] candidateSlots = eventHandler.getCandidateSlots(numObs, context, WeekEnum.THREE.toString());
        String context = candidateSlots[0].get("label").toString();
        for(int i=0; i<candidateSlots.length; i++) {
            //print
            System.out.println("Event received: {Slot: " + candidateSlots[i].get("slot") + ", " +
                    "Context: " + candidateSlots[i].get("label") + ", " +
                    "Count: " + candidateSlots[i].get("countEvent") + "");

            //atualiza aux_pattern
            int slot = Integer.parseInt(candidateSlots[i].get("slot").toString());
            int count_slot = Integer.parseInt(candidateSlots[i].get("countEvent").toString());
            patternAux[slot] = count_slot;
        }
        //printArray(patternAux,context);
        System.out.println("------------------------------------------------------------------");
        System.out.println("Pattern date "+datePattern+ "Obs:"+numObs);
        //Limiar de intervalo frequente
        double limitInterval = ParametersUtil.THETA * numObs;
        double limitSlot = numObs * ParametersUtil.PHI * (1 / (ParametersUtil.NUM_SLOTS));
        System.out.println("Limiar de Slot: " + limitSlot);
        System.out.println("Limiar de Rotina: " + limitInterval);
        List<List<Integer>> sociabilityPatterns = new ArrayList<>();
        List<Integer> interval = new ArrayList<>();
        for (int i = 1; i < patternAux.length; i++ ){
            if (patternAux[i] != 0){
                interval.add(i);
            }else{
                if (interval.size()>0){
                    // soma dos slots > limiar
                    int sumInterval = ExtractPatternUtil.getSum(interval,patternAux);
                    if (sumInterval >= limitInterval) {
                        sociabilityPatterns.add(interval);
                    }
                }
                interval = new ArrayList<>();
            }
        }
        //ExtractPatternUtil.printList(sociabilityPatterns,context);
       // ExtractPatternUtil.writePattern(sociabilityPatterns,context,datePattern);
        //ExtractPatternUtil.printPatterns(sociabilityPatterns, patternAux, ParametersUtil.PARAM_SLOT);
    }

    public void setContext (String context){
        this.context = context;
    }

    public void setTypeContext(int typeContext){
        this.typeContext = typeContext;
    }


}