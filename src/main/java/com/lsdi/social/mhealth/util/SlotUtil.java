package com.lsdi.social.mhealth.util;

import com.lsdi.social.mhealth.model.SocialEvent;

public class SlotUtil {

    // extrai o número de slots referente aos minutos
    public static int getSlotMinute(double w, int time) {

        int min = (int)(60 / (1 / w));
        if (time<min) {
            return 1;
        } else if (time % min == 0){
            return time/min;
        }else{
            return (time / min) + 1;
        }
    }


    /**
     * Extract time slot.
     * @param event the event to handle
     */
    public static int extractSlotTime(SocialEvent event, double w){
        int hourEvent = event.getStartTime().getHours();
        int minuteEvent = event.getStartTime().getMinutes();
        int slot = (int)(hourEvent/w) + getSlotMinute(w, minuteEvent);
        return slot;
    }

}
