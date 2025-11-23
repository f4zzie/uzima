package com.uzima.models;

import java.time.LocalDateTime;

/**
 * Represents a summary of a service request from the admin perspective.
 */
public class ServiceRequestSummary {
    private final int requestId;
    private final int clientId;
    private final String clientName;
    private final String status;
    private final LocalDateTime requestDate;
    private final LocalDateTime scheduledDate;

    public ServiceRequestSummary(int requestId,
                                 int clientId,
                                 String clientName,
                                 String status,
                                 LocalDateTime requestDate,
                                 LocalDateTime scheduledDate) {
        this.requestId = requestId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.status = status;
        this.requestDate = requestDate;
        this.scheduledDate = scheduledDate;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    @Override
    public String toString() {
        return "ServiceRequestSummary{" +
                "requestId=" + requestId +
                ", clientId=" + clientId +
                ", clientName='" + clientName + '\'' +
                ", status='" + status + '\'' +
                ", requestDate=" + requestDate +
                ", scheduledDate=" + scheduledDate +
                '}';
    }
}
