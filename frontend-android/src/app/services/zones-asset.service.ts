import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, shareReplay } from 'rxjs';

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
    const candidatas = zones.filter(z => z.zoneType !== 'PROVINCE');
    for (const zona of candidatas) {
      if (this.geometryContainsPoint(zona.geometry, lng, lat)) {
        return zona;
      }
    }
    return null;
  }

  // ─── Point-in-polygon (ray casting), sin dependencias externas ─────────────

  private geometryContainsPoint(geometry: GeoJSON.Geometry, lng: number, lat: number): boolean {
    if (geometry.type === 'Polygon') {
      return this.polygonContainsPoint(geometry.coordinates as number[][][], lng, lat);
    }
    if (geometry.type === 'MultiPolygon') {
      return (geometry.coordinates as number[][][][]).some(poly => this.polygonContainsPoint(poly, lng, lat));
    }
    return false;
  }

  /** rings[0] = anillo exterior, rings[1..] = agujeros. */
  private polygonContainsPoint(rings: number[][][], lng: number, lat: number): boolean {
    if (!this.ringContainsPoint(rings[0], lng, lat)) return false;
    for (let i = 1; i < rings.length; i++) {
      if (this.ringContainsPoint(rings[i], lng, lat)) return false; // dentro de un agujero
    }
    return true;
  }

  private ringContainsPoint(ring: number[][], lng: number, lat: number): boolean {
    let inside = false;
    for (let i = 0, j = ring.length - 1; i < ring.length; j = i++) {
      const [xi, yi] = ring[i];
      const [xj, yj] = ring[j];
      const intersect = ((yi > lat) !== (yj > lat)) &&
        (lng < (xj - xi) * (lat - yi) / (yj - yi) + xi);
      if (intersect) inside = !inside;
    }
    return inside;
  }
}
