import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, shareReplay } from 'rxjs';
import booleanPointInPolygon from '@turf/boolean-point-in-polygon';
import { point } from '@turf/helpers';

/* ------------------------------------------------------------------ */
/*  Límites comunales reales (OpenStreetMap / Nominatim)               */
/*                                                                      */
/*  No vienen del backend — zone-service todavía no tiene datos reales */
/*  de comunas, así que esta capa los provee localmente como asset     */
/*  estático (src/assets/zones/comunas.geojson).                       */
/* ------------------------------------------------------------------ */
export type ComunaZoneType = 'PROVINCE' | 'MAIN' | 'OPERATIONAL';

export interface ComunaZone {
  id: string;
  name: string;
  zoneType: ComunaZoneType;
  color: string;
  geometry: GeoJSON.Geometry;
}

interface ComunaFeature {
  type: 'Feature';
  properties: { id: string; name: string; zoneType: ComunaZoneType; color: string };
  geometry: GeoJSON.Geometry;
}

interface ComunaFeatureCollection {
  type: 'FeatureCollection';
  features: ComunaFeature[];
}

@Injectable({ providedIn: 'root' })
export class ZonesAssetService {

  private readonly assetUrl = 'assets/zones/comunas.geojson';
  private cached$?: Observable<ComunaZone[]>;

  constructor(private http: HttpClient) {}

  /** Carga (una sola vez, cacheado) los límites comunales reales. */
  getZones(): Observable<ComunaZone[]> {
    if (!this.cached$) {
      this.cached$ = this.http.get<ComunaFeatureCollection>(this.assetUrl).pipe(
        map(fc => fc.features.map(f => ({
          id: f.properties.id,
          name: f.properties.name,
          zoneType: f.properties.zoneType,
          color: f.properties.color,
          geometry: f.geometry
        }))),
        shareReplay(1)
      );
    }
    return this.cached$;
  }

  /**
   * Devuelve la comuna (MAIN u OPERATIONAL, nunca PROVINCE) cuyo polígono
   * contiene el punto dado, o null si el punto cae fuera de todas.
   */
  findZoneContaining(lat: number, lng: number, zones: ComunaZone[]): ComunaZone | null {
    const pt = point([lng, lat]);
    const candidatas = zones.filter(z => z.zoneType !== 'PROVINCE');
    for (const zona of candidatas) {
      try {
        if (booleanPointInPolygon(pt, zona.geometry as any)) {
          return zona;
        }
      } catch {
        // Geometría inválida para esta comuna — la ignoramos y seguimos
      }
    }
    return null;
  }
}
