import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

/* ------------------------------------------------------------------ */
/*  Interfaces (match backend DTOs)                                    */
/* ------------------------------------------------------------------ */
export interface AlertaRequest {
  reporteId?: string;
  mensaje: string;
  tipo: string;
  protocolo?: 'Evacuación' | 'Incendio' | 'Prevención' | 'Controlado';  // ✅ NUEVO
  /** Si se omiten, alert-service los clasifica automáticamente (ClasificadorEmergencia). */
  nivelEmergencia?: 'CRITICO' | 'ALTO' | 'MEDIO' | 'BAJO';
  destinatarios?: 'TODOS' | 'BRIGADAS' | 'ADMINISTRADORES' | 'BRIGADAS_Y_ADMINISTRADORES';
  canalEmail?: boolean;
  canalPush?: boolean;
  usuarioRemitente?: string;
}

export interface Notificacion {
  id: string;
  despachoId?: string;
  tipoAlerta: string;
  mensaje: string;
  fechaEnvio: string;
  destinatario: string;
  estadoEnvio: string;
  /** "EMAIL" | "PUSH" | "EMAIL+PUSH" */
  canal?: string;
  nivelEmergencia?: 'CRITICO' | 'ALTO' | 'MEDIO' | 'BAJO';
  /** "CIUDADANO" | "BRIGADA" | "ADMINISTRADOR" | "SISTEMA" */
  tipoDestinatario?: string;
  reporteId?: string;
  usuarioId?: string;
  usuarioRemitente?: string;
}

/* ------------------------------------------------------------------ */
/*  Service                                                            */
/* ------------------------------------------------------------------ */
@Injectable({ providedIn: 'root' })
export class AlertService {

  private baseUrl = `${environment.apiGateway}/api/alertas`;

  constructor(private http: HttpClient) {}

  enviarAlerta(body: AlertaRequest): Observable<string> {
    return this.http.post(`${this.baseUrl}/enviar`, body, { responseType: 'text' });
  }

  historial(): Observable<Notificacion[]> {
    return this.http.get<Notificacion[]>(this.baseUrl);
  }

  eliminarAlerta(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
