package com.vetclinic.visit;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.vetclinic.visit")
@EnableJpaRepositories(basePackages = "com.vetclinic.visit.infrastructure.persistence")
public class VisitModuleConfig {}
