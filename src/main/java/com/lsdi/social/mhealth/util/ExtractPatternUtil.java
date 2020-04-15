package com.lsdi.social.mhealth.util;

import com.espertech.esper.client.EventBean;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExtractPatternUtil {
    private static Gson gson = new Gson();

    public static List<List<Integer>> extractPattern (EventBean[] candidateSlots, int numObs,
                                                      double MIN_SUPP, double NUM_SLOTS, double PHI, boolean printPattern) {
       // String context = candidateSlots[0].get("label").toString();
        List<List<Integer>> sociabilityPatterns = new ArrayList<>();
        List<Integer> interval = new ArrayList<>();

        //inicializa o pattern auxliar
        int[] patternAux = new int[97];
        for (int i =0; i<patternAux.length;i++){
            patternAux[i] = 0;
        }
        //Array de slots candidatos
        for(int i=0; i<candidateSlots.length; i++) {
            //print
            /*System.out.println("Event received: {Slot: " + candidateSlots[i].get("slot") + ", " +
                    "Context: " + candidateSlots[i].get("label") + ", " +
                    "Count: " + candidateSlots[i].get("countEvent") + "");
            */
            //atualiza aux_pattern
            int slot = Integer.parseInt(String.valueOf(candidateSlots[i].get("slot")).replace(".0",""));
            int count_slot = Integer.parseInt(candidateSlots[i].get("countEvent").toString());
            patternAux[slot] = count_slot;
        }

        System.out.println("------------------------------------------------------------------");
        System.out.println("Num Obs:"+numObs);
        //Limiar de intervalo frequente
        double limitInterval = MIN_SUPP * numObs;
        double limitSlot = numObs * PHI * (1 / (NUM_SLOTS));
        System.out.println("Limiar de Slot: " + limitSlot);
        System.out.println("Limiar de Rotina: " + limitInterval);

        // percorre os slots candidatos para identificar os intervalos de sociabilidade
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
        if (printPattern){
            ExtractPatternUtil.printPatterns(sociabilityPatterns,patternAux,ParametersUtil.T);
        }
        return sociabilityPatterns;
        //ExtractPatternUtil.printList(sociabilityPatterns,context);
        // ExtractPatternUtil.writePattern(sociabilityPatterns,context,null);



    }

    public static void printPatterns(List<List<Integer>> listSociability, int[] patternAux, double w){
        for (List<Integer> interval : listSociability){
            if (interval.size() == 1) {
                int slot = interval.get(0);
                System.out.println("Slots[" + slot + "]: " +
                        "Interval: " + (slot * w - (0.25)) + " -  " + slot * w + " sum: " + patternAux[slot]);
            }
            else{
                int sumInterval = getSum(interval,patternAux);
                System.out.println("Slots[" + interval.get(0) + ", " + interval.get(interval.size() - 1) + "]: "
                        + "Interval: " + (interval.get(0) * w - (0.25) + " -  " + (interval.get(interval.size() - 1) * w) + "  sum: " + sumInterval));
            }
        }
    }

    public static void writePattern(List<List<Integer>> sociabilityPattern, String context, String path){
        String patterJSON;
        HashMap<String, List<List<Integer>>> patternsDay = FileUtil.getCurrentPattern(path);
        if (patternsDay != null){
            patternsDay.put(context, sociabilityPattern);
        }else{
            System.out.println("Arquivo: "+path+" criado");
        patternsDay = new HashMap<>();
        patternsDay.put(context, sociabilityPattern);
        }
        patterJSON = gson.toJson(patternsDay);
        FileUtil.writeFile(path,patterJSON,false);
        //String date = String.valueOf(datePattern.getDate())+"-"+datePattern.getMonth();
        //FileUtil.writeFile(context+"\\"+date,patterJSON,false);

    }

    public static int getSum(List<Integer> interval, int[] patternAux){
        int sum = 0;
        for (Integer slot : interval){
            sum += patternAux[slot];
        }
        return sum;
    }

    public static void printArray(int[] array, String context){
        StringBuilder builder = new StringBuilder();
        builder.append(context);
        for (int i = 1; i < array.length; i++){
            builder.append(",S"+i+":"+array[i]);
        }
        System.out.println(builder.toString());
    }

    public static void printList(List<List<Integer>> listSociability, String context){
        StringBuilder builder = new StringBuilder();
        builder.append(context);
        for (List<Integer> listPattern : listSociability){
            for (Integer slot : listPattern){
                builder.append(","+slot);
            }
            System.out.println(builder.toString());
            builder = new StringBuilder();
            builder.append(context);
        }
    }

}
