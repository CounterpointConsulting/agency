package com.c20g.labs.agency.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Skill {
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    
    @Column(length=200, nullable=false)
    private String shortName;

    @Lob
    private String description;
    
    @Column(nullable=false)
    private Integer port;

    @Column(length=10, nullable=false)
    private String verb;

    @Column(length=200, nullable=false)
    private String path;

    @Lob
    private String sampleRequest;
    
    @Lob
    private String sampleResponse;

}
