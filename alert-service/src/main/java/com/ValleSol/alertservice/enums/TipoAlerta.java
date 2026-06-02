package com.ValleSol.alertservice.enums;

/**
 * Classifies the nature of the alert.
 *
 *  INCENDIO    → fire report confirmed
 *  ZONA_CRITICA → a geographic zone has been flagged as critical
 *  AUTOMATICA  → auto-triggered by the system when a new report arrives
 *  MANUAL      → manually created by an operator via the UI
 *  SISTEMA     → internal system communication (maintenance, status)
 */
public enum TipoAlerta {
    INCENDIO,
    ZONA_CRITICA,
    AUTOMATICA,
    MANUAL,
    SISTEMA
}
