package com.javasecurityaudit.jsa_core.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSyncEvent {
    private String entityType;
    private String action;
    private String payload;
}
