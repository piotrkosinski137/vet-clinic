package com.vetclinic.veterinarian;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan
@EnableJpaRepositories(basePackages = "com.vetclinic.veterinarian.infrastructure.persistence")
public class VeterinarianModuleConfig {}
