import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';

/* ------------------------------------------------------------------ */
/*  Enums                                                              */
/* ------------------------------------------------------------------ */
export type BrigadeStatus = 'AVAILABLE' | 'DEPLOYED' | 'OFFLINE';
export type GeoReportStatus = 'ACTIVE' | 'INACTIVE';
export type SeverityLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type ZoneType = 'MAIN' | 'OPERATIONAL';

/* ------------------------------------------------------------------ */
/*  DTOs (match bff-service real DTOs)                                 */
/* ------------------------------------------------------------------ */
export interface BrigadeRequest {
  name: string;
  institution: string;
  status: BrigadeStatus;
  latitude: number;
  longitude: number;
  /** Requerido por el backend (BrigadeRequestDTO.zoneId es @NotNull) */
  zoneId: string;
}

export interface BrigadeResponse {
  id: string;
  name: string;
  institution: string;
  status: BrigadeStatus;
  latitude: number;
  longitude: number;
  isActive: boolean;
  zoneId: string;
  zoneName: string;
}

/** Reporte georreferenciado, sincronizado por report-service hacia geo-service. */
export interface MappedReportResponse {
  id: string;
  externalReportId: string;
  reportStatus: GeoReportStatus;
  severity: SeverityLevel;
  latitude: number;
  longitude: number;
  reportedAt: string;
  lastSyncAt: string;
  zoneId: string;
}

export interface ZoneRequest {
  name: string;
  description: string;
  isActive: boolean;
  /** Formato "#RRGGBB" */
  color: string;
  zoneType: ZoneType;
  geoJson: string;
}

export interface ZoneResponse {
  id: string;
  name: string;
  color: string;
  zoneType: ZoneType;
  geoJson: string;
}

export interface EvacuationRouteRequest {
  name: string;
  description: string;
  geoJson: string;
  zoneId: string;
}

export interface EvacuationResponse {
  id: string;
  name: string;
  description: string;
  geoJson: string;
  zoneId: string;
}

/** Respuesta consolidada de GET /api/map-data */
export interface MapDataResponse {
  zones: ZoneResponse[];
  evacuationRoutes: EvacuationResponse[];
  brigades: BrigadeResponse[];
  reports: MappedReportResponse[];
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

/* ------------------------------------------------------------------ */
/*  Service                                                            */
/* ------------------------------------------------------------------ */
@Injectable({ providedIn: 'root' })
export class GeoService {

  private apiUrl = environment.apiGateway;

  constructor(private http: HttpClient) {}

  /* ----------  MAP DATA (all-in-one)  ---------- */
  /** Devuelve zonas, rutas de evacuación, brigadas y reportes mapeados en una sola llamada. */
  getMapData(): Observable<MapDataResponse> {
    return this.http.get<ApiResponse<MapDataResponse>>(`${this.apiUrl}/api/map-data`)
      .pipe(map(r => r.data));
  }

  /* ----------  BRIGADES  ---------- */
  getBrigades(): Observable<BrigadeResponse[]> {
    return this.http.get<BrigadeResponse[]>(`${this.apiUrl}/api/brigades`);
  }

  getBrigade(id: string): Observable<BrigadeResponse> {
    return this.http.get<BrigadeResponse>(`${this.apiUrl}/api/brigades/${id}`);
  }

  createBrigade(body: BrigadeRequest): Observable<BrigadeResponse> {
    return this.http.post<BrigadeResponse>(`${this.apiUrl}/api/brigades`, body);
  }

  updateBrigade(id: string, body: BrigadeRequest): Observable<BrigadeResponse> {
    return this.http.put<BrigadeResponse>(`${this.apiUrl}/api/brigades/${id}`, body);
  }

  deleteBrigade(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/api/brigades/${id}`);
  }

  /* ----------  ZONES  ---------- */
  getZones(): Observable<ZoneResponse[]> {
    return this.http.get<ZoneResponse[]>(`${this.apiUrl}/api/zones`);
  }

  getMainZone(): Observable<ZoneResponse> {
    return this.http.get<ZoneResponse>(`${this.apiUrl}/api/zones/main`);
  }

  getOperationalZones(): Observable<ZoneResponse[]> {
    return this.http.get<ZoneResponse[]>(`${this.apiUrl}/api/zones/operational`);
  }

  getActiveZones(): Observable<ZoneResponse[]> {
    return this.http.get<ZoneResponse[]>(`${this.apiUrl}/api/zones/active`);
  }

  createZone(body: ZoneRequest): Observable<ZoneResponse> {
    return this.http.post<ZoneResponse>(`${this.apiUrl}/api/zones`, body);
  }

  updateZone(id: string, body: ZoneRequest): Observable<ZoneResponse> {
    return this.http.put<ZoneResponse>(`${this.apiUrl}/api/zones/${id}`, body);
  }

  deleteZone(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/api/zones/${id}`);
  }

  /* ----------  EVACUATION ROUTES  ---------- */
  getEvacuationRoutes(): Observable<EvacuationResponse[]> {
    return this.http.get<EvacuationResponse[]>(`${this.apiUrl}/api/evacuation-routes`);
  }

  getEvacuationRoutesByZone(zoneId: string): Observable<EvacuationResponse[]> {
    return this.http.get<EvacuationResponse[]>(`${this.apiUrl}/api/evacuation-routes/zone/${zoneId}`);
  }

  createEvacuationRoute(body: EvacuationRouteRequest): Observable<EvacuationResponse> {
    return this.http.post<EvacuationResponse>(`${this.apiUrl}/api/evacuation-routes`, body);
  }

  updateEvacuationRoute(id: string, body: EvacuationRouteRequest): Observable<EvacuationResponse> {
    return this.http.put<EvacuationResponse>(`${this.apiUrl}/api/evacuation-routes/${id}`, body);
  }

  deleteEvacuationRoute(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/api/evacuation-routes/${id}`);
  }

  /* ----------  MAPPED REPORTS (geo-service)  ---------- */
  getMappedReports(): Observable<MappedReportResponse[]> {
    return this.http.get<MappedReportResponse[]>(`${this.apiUrl}/api/geo`);
  }

  /** Borrado lógico: al finalizar un incendio, deja de aparecer en el mapa para siempre. */
  deleteMappedReport(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/api/geo/${id}`);
  }
}
