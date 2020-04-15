package com.lsdi.social.mhealth.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
public class SocialEvent {

    private String id;
    private String perception;
    private String location;
    private Date startTime;
    private Date endTime;
    private Double duration;


    public SocialEvent(String id, String perception, String location, Date startTime, Date endTime, Double duration) {
        this.id = id;
        this.perception = perception;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }


    @Override
    public String toString() {
        return "{id: "+id+", startTime: "+startTime+", endTime: "+endTime+", duration: "+duration+"}";
    }
}
