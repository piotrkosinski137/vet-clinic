package com.vetclinic.patient;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.vetclinic.patient")
@EnableJpaRepositories(basePackages = "com.vetclinic.patient.infrastructure.persistence")
public class PatientModuleConfig {
}
