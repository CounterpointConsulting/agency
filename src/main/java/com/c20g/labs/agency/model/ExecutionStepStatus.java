package com.c20g.labs.agency.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ExecutionStepStatus {
    
    public static final Long STATUS_PENDING = 1L;
    public static final Long STATUS_IN_PROGRESS = 2L;
    public static final Long STATUS_FAILED = 3L;
    public static final Long STATUS_COMPLETE = 4L;
    
    @Id
    private Long id;
    
    @Column(length = 100)
    private String description;

}
