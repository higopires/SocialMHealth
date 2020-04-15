package com.lsdi.social.mhealth.components;

import com.lsdi.social.mhealth.model.SocialEvent;
import com.lsdi.social.mhealth.model.SocialUpdate;
import com.lsdi.social.mhealth.util.ParametersUtil;
import com.lsdi.social.mhealth.util.SlotUtil;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

//@Component
public class EnrichEvent {

    /** The EventHandler - wraps the Esper engine and processes the Events  */
    //@Autowired

    private SocialUpdate socialUpdate;

    /**
     * Extract time slot.
     * @param event the event to handle
     */
    public SocialUpdate extractContextData(SocialUpdate event, Date dateTime){
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTime);
        DateFormatSymbols dfs = new DateFormatSymbols(Locale.US);
        String day = dfs.getWeekdays()[cal.get(Calendar.DAY_OF_WEEK)];
        String week = (dateTime.getDay() >= 6) | (dateTime.getDay()==0)? "Weekend" : "Week";
        return new SocialUpdate("u52", dateTime,"Interaction",event.getSlot(), day, week,"All");
    }

    public SocialUpdate enrichSocialEvent(SocialEvent event) {
        int slot = SlotUtil.extractSlotTime(event, ParametersUtil.T);
        socialUpdate = new SocialUpdate();
        socialUpdate.setSlot(slot);
        socialUpdate = extractContextData(socialUpdate, event.getStartTime());
        return socialUpdate;
    }

}
