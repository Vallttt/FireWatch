import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';

/* ------------------------------------------------------------------ */
/*  Enums                                                              */
/* ------------------------------------------------------------------ */
export type BrigadeStatus = 'AVAILABLE' | 'DEPLOYED' | 'OFFLINE';
export type GeoReportStatus = 'ACTIVE' | 'INACTIVE';
export type GeoSeverityLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type ZoneType = string;

/* ------------------------------------------------------------------ */
/*  Response wrappers                                                  */
/* ------------------------------------------------------------------ */
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

/* ------------------------------------------------------------------ */
/*  DTOs                                                               */
/* ------------------------------------------------------------------ */
export interface BrigadeRequest {
  name: string;
  institution: string;
  status: BrigadeStatus;
  latitude: number;
  longitude: number;
  zoneId?: string;
}

export interface BrigadeResponse {
  id: string;
  name: string;
  institution: string;
  status: BrigadeStatus;
  latitude: number;
  longitude: number;
  zoneId: string;
  zoneName: string;
}

export interface MappedReportRequest {
  externalReportId: string;
  reportStatus: GeoReportStatus;
  severity: GeoSeverityLevel;
  latitude: number;
  longitude: number;
  reportedAt: string;
  zoneId?: string;
}

export interface MappedReportResponse {
  id: string;
  externalReportId: string;
  reportStatus: GeoReportStatus;
  severity: GeoSeverityLevel;
  latitude: number;
  longitude: number;
  reportedAt: string;
  lastSyncAt: string;
  zoneId: string;
}

export interface ZoneResponse {
  id: string;
  name: string;
  color: string;
  type: ZoneType;
  geoJson: string;
}

export interface EvacuationResponse {
  id: string;
  name: string;
  description: string;
  geoJson: string;
  zoneId: string;
}

export interface MapDataResponse {
  zones: ZoneResponse[];
  routes: EvacuationResponse[];
  brigades: BrigadeResponse[];
  reports: MappedReportResponse[];
}

/* ------------------------------------------------------------------ */
/*  Service                                                            */
/* ------------------------------------------------------------------ */
@Injectable({ providedIn: 'root' })
export class GeoService {

  private baseUrl = `${environment.apiGateway}/api/mapa`;

  constructor(private http: HttpClient) {}

  /* ----------  MAP DATA (all-in-one)  ---------- */
  getMapData(): Observable<MapDataResponse> {
    return this.http.get<ApiResponse<MapDataResponse>>(`${this.baseUrl}/api/map-data`)
      .pipe(map(r => r.data));
  }

  /* ----------  BRIGADES  ---------- */
  getBrigades(): Observable<BrigadeResponse[]> {
    return this.http.get<ApiResponse<BrigadeResponse[]>>(`${this.baseUrl}/api/brigades`)
      .pipe(map(r => r.data));
  }

  createBrigade(body: BrigadeRequest): Observable<BrigadeResponse> {
    return this.http.post<ApiResponse<BrigadeResponse>>(`${this.baseUrl}/api/brigades`, body)
      .pipe(map(r => r.data));
  }

  updateBrigade(id: string, body: BrigadeRequest): Observable<BrigadeResponse> {
    return this.http.put<ApiResponse<BrigadeResponse>>(`${this.baseUrl}/api/brigades/${id}`, body)
      .pipe(map(r => r.data));
  }

  deleteBrigade(id: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/api/brigades/${id}`)
      .pipe(map(r => r.data));
  }

  /* ----------  MAPPED REPORTS  ---------- */
  getMappedReports(): Observable<MappedReportResponse[]> {
    return this.http.get<ApiResponse<MappedReportResponse[]>>(`${this.baseUrl}/api/mapped-reports`)
      .pipe(map(r => r.data));
  }

  createMappedReport(body: MappedReportRequest): Observable<MappedReportResponse> {
    return this.http.post<ApiResponse<MappedReportResponse>>(`${this.baseUrl}/api/mapped-reports`, body)
      .pipe(map(r => r.data));
  }

  deleteMappedReport(id: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/api/mapped-reports/${id}`)
      .pipe(map(r => r.data));
  }

  /* ----------  ZONES  ---------- */
  getZones(): Observable<ZoneResponse[]> {
    return this.http.get<ApiResponse<ZoneResponse[]>>(`${this.baseUrl}/api/zones`)
      .pipe(map(r => r.data));
  }

  /* ----------  EVACUATION ROUTES  ---------- */
  getEvacuationRoutes(): Observable<EvacuationResponse[]> {
    return this.http.get<ApiResponse<EvacuationResponse[]>>(`${this.baseUrl}/api/evacroute`)
      .pipe(map(r => r.data));
  }
}
