import { Capacitor } from '@capacitor/core';

export const environment = {
  production: false,

  // Única puerta de entrada: API Gateway
  // Frontend → Gateway (:8000) → BFF (:8001) → Microservicios
  //
  // Dentro del emulador/dispositivo Android nativo, "localhost" apunta al
  // propio dispositivo, no a la máquina host — hay que usar 10.0.2.2 (el
  // alias que usa el emulador para llegar al host). En la build web normal
  // (navegador, Docker) sí es localhost.
  apiGateway: Capacitor.isNativePlatform() ? 'http://10.0.2.2:8000' : 'http://localhost:8000'
};
