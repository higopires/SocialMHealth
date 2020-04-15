package com.lsdi.social.mhealth.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class NotifyNewPattern {

    private Date date;
    private String eventType;
    private String context;
    private List<List<Integer>> socialPattern;

}
