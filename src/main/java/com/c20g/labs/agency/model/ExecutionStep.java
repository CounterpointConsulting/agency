package com.c20g.labs.agency.model;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExecutionStep {
    
    private Long id;
    private PlanExecution execution;
    private PlanStep planStep;
    private ExecutionStepStatus status;
    private String requestMessage;
    private String responseMessage;
    private Instant startTime;
    private Instant endTime;

}
