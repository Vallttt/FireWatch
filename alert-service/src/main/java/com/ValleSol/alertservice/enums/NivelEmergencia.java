package com.ValleSol.alertservice.enums;

/**
 * Emergency classification levels used to determine priority,
 * notification channels, and target recipients.
 *
 *  CRITICO  → immediate threat, all channels + brigades + admins
 *  ALTO     → active fire/zone, push + email to brigades and admins
 *  MEDIO    → developing situation, email to admins
 *  BAJO     → informational, email only to general users
 */
public enum NivelEmergencia {
    CRITICO,
    ALTO,
    MEDIO,
    BAJO
}
