package com.c20g.labs.agency.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ExecutionStep {
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @OneToOne
    private PlanExecution execution;

    @ManyToOne
    private PlanStep planStep;

    @ManyToOne
    private ExecutionStepStatus status;
    
    @Lob
    private String requestMessage;
    
    @Lob
    private String responseMessage;
    
    @Column(nullable=false)
    private Instant startTime;
    
    @Column(nullable=true)
    private Instant endTime;

}
