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
public class NotifyAbnormalBehavior {
    private Date date;
    private String eventType;
    private String context;
    private Double similarity;
    private Double defuzzifiedValue;
    private Double isAbnormal;
    private Double isNormal;
    private Double isWarning;
    private String message;


}
