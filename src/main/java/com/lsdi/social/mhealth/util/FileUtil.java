package com.lsdi.social.mhealth.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtil {
    private static Gson gson = new Gson();
    private static LevenshteinDistance lv = new LevenshteinDistance();
    public static String readFile(File file) throws IOException {
        StringBuilder retorno = new StringBuilder();
        BufferedReader body = new BufferedReader(new FileReader(file));
        while (body.ready()) {
            //assim ele vai gerar a string já com as linhas
            retorno.append(body.readLine()).append("\r\n");
        }
        return retorno.toString();
    }

    //Para gravar em um arquivo
    public static void writeFile(String fileName, String body, boolean append) {
        File arquivo = new File(fileName);
        try {
            FileWriter fileWriter = new FileWriter(arquivo, append);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(body);
            printWriter.close();
            fileWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(FileUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String getPath(Date datePattern){
        return datePattern.getDate()+"-"+datePattern.getMonth()+".json";
    }



    public static HashMap<String, List<List<Integer>>> getCurrentPattern(String path){
        HashMap<String, List<List<Integer>>> patternAux = new HashMap<>();
        try {
            String patternJSON = FileUtil.readFile(new File(path));
            Type type = new TypeToken<HashMap<String, List<List<Integer>>>>(){}.getType();
            patternAux =   gson.fromJson(patternJSON, type);
        } catch (IOException e) {
            System.out.println("Arquivo: "+path+" não encontrado");
            return null;
        }
        return patternAux;
    }

    public static HashMap<Integer, Boolean> patternToBoolean(List<List<Integer>> patternAux){

        HashMap<Integer, Boolean> currentPattern = new HashMap<>();

        for (List<Integer> slots : patternAux){
            for (Integer slot : slots){
                currentPattern.put(slot,true);
            }
        }
        for (int i=1; i<= ParametersUtil.NUM_SLOTS; i++){
            currentPattern.putIfAbsent(i, false);
        }
        return currentPattern;
    }

    public static String patternToString(List<List<Integer>> patternAux){
        StringBuilder stringBuilder = new StringBuilder();

        HashMap<Integer, Boolean> patternBoolean = patternToBoolean(patternAux);

        for (int i=1; i<= ParametersUtil.NUM_SLOTS; i++){
            if (patternBoolean.get(i)){
                stringBuilder.append("1");
            }else{
                stringBuilder.append("0");
            }
        }

        return stringBuilder.toString();
    }

    public static double levensteinRatio(String s, String s1) {
        return 1 - ((double) lv.apply(s, s1)) / Math.max(s.length(), s1.length());
    }
   /* public static HashMap<Integer, Boolean> readCurrentPatternBoolean(Date eventDate, String context){


        List<List<Integer>> patternAux = getCurrentPattern("currentPattern.json", context);;
        HashMap<Integer, Boolean> currentPattern = new HashMap<>();

        for (List<Integer> slots : patternAux){
            for (Integer slot : slots){
                int slotAux = Integer.parseInt(String.valueOf(slot).replace(".0",""));
                currentPattern.put(slotAux,true);
            }
        }

        for (int i=1; i<= ParametersUtil.NUM_SLOTS; i++){
            currentPattern.putIfAbsent(i, false);
        }

        return currentPattern;
    }

    public static String readCurrentPatternString( String context){

        HashMap<String, List<List<Double>>> patternAux = getCurrentPattern("currentPattern.json");
        HashMap<Integer, Boolean> currentPattern = new HashMap<>();
        StringBuilder stringPattern = new StringBuilder();

        for (List<Double> slots : patternAux.get(context)){
            for (Double slot : slots){
                int slotAux = Integer.parseInt(String.valueOf(slot).replace(".0",""));
                currentPattern.put(slotAux,true);
            }
        }

        for (int i=1; i<= ParametersUtil.NUM_SLOTS; i++){
            if (currentPattern.get(i) == null){
                stringPattern.append(",0");
            }else{
                stringPattern.append(",1");
            }
        }
        return stringPattern.toString();
    }*/

}
