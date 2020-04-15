package com.lsdi.social.mhealth.builder;

import com.lsdi.social.mhealth.enums.WeekEnum;
import com.lsdi.social.mhealth.handler.EventHandler;
import com.lsdi.social.mhealth.subscriber.AbnormalBehaviorListener;
import com.lsdi.social.mhealth.subscriber.ChangeBehaviorListener;
import com.lsdi.social.mhealth.util.EPLQueries;
import com.espertech.esper.client.EPStatement;


public class SociabilityPattern {

    private String context;
    private Boolean abnormalBehavior;
    private Boolean changeBehavior;
    private String rootTopic;
    private Double sensitivityOfChange;

    private SociabilityPattern(String context, Boolean abnormalBehavior, Boolean changeBehavior, String rootTopic, Double sensitivityOfChange) {
        this.context = context;
        this.abnormalBehavior = abnormalBehavior;
        this.changeBehavior = changeBehavior;
        this.rootTopic = rootTopic;
        this.sensitivityOfChange = sensitivityOfChange;
    }

    public static class Builder{

        private EventHandler eventHandler = EventHandler.getInstance();

        private String context;
        private Boolean abnormalBehavior;
        private Boolean changeBehavior;
        private String rootTopic;
        private Double sensitivityOfChange;
        private AbnormalBehaviorListener abnormalBehaviorListener;


        public Builder(String context, Double sensitivityOfChange){
            this.context = context;
            this.sensitivityOfChange = sensitivityOfChange;
        }

        public Builder setRootTopic(String rootTopic) {
            this.rootTopic = rootTopic;
            System.out.println("start topic: "+rootTopic);
            return this;
        }

        public Builder setAbnormalBehavior(boolean abnormalBehavior) {
            this.abnormalBehavior = abnormalBehavior;
            if (abnormalBehavior){
                AbnormalBehaviorListener abnormalBehaviorListener = new AbnormalBehaviorListener(context, sensitivityOfChange, rootTopic);
                EPStatement abnormalBehaviorSmt =
                        eventHandler.setQuery(EPLQueries.getSelectCandidateSlots(context, WeekEnum.ONE.toString()));
                abnormalBehaviorSmt.addListener(abnormalBehaviorListener);
            }
            return this;
        }

        public Builder setChangeBehavior(boolean changeBehavior) {
            this.changeBehavior = changeBehavior;

            ChangeBehaviorListener changeBehaviorListener = new ChangeBehaviorListener(context, sensitivityOfChange, rootTopic, changeBehavior);
            EPStatement changeBehaviorSmt =
                    eventHandler.setQuery(EPLQueries.getSelectCandidateSlots(context, WeekEnum.THREE.toString()));
            changeBehaviorSmt.addListener(changeBehaviorListener);
            return this;
        }

        public SociabilityPattern build(){
            return new SociabilityPattern(context,
                    abnormalBehavior,
                    changeBehavior, rootTopic, sensitivityOfChange);
        }
    }

}
