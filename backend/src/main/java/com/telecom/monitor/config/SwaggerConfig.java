package com.telecom.monitor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Telecom Network Incident Monitoring Platform API")
                        .version("1.0.0")
                        .description("Production-grade backend platform for monitoring telecom network infrastructure, " +
                                "detecting abnormal network conditions (latency, packet loss, outage), automated incident correlation, " +
                                "and operational resolution workflows inspired by Vodafone Idea / Airtel NOC architectures.")
                        .contact(new Contact()
                                .name("Telecom SDE Platform Team")
                                .email("engineering@telecom-monitor.internal"))
                        .license(new License().name("Apache 2.0")));
    }
}
