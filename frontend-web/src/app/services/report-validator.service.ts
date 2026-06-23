import { Injectable } from '@angular/core';

/**
 * ✅ REPORT VALIDATOR SERVICE
 * Valida toda la información de un reporte antes de enviarlo
 * Asegura que el nivel de emergencia, tipo y ubicación sean correctos
 */

export interface ReportValidation {
  isValid: boolean;
  errors: string[];
  warnings: string[];
  summary: ReportSummary;
}

export interface ReportSummary {
  tipo: string;
  nivel: string;
  nivelLabel: string;
  descripcion: string;
  ubicacion: {
    latitud: number;
    longitud: number;
  };
  zona: string;
  severidad: string;
}

@Injectable({ providedIn: 'root' })
export class ReportValidatorService {

  /**
   * ✅ Mapeo de niveles de emergencia con descripciones
   */
  private readonly emergencyLevels = {
    'baja': {
      label: 'BAJO',
      color: '#16a34a',
      icon: '🟢',
      description: 'Riesgo bajo - Incendio controlable con recursos disponibles'
    },
    'media': {
      label: 'MEDIO',
      color: '#f59e0b',
      icon: '🟡',
      description: 'Riesgo medio - Requiere movilización de brigadas'
    },
    'alta': {
      label: 'ALTO',
      color: '#ef4444',
      icon: '🔴',
      description: 'Riesgo alto - Situación crítica que requiere recursos significativos'
    },
    'critica': {
      label: 'CRÍTICA',
      color: '#7c2d12',
      icon: '⚫',
      description: 'Riesgo crítico - Emergencia extrema, evacuación requerida'
    }
  };

  /**
   * ✅ Mapeo de tipos de incendios
   */
  private readonly fireTypes = {
    'FORESTAL': {
      label: 'Incendio Forestal',
      safetyDistance: { baja: 300, media: 1000, alta: 3000, critica: 5000 },
      description: 'Fuego en áreas boscosas o vegetación silvestre'
    },
    'URBANO': {
      label: 'Incendio Urbano',
      safetyDistance: { baja: 100, media: 300, alta: 500, critica: 1000 },
      description: 'Fuego en áreas pobladas o zonas construidas'
    },
    'ESTRUCTURAL': {
      label: 'Incendio Estructural',
      safetyDistance: { baja: 50, media: 100, alta: 200, critica: 500 },
      description: 'Fuego en edificios o estructuras'
    }
  };

  /**
   * ✅ VALIDAR REPORTE COMPLETO
   */
  validateReport(
    tipo: string,
    severidad: string,
    descripcion: string,
    latitud: number,
    longitud: number,
    zona: string
  ): ReportValidation {

    const errors: string[] = [];
    const warnings: string[] = [];

    // ✅ Validar tipo de incendio
    if (!tipo || !this.fireTypes[tipo as keyof typeof this.fireTypes]) {
      errors.push('❌ Tipo de incendio no válido. Debe ser: FORESTAL, URBANO o ESTRUCTURAL');
    }

    // ✅ Validar severidad
    if (!severidad || !this.emergencyLevels[severidad as keyof typeof this.emergencyLevels]) {
      errors.push('❌ Nivel de severidad no válido. Debe ser: baja, media, alta o critica');
    }

    // ✅ Validar descripción
    if (!descripcion || descripcion.trim().length < 10) {
      errors.push('❌ Descripción debe tener al menos 10 caracteres');
    }

    if (descripcion && descripcion.length > 1000) {
      warnings.push('⚠️ La descripción es muy larga (máximo 1000 caracteres)');
    }

    // ✅ Validar ubicación (coordenadas)
    if (!this.isValidLatitude(latitud)) {
      errors.push(`❌ Latitud inválida (debe estar entre -90 y 90). Recibido: ${latitud}`);
    }

    if (!this.isValidLongitude(longitud)) {
      errors.push(`❌ Longitud inválida (debe estar entre -180 y 180). Recibido: ${longitud}`);
    }

    // ✅ Validar zona
    if (!zona || zona.trim() === '') {
      errors.push('❌ Zona no especificada');
    }

    // ✅ Validaciones de coherencia
    if (tipo === 'FORESTAL' && (severidad === 'media' || severidad === 'alta' || severidad === 'critica')) {
      warnings.push('⚠️ Incendio forestal con nivel de severidad elevado - Riesgo de propagación rápida');
    }

    if (severidad === 'critica') {
      warnings.push('⚠️ ALERTA CRÍTICA ACTIVADA - Se notificará a todas las brigadas disponibles');
    }

    const isValid = errors.length === 0;

    return {
      isValid,
      errors,
      warnings,
      summary: {
        tipo: this.fireTypes[tipo as keyof typeof this.fireTypes]?.label || tipo,
        nivel: this.emergencyLevels[severidad as keyof typeof this.emergencyLevels]?.label || severidad,
        nivelLabel: this.getEmergencyLevelLabel(severidad),
        descripcion: descripcion?.substring(0, 100) + (descripcion?.length > 100 ? '...' : ''),
        ubicacion: { latitud, longitud },
        zona,
        severidad
      }
    };
  }

  /**
   * ✅ Obtener información completa de nivel de emergencia
   */
  getEmergencyLevelInfo(nivel: string): any {
    return this.emergencyLevels[nivel as keyof typeof this.emergencyLevels] || this.emergencyLevels['baja'];
  }

  /**
   * ✅ Obtener información de tipo de incendio
   */
  getFireTypeInfo(tipo: string): any {
    return this.fireTypes[tipo as keyof typeof this.fireTypes] || this.fireTypes['URBANO'];
  }

  /**
   * ✅ Obtener distancia de seguridad recomendada
   */
  getSafetyDistance(tipo: string, severidad: string): number {
    const fireType = this.fireTypes[tipo as keyof typeof this.fireTypes];
    if (!fireType) return 200;

    return fireType.safetyDistance[severidad as keyof typeof fireType.safetyDistance] || fireType.safetyDistance['media'];
  }

  /**
   * ✅ Obtener etiqueta formateada de nivel de emergencia
   */
  getEmergencyLevelLabel(nivel: string): string {
    const info = this.emergencyLevels[nivel as keyof typeof this.emergencyLevels];
    return info ? `${info.icon} ${info.label}` : nivel.toUpperCase();
  }

  /**
   * ✅ Validar latitud
   */
  private isValidLatitude(lat: number): boolean {
    return typeof lat === 'number' && lat >= -90 && lat <= 90;
  }

  /**
   * ✅ Validar longitud
   */
  private isValidLongitude(lng: number): boolean {
    return typeof lng === 'number' && lng >= -180 && lng <= 180;
  }

  /**
   * ✅ Generar resumen en formato legible para notificaciones
   */
  generateSummaryText(validation: ReportValidation): string {
    const summary = validation.summary;
    return `
🔥 ${summary.tipo}
${this.getEmergencyLevelLabel(summary.severidad)}
📍 Lat: ${summary.ubicacion.latitud.toFixed(4)}, Long: ${summary.ubicacion.longitud.toFixed(4)}
📝 ${summary.descripcion}
    `.trim();
  }

  /**
   * ✅ Generar color según nivel de severidad
   */
  getColorByLevel(nivel: string): string {
    return this.emergencyLevels[nivel as keyof typeof this.emergencyLevels]?.color || '#6b7280';
  }
}
