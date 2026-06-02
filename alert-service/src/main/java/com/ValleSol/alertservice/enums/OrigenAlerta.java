package com.ValleSol.alertservice.enums;

/**
 * Records who or what originated the alert.
 *
 *  REPORTE  → triggered automatically when report-service creates a new report
 *  SISTEMA  → triggered by internal system logic (scheduled tasks, thresholds)
 *  MANUAL   → created explicitly by an operator via the dashboard
 */
public enum OrigenAlerta {
    REPORTE,
    SISTEMA,
    MANUAL
}
