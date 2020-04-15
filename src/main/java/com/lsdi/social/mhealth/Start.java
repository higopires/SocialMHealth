
package com.lsdi.social.mhealth;

import com.lsdi.social.mhealth.builder.SociabilityPattern;
import com.lsdi.social.mhealth.enums.ContextEnum;
import com.lsdi.social.mhealth.util.StreamReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Start {

    /** Logger */
    private static Logger LOG = LoggerFactory.getLogger(Start.class);

    public static void main(String[] args) {

        StreamReceiver receiver = new StreamReceiver();
        receiver.setBroker("tcp://127.0.0.1:1883");
        receiver.setTopic("social");
        receiver.receiverStream();

        SociabilityPattern sociabilityPattern = new SociabilityPattern
                .Builder(ContextEnum.MONDAY_.toString(), 50.0)
                .setRootTopic("com/lsdi/sociability")
                .setAbnormalBehavior(true)
                .setChangeBehavior(true)
                .build();

    }

}
