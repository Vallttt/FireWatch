import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

/* ------------------------------------------------------------------ */
/*  Interfaces (match backend DTOs)                                    */
/* ------------------------------------------------------------------ */
export type SeverityLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type ReportStatus  = 'ACTIVE' | 'INACTIVE';

export interface ReporteRequest {
  userId?: string;
  usuarioReportante?: string;
  descripcion: string;
  zoneId?: string;
  longitude: number;
  latitude: number;
  severity: SeverityLevel;
}

export interface ReporteResponse {
  id: string;
  usuarioId: string;
  usuarioReportante: string;
  descripcion: string;
  estado: ReportStatus;
  fechaIncidente: string;   // ISO LocalDateTime
  zoneId: string;
  longitude: number;
  latitude: number;
  severity: SeverityLevel;
  /** Number of media files attached (0 = none). */
  mediaCount: number;
}

export interface ReportStatusUpdate {
  estado: ReportStatus;
}

/** A single photo or video attached to a report (base64 data-URL). */
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

  private baseUrl = `${environment.apiGateway}/api/reportes`;

  constructor(private http: HttpClient) {}

  crearReporte(body: ReporteRequest): Observable<ReporteResponse> {
    return this.http.post<ReporteResponse>(this.baseUrl, body);
  }

  listarReportes(): Observable<ReporteResponse[]> {
    return this.http.get<ReporteResponse[]>(this.baseUrl);
  }

  obtenerReporte(id: string): Observable<ReporteResponse> {
    return this.http.get<ReporteResponse>(`${this.baseUrl}/${id}`);
  }

  actualizarEstado(id: string, body: ReportStatusUpdate): Observable<ReporteResponse> {
    return this.http.patch<ReporteResponse>(`${this.baseUrl}/${id}/estado`, body);
  }

  eliminarReporte(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  /** Upload files attached to a report (multipart/form-data). */
  subirMedia(reporteId: string, files: File[]): Observable<ReporteMediaItem[]> {
    const form = new FormData();
    files.forEach(f => form.append('files', f));
    return this.http.post<ReporteMediaItem[]>(`${this.baseUrl}/${reporteId}/media`, form);
  }

  /** Retrieve all media for a report as base64 data-URLs. */
  obtenerMedia(reporteId: string): Observable<ReporteMediaItem[]> {
    return this.http.get<ReporteMediaItem[]>(`${this.baseUrl}/${reporteId}/media`);
  }
}
