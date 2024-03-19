package com.c20g.labs.agency.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExecutionStatus {

    public static final Long STATUS_PENDING = 1L;
    public static final Long STATUS_IN_PROGRESS = 2L;
    public static final Long STATUS_FAILED = 3L;
    public static final Long STATUS_COMPLETE = 4L;
    
    private Long id;
    private String description;

}
