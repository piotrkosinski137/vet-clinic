package com.vetclinic.billing;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.vetclinic.billing")
@EnableJpaRepositories(basePackages = "com.vetclinic.billing.infrastructure.persistence")
public class BillingModuleConfig {}
