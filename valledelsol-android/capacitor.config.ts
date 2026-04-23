import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.vallesol.app',
  appName: 'Valle del Sol',
  webDir: 'www',
  android: {
    allowMixedContent: true
  },
  server: {
    // Usar http en lugar de https para evitar Mixed Content
    // al hacer peticiones a http://10.0.2.2:8000 desde el emulador
    androidScheme: 'http'
  }
};

export default config;
