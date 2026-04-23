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
}

export interface ReportStatusUpdate {
  estado: ReportStatus;
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
}
