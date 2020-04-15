
package com.lsdi.social.mhealth.util;

import com.lsdi.social.mhealth.enums.WeekEnum;

/**
 * A class of static methods that return EPLQueries
 * @author ivanrmoura
 */
public class EPLQueries {

    /**
     * fatia o fluxo a partir dos atributos de contexto
     */
    public static String getCreateCategoryContext(){
        return "CREATE CONTEXT CategoryContext " +
                "GROUP ctxAll = 'All' as ALL_, " +
                "GROUP ctxWeek = 'Week' as WEEK_, " +
                "GROUP ctxWeek = 'Weekend' as WEEKEND_, " +
                "GROUP ctxDay = 'Sunday' as SUNDAY_, " +
                "GROUP ctxDay = 'Monday' as MONDAY_, " +
                "GROUP ctxDay = 'Tuesday' as TUESDAY_, " +
                "GROUP ctxDay = 'Wednesday' as WEDNESDAY_, " +
                "GROUP ctxDay = 'Thursday' as THURSDAY_, " +
                "GROUP ctxDay= 'Friday' as FRIDAY_, " +
                "GROUP ctxDay= 'Saturday' as SATURDAY_ " +
                "FROM SocialUpdate";
    }
    
    /**
     * Gera um evento derivado (ContextEvent)
     */
    public static String getCreateContextEvent(){
        return "CONTEXT CategoryContext " +
                "INSERT INTO ContextEvent " +
                "SELECT slot,  timestamps, context.label " +
                "FROM SocialUpdate";
    }

    /**
     * cria uma NamedTable para contar os eventos de cada contexto
     */
   public static String getGetCreateCountTable(String countTable){
       if (countTable.equals(WeekEnum.THREE.toString())){
           return "create table CountTableThree " +
                   "( label string primary key, myCount count(*) )";

       }else if (countTable.equals(WeekEnum.ONE.toString())){
           return "create table CountTableOne " +
                   "( label string primary key, myCount count(*) )";
       }
       return "";
   }

    /*public static String getInsertCountTable = "INTO TABLE CountTable " +
            "SELECT count(*) AS myCount " +
            "FROM ContextEvent.win:keepall() " +
            "GROUP BY label";
    */

    /**
     * Atualiza a contagem de n° de observações contextuais
     */
    public static String getInsertCountTable(String countTable){
        if (countTable.equals(WeekEnum.THREE.toString())){
            return "INTO TABLE CountTableThree " +
                    "SELECT count(*) AS myCount " +
                    "FROM ContextEvent.win:ext_timed(timestamps, 21 day) " +
                    "GROUP BY label";

        }else if (countTable.equals(WeekEnum.ONE.toString())){
            return "INTO TABLE CountTableOne " +
                    "SELECT count(*) AS myCount " +
                    "FROM ContextEvent.win:ext_timed(timestamps, 7 day) " +
                    "GROUP BY label";
        }
        return "";
    }

    /**
     * seleciona a contagem de observações de um contexto
     */
    public static String getSelectCountContext(String label, String countTable){
        if (countTable.equals(WeekEnum.THREE.toString())){
        return "select label, myCount from CountTableThree as countTable " +
                "where countTable.label = '"+label+"'";
        }else if (countTable.equals(WeekEnum.ONE.toString())){
            return "select label, myCount from CountTableOne as countTable " +
                    "where countTable.label = '"+label+"'";
        }
        return "";
    }

    /**
     * cria a tabela sociability (slot, label, countSlot)
     */
    public static String getCreateSociabilityTable(String countTable){
        if (countTable.equals(WeekEnum.THREE.toString())){
            return "create table SociabilityTableThree " +
                    "( slot int primary key, " +
                    "label string primary key, " +
                    "countEvent count(*) )";

        }else if (countTable.equals(WeekEnum.ONE.toString())){
            return "create table SociabilityTableOne " +
                    "( slot int primary key, " +
                    "label string primary key, " +
                    "countEvent count(*) )";
        }
        return "";
    }

    /**
     * Atualiza a contagem de observações por slot
     */
    public static String getInsertTableSociability(String countTable){
        if (countTable.equals(WeekEnum.THREE.toString())){
            return "into table SociabilityTableThree " +
                    "select count(*) as countEvent " +
                    "from ContextEvent.win:ext_timed(timestamps,21 day) " +
                    "group by slot, label";

        }else if (countTable.equals(WeekEnum.ONE.toString())){
            return "into table SociabilityTableOne " +
                    "select count(*) as countEvent " +
                    "from ContextEvent.win:ext_timed(timestamps, 7 day) " +
                    "group by slot, label";
        }
        return "";
    }


    /**
     * Cria as constantes n° de slots e phi
     */
    public static String getCreateNslot(double numSlots){
       return "create variable double var_nSlot = "+numSlots+"";
    }

    public static String getCreatePhi(double phi){
        return "create variable double var_phi = "+phi+"";
    }

    /**
     * Seleciona slots candidatos (numObs * (1/(numSlots * phi))
     */
    public static String getSelectCandidateSlots(String label, String countTable){
        if (countTable.equals(WeekEnum.THREE.toString())){
            return "select * from SociabilityTableThree as st " +
                    "where st.label = '"+label+"' and " +
                    "countEvent >= ((select myCount from CountTableThree as countTable where countTable.label = st.label) " +
                    " * "+ParametersUtil.PHI+" * (1/(var_nSlot))) output snapshot every 14 day " +
                    "order by slot";

        }else if (countTable.equals(WeekEnum.ONE.toString())){
            return "select * from SociabilityTableOne as st " +
                    "where st.label = '"+label+"' and " +
                    "countEvent >= ((select myCount from CountTableOne as countTable where countTable.label = st.label) " +
                    " * "+ParametersUtil.PHI+" * (1/(var_nSlot))) output snapshot every 7 day " +
                    "order by slot";
        }
        return "";
    }

    public static String getCandidateSlots(String label, int numObs, String countTable){
        if (countTable.equals(WeekEnum.THREE.toString())){
            return "select * from SociabilityTableThree as st " +
                    "where st.label = '"+label+"' and " +
                    "countEvent >= "+numObs+" * "+ParametersUtil.PHI+" * (1/(var_nSlot))" +
                    "order by slot";

        }else if (countTable.equals(WeekEnum.ONE.toString())){
            return "select * from SociabilityTableOne as st " +
                    "where st.label = '"+label+"' and " +
                    "countEvent >= "+numObs+" * "+ParametersUtil.PHI+" * (1/(var_nSlot))" +
                    "order by slot";
        }
        return "";
    }

    private static String getNumSlotByContext(String label){
        return "(select myCount from CountTable as countTable " +
                "where countTable.label = sbTable."+label+"";
    }



    /**
     * seleciona a contagem de observações de um contexto a cada 20 segundos
     public static String getSelectCountTableEvery(String contextName) {
     return "SELECT * FROM CountTable AS countTable " +
     "where countTable.label = '"+contextName+"' " +
     "OUTPUT SNAPSHOT EVERY 7 second";
     }*/

    /*public static String getInsertTableSociability = "into table SociabilityTable " +
            "select count(*) as countEvent " +
            "from ContextEvent.win:keepall() " +
            "group by slot, label";*/


}
