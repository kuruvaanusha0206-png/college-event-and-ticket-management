package com.univibe.interfaces;

import java.util.Date;

/**
 * Interface representing audit logging metadata.
 * Demonstrates: Interfaces, Date manipulation.
 */
public interface Auditable {
    Date getCreationTimestamp();
    String getAuditSummary();
}
