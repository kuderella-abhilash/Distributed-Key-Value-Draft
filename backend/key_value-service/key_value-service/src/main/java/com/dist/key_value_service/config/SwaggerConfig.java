package com.dist.key_value_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.node-id:node-1}")
    private String nodeId;

    @Bean
    public OpenAPI kvServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KV Service API")
                        .description(
                                "Distributed Key-Value Store — KV Service (" + nodeId + ")\n\n" +
                                        "Handles all CRUD operations on key-value pairs. " +
                                        "Writes are published to Kafka for replication. " +
                                        "Reads use Redis cache with PostgreSQL fallback."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Distributed KV Team")
                                .email("team@dist-kv.com"))
                        .license(new License()
                                .name("MIT License")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("KV Node 1"),
                        new Server().url("http://localhost:8085").description("KV Node 2"),
                        new Server().url("http://localhost:8086").description("KV Node 3")
                ));
    }
}