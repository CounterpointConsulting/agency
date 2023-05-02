package com.c20g.labs.agency.skill;

import java.lang.annotation.*;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@ComponentScan(
    basePackages = {
      "com.c20g.labs.agency"
    })
public @interface SkillService {
    
}
