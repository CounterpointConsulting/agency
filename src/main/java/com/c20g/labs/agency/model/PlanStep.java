package com.c20g.labs.agency.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class PlanStep {
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Column(nullable=false)
    private int stepNumber;

    @ManyToOne
    private Skill skill;

    @ManyToOne
    private Plan plan;

    @Lob
    private String explanation;
    
    @Column(nullable=false)
    private Instant startTime;

    @Column(nullable=true)
    private Instant endTime;

}
