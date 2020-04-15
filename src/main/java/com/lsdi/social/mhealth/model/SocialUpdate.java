package com.lsdi.social.mhealth.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;


@NoArgsConstructor
@Getter
@Setter
public class SocialUpdate {

    private String id;
    private String perception;
    private Date timestamp;
    private int slot;
    private String ctxDay;
    private String ctxWeek;
    private String ctxAll;
    private long timestamps;


    public SocialUpdate(String id, Date timestamp, String perception,  int slot, String ctxDay, String ctxWeek, String ctxAll) {
        this.id = id;
        this.timestamp = timestamp;
        this.perception = perception;
        this.slot = slot;
        this.ctxDay = ctxDay;
        this.ctxWeek = ctxWeek;
        this.ctxAll = ctxAll;
    }


    @Override
    public String toString() {
        return "{id: "+id+", Slot: "+slot+", ctxDay: "+ctxDay+", ctxWeek: "+ctxWeek+" ctxAll: "+ctxAll+"}";
    }
}
