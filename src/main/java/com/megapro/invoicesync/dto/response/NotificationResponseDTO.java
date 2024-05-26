package com.megapro.invoicesync.dto.response;

import java.util.UUID;

import lombok.Data;

@Data
public class NotificationResponseDTO {
    private int notificationId;
    private String content;
    private UUID invoiceId;
    private String invoiceNumber;
    private boolean isRead;
    private long age;
    private int approvalId;
}
