package com.vetclinic.compliance;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.vetclinic.compliance")
@EnableJpaRepositories(basePackages = "com.vetclinic.compliance.infrastructure.persistence")
public class ComplianceModuleConfig {}
