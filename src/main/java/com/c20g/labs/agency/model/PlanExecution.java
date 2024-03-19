package com.c20g.labs.agency.model;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanExecution {
    
    private Long id;
    private Plan plan;
    private ExecutionStatus status;
    private ExecutionStep currentStep;
    private Instant startTime;
    private Instant endTime;

}
