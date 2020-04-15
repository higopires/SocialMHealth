package com.lsdi.social.mhealth.handler;

import com.lsdi.social.mhealth.components.EnrichEvent;
import com.lsdi.social.mhealth.enums.WeekEnum;
import com.lsdi.social.mhealth.model.SocialEvent;
import com.lsdi.social.mhealth.model.SocialUpdate;
import com.lsdi.social.mhealth.util.EPLQueries;
import com.lsdi.social.mhealth.util.ParametersUtil;
import com.espertech.esper.client.*;
import com.espertech.esper.client.time.CurrentTimeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


//@Component
//@Scope(value = "singleton")
public class EventHandler { //implements InitializingBean {

    private volatile static EventHandler instance;

    private EventHandler(){

    }

    public static EventHandler getInstance(){
        if( instance == null ){

            synchronized (EventHandler.class){
                if( instance == null ) {
                    instance = new EventHandler();
                    instance.initService();
                    LOG.debug("Configuring..");
                }
            }
        }
        return instance;
    }

    private int day = 50;
    private static Logger LOG = LoggerFactory.getLogger(EventHandler.class);
    private int count = 0;
    private EPServiceProvider epService; //The esper service
    private EnrichEvent enrichEvent;
    public String teste = "Teste deu certo";

    private EPAdministrator epAdm;

    /**
     * Configure the Esper Environment.
     */
    private void initService() {

        LOG.debug("Initializing Servcie ..");

        /** Relational DB Configuration */
        ConfigurationDBRef dbConfig = new ConfigurationDBRef();

        dbConfig.setDriverManagerConnection("org.postgresql.Driver",
                "jdbc:postgresql://localhost:5432/EsperSocial",
                "root",
                "");
        dbConfig.setMetadataOrigin(ConfigurationDBRef.MetadataOriginEnum.SAMPLE);

        /** Setting the Esper Configuration and EPService */
        Configuration config = new Configuration();
        config.addImport("com.lsdi.social.mhealth.util.*");
        config.addDatabaseReference("Postgresql", dbConfig);
        config.addEventTypeAutoName("com.lsdi.social.mhealth.model");

        config.getEngineDefaults().getThreading().setInternalTimerEnabled(false);

        epService = EPServiceProviderManager.getDefaultProvider(config);
        epAdm = epService.getEPAdministrator();
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(0));


        /** Start of EPLStatement and Listener registration */

        /**
         * Cria o contexto de categoria
         */
        EPStatement createCategoryCtxStm = setQuery(EPLQueries.getCreateCategoryContext());

        /**
         * Emite evento derivado contextEvent
         */
        EPStatement createContextEventStm = setQuery(EPLQueries.getCreateContextEvent());

        /**
         * Cria e inseri na tabela de contagem de observações por contexto
         */
        EPStatement createCountTable3WeekStm = setQuery(EPLQueries.getGetCreateCountTable(WeekEnum.THREE.toString()));
        EPStatement createCountTable1WeekStm = setQuery(EPLQueries.getGetCreateCountTable(WeekEnum.ONE.toString()));

        EPStatement insertCountTable3WeekStm = setQuery(EPLQueries.getInsertCountTable(WeekEnum.THREE.toString()));
        EPStatement insertCountTable1WeekStm = setQuery(EPLQueries.getInsertCountTable(WeekEnum.ONE.toString()));

        /**
         * Cria e inseri na tabela de contagens de observações por slot
         */
        EPStatement createSociabilityTable3WeekStm = setQuery(EPLQueries.getCreateSociabilityTable(WeekEnum.THREE.toString()));
        EPStatement createSociabilityTable1WeekStm = setQuery(EPLQueries.getCreateSociabilityTable(WeekEnum.ONE.toString()));

        EPStatement insertSociabilityTable3WeekStm = setQuery(EPLQueries.getInsertTableSociability(WeekEnum.THREE.toString()));
        EPStatement insertSociabilityTable1WeekStm = setQuery(EPLQueries.getInsertTableSociability(WeekEnum.ONE.toString()));

        /**
         * Cria as constantes n° de slot (W * w) e phi
         */
        EPStatement createNumSlotStm = setQuery(EPLQueries.getCreateNslot(ParametersUtil.NUM_SLOTS));

        EPStatement createPhiStm = setQuery(EPLQueries.getCreatePhi(ParametersUtil.PHI));

        /** End of EPLStatement and Listener registration */

        enrichEvent = new EnrichEvent();

    }

    public EPStatement setQuery(String EPLQuery){
        return epAdm.createEPL(EPLQuery);
    }

    /**
     * Handle the incoming SocialUpdate.
     * @param socialEvent the event to handle
     */
    public void handle(SocialEvent socialEvent) {
        count++;
        SocialUpdate socialUpdate = enrichEvent.enrichSocialEvent(socialEvent);
        int slot = Integer.parseInt(String.valueOf(socialUpdate.getSlot()).replace(".0",""));
        socialUpdate.setSlot(slot);
        socialUpdate.setTimestamps(socialUpdate.getTimestamp().getTime());
        //long diff = socialUpdate.getTimestamp().getTime() - actualDate;

        if (count != 1) {
            // CurrentTimeSpanEvent currentTimeSpanEvent = new CurrentTimeSpanEvent(socialUpdate.getTimestamp().getTime());
            //actualTime = actualTime + 3600000;//+ diff;
            epService.getEPRuntime().sendEvent(new CurrentTimeEvent(socialUpdate.getTimestamps()));
            epService.getEPRuntime().sendEvent(socialUpdate);
            //actualDate = socialUpdate.getTimestamp().getTime();
            // System.out.println("Data atual: " + epService.getEPRuntime().getCurrentTime());
        }

    }
    /**
     * Recupera a quantidade de eventos processados em um deteminado contexto
     * @param label
     * @return
     */
    public int getNumObs(String label, String countTable){
        int count = 0;
        EPOnDemandQueryResult result =
                epService.getEPRuntime().executeQuery(EPLQueries.getSelectCountContext(label,countTable));
        for (EventBean row : result.getArray()) {
            count = Integer.parseInt(row.get("myCount").toString());
        }
        return count;
    }

    /**
     * Seleciona todos os slots candidatos
     * @param
     * @return
     */
    public EventBean[] getCandidateSlots(int numObs, String context, String countTable){
        EPOnDemandQueryResult result =
                epService.getEPRuntime().executeQuery(EPLQueries.getCandidateSlots(context, numObs, countTable));
        return result.getArray();
    }

    public Date getActualDate(){
        Date dt = new Date (epService.getEPRuntime().getCurrentTime());
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        df.setTimeZone (TimeZone.getTimeZone ("GMT"));
        return dt;
    }

    /*@Override
    public void afterPropertiesSet() {

        LOG.debug("Configuring..");
        initService();
    }*/
}
