import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, map, switchMap, of, catchError } from 'rxjs';
import { environment } from '../../environments/environment';

/* ------------------------------------------------------------------ */
/*  Interfaces (match backend DTOs)                                    */
/*                                                                      */
/*  El "reporte" está repartido en 3 microservicios:                   */
/*    - report-service:   datos base (descripción, ubicación, etc.)    */
/*    - incident-service:  estado (ACTIVE/INACTIVE) y severity          */
/*    - evidence-service:  fotos/videos adjuntos                        */
/*  Este service combina los tres para que las páginas sigan viendo     */
/*  un único modelo "ReporteResponse".                                  */
/* ------------------------------------------------------------------ */
export type SeverityLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type ReportStatus  = 'ACTIVE' | 'INACTIVE';

export interface ReporteRequest {
  userId?: string;
  usuarioReportante?: string;
  descripcion: string;
  /** Requerido por el backend (ReporteRequestDTO.zoneId es @NotNull) */
  zoneId: string;
  longitude: number;
  latitude: number;
  severity: SeverityLevel;
}

/** DTO base devuelto por report-service (sin estado/severity/mediaCount). */
interface ReporteBaseDTO {
  id: string;
  usuarioId: string;
  usuarioReportante: string;
  descripcion: string;
  fechaIncidente: string;   // ISO LocalDateTime
  zoneId: string;
  longitude: number;
  latitude: number;
}

/** Modelo combinado usado por las páginas. estado/severity/mediaCount son
 *  enriquecidos client-side (no vienen directos de report-service). */
export interface ReporteResponse extends ReporteBaseDTO {
  estado?: ReportStatus;
  severity?: SeverityLevel;
  mediaCount: number;
}

export interface ReportStatusUpdate {
  estado: ReportStatus;
}

/** Un incidente (estado + severity) tal como lo devuelve incident-service. */
interface IncidenteDTO {
  id: string;
  reporteId: string;
  estado: ReportStatus;
  severity: SeverityLevel;
}

/** Un foto o video adjunto a un reporte (base64 data-URL). */
export interface ReporteMediaItem {
  id: string;
  filename: string;
  contentType: string;
  /** data-URL e.g. "data:image/jpeg;base64,/9j/..." */
  data: string;
  fechaSubida: string;
}

/* ------------------------------------------------------------------ */
/*  Service                                                            */
/* ------------------------------------------------------------------ */
@Injectable({ providedIn: 'root' })
export class ReportService {

  private apiUrl = environment.apiGateway;
  private reportesUrl   = `${this.apiUrl}/api/reportes`;
  private incidentesUrl = `${this.apiUrl}/api/incidentes`;
  private evidenciasUrl = `${this.apiUrl}/api/evidencias`;

  constructor(private http: HttpClient) {}

  crearReporte(body: ReporteRequest): Observable<ReporteResponse> {
    return this.http.post<ReporteBaseDTO>(this.reportesUrl, body)
      .pipe(map(r => ({ ...r, mediaCount: 0 })));
  }

  /**
   * Lista los reportes y los enriquece con estado/severity en una sola
   * llamada extra a incident-service (evita N+1: se trae la lista completa
   * de incidentes y se cruza por reporteId).
   */
  listarReportes(): Observable<ReporteResponse[]> {
    return forkJoin({
      reportes: this.http.get<ReporteBaseDTO[]>(this.reportesUrl),
      incidentes: this.http.get<IncidenteDTO[]>(this.incidentesUrl).pipe(
        catchError(() => of([] as IncidenteDTO[]))
      )
    }).pipe(
      map(({ reportes, incidentes }) => reportes.map(r => {
        const incidente = incidentes.find(i => i.reporteId === r.id);
        return {
          ...r,
          estado: incidente?.estado,
          severity: incidente?.severity,
          mediaCount: 0
        };
      }))
    );
  }

  /** Reporte base, sin estado/severity/evidencias. Usar obtenerReporteCompleto() para el detalle. */
  obtenerReporte(id: string): Observable<ReporteResponse> {
    return this.http.get<ReporteBaseDTO>(`${this.reportesUrl}/${id}`)
      .pipe(map(r => ({ ...r, mediaCount: 0 })));
  }

  /**
   * Reporte agregado: datos base + estado/severity (incident-service) +
   * evidencias (evidence-service), todo en una sola petición al BFF.
   */
  obtenerReporteCompleto(id: string): Observable<ReporteResponse & { evidencias: ReporteMediaItem[] }> {
    return this.http.get<ReporteBaseDTO & { estado: ReportStatus; severity: SeverityLevel; evidencias: ReporteMediaItem[] }>(
      `${this.reportesUrl}/${id}/completo`
    ).pipe(
      map(r => ({ ...r, mediaCount: r.evidencias?.length ?? 0 }))
    );
  }

  /**
   * Actualiza el estado del reporte. El estado vive en incident-service,
   * indexado por su propio id (no el del reporte), así que primero se
   * resuelve el incidente asociado y luego se aplica el PATCH.
   */
  actualizarEstado(reporteId: string, body: ReportStatusUpdate): Observable<ReporteResponse> {
    return this.http.get<IncidenteDTO>(`${this.incidentesUrl}/reporte/${reporteId}`).pipe(
      switchMap(incidente =>
        this.http.patch<IncidenteDTO>(`${this.incidentesUrl}/${incidente.id}/estado`, body)
      ),
      switchMap(() => this.obtenerReporte(reporteId)),
      map(r => ({ ...r, estado: body.estado }))
    );
  }

  eliminarReporte(id: string): Observable<void> {
    return this.http.delete<void>(`${this.reportesUrl}/${id}`);
  }

  /** Upload files attached to a report (multipart/form-data) → evidence-service. */
  subirMedia(reporteId: string, files: File[]): Observable<ReporteMediaItem[]> {
    const form = new FormData();
    files.forEach(f => form.append('files', f));
    return this.http.post<ReporteMediaItem[]>(`${this.evidenciasUrl}/${reporteId}`, form);
  }

  /** Retrieve all media for a report as base64 data-URLs. */
  obtenerMedia(reporteId: string): Observable<ReporteMediaItem[]> {
    return this.http.get<ReporteMediaItem[]>(`${this.evidenciasUrl}/${reporteId}`);
  }
}
