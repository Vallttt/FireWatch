import { Injectable } from '@angular/core';

export interface MediaItem {
  name: string;
  type: string;
  data: string; // base64 data URL
}

interface StoredRecord {
  reportId: string;
  media: MediaItem[];
}

/**
 * Persists report media (images / videos) in the browser's IndexedDB.
 * Data survives page reloads on the same device/browser.
 */
@Injectable({ providedIn: 'root' })
export class MediaStorageService {

  private readonly DB_NAME    = 'firewatch-media';
  private readonly STORE_NAME = 'report-media';
  private readonly DB_VERSION = 1;
  private db: IDBDatabase | null = null;

  // ------------------------------------------------------------------ //
  //  Public API
  // ------------------------------------------------------------------ //

  async saveMedia(reportId: string, files: File[]): Promise<void> {
    try {
      const db = await this.openDB();
      const media: MediaItem[] = await Promise.all(
        files.map(async f => ({
          name: f.name,
          type: f.type,
          data: await this.readAsDataURL(f)
        }))
      );
      await this.idbPut(db, { reportId, media });
    } catch (err) {
      console.warn('MediaStorageService: saveMedia failed', err);
    }
  }

  async getMedia(reportId: string): Promise<MediaItem[] | null> {
    try {
      const db = await this.openDB();
      const record = await this.idbGet(db, reportId) as StoredRecord | undefined;
      return record?.media ?? null;
    } catch (err) {
      console.warn('MediaStorageService: getMedia failed', err);
      return null;
    }
  }

  async hasMedia(reportId: string): Promise<boolean> {
    const media = await this.getMedia(reportId);
    return !!(media && media.length > 0);
  }

  async deleteMedia(reportId: string): Promise<void> {
    try {
      const db = await this.openDB();
      await this.idbDelete(db, reportId);
    } catch (err) {
      console.warn('MediaStorageService: deleteMedia failed', err);
    }
  }

  // ------------------------------------------------------------------ //
  //  IndexedDB helpers
  // ------------------------------------------------------------------ //

  private openDB(): Promise<IDBDatabase> {
    if (this.db) return Promise.resolve(this.db);
    return new Promise((resolve, reject) => {
      const req = indexedDB.open(this.DB_NAME, this.DB_VERSION);
      req.onupgradeneeded = (e) => {
        const db = (e.target as IDBOpenDBRequest).result;
        if (!db.objectStoreNames.contains(this.STORE_NAME)) {
          db.createObjectStore(this.STORE_NAME, { keyPath: 'reportId' });
        }
      };
      req.onsuccess = (e) => {
        this.db = (e.target as IDBOpenDBRequest).result;
        resolve(this.db);
      };
      req.onerror = () => reject(req.error);
    });
  }

  private idbPut(db: IDBDatabase, record: StoredRecord): Promise<void> {
    return new Promise((resolve, reject) => {
      const tx  = db.transaction(this.STORE_NAME, 'readwrite');
      const req = tx.objectStore(this.STORE_NAME).put(record);
      req.onsuccess = () => resolve();
      req.onerror  = () => reject(req.error);
    });
  }

  private idbGet(db: IDBDatabase, key: string): Promise<unknown> {
    return new Promise((resolve, reject) => {
      const tx  = db.transaction(this.STORE_NAME, 'readonly');
      const req = tx.objectStore(this.STORE_NAME).get(key);
      req.onsuccess = () => resolve(req.result);
      req.onerror  = () => reject(req.error);
    });
  }

  private idbDelete(db: IDBDatabase, key: string): Promise<void> {
    return new Promise((resolve, reject) => {
      const tx  = db.transaction(this.STORE_NAME, 'readwrite');
      const req = tx.objectStore(this.STORE_NAME).delete(key);
      req.onsuccess = () => resolve();
      req.onerror  = () => reject(req.error);
    });
  }

  private readAsDataURL(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload  = () => resolve(reader.result as string);
      reader.onerror = () => reject(reader.error);
      reader.readAsDataURL(file);
    });
  }
}
