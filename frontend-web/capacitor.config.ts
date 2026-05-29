import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.vallesol.app',
  appName: 'Valle del Sol',
  webDir: 'www',
  // SIN server.url: el APK carga los assets bundleados desde /www
  // Esto permite usar environment.android.ts con la IP real del servidor.
  // Para live-reload de desarrollo: agregar server.url y correr "ng serve --configuration android"
  android: {
    allowMixedContent: true   // permite HTTP cleartext dentro de la WebView
  }
};

export default config;
