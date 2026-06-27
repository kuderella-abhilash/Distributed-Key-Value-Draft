package com.dist.key_value_service.event;

import lombok.*;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class KVEvent {
    private UUID eventId;
    private String operation;
    private String key;
    private String value;
    private Long version;
    private String nodeId;
}
