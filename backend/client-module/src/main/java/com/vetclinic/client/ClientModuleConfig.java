package com.vetclinic.client;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.vetclinic.client")
@EnableJpaRepositories(basePackages = "com.vetclinic.client.infrastructure.persistence")
public class ClientModuleConfig {}
