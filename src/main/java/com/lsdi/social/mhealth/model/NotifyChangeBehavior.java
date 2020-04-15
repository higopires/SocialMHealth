package com.lsdi.social.mhealth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class NotifyChangeBehavior {
    private Date date;
    private String eventType;
    private String context;
    private Double similarity;
    private Double defuzzifiedValue;
    private Double isChange;
    private Double isNoChange;
    private Double isModerateChange;
    private String message;
}
