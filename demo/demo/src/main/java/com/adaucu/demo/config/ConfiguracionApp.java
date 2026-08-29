package com.adaucu.demo.config;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ParametrosDeVenta.class)
public class ConfiguracionApp {

    @Bean
    public Clock reloj() {
        return Clock.systemUTC();
    }
}
