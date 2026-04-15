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
}

export interface Notificacion {
  id: number;
  tipo: string;
  mensaje: string;
  fechaEnvio: string;
  [key: string]: any;          // campos extra que envíe el backend
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
}
