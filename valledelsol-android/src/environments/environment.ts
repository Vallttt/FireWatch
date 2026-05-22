export const environment = {
  production: false,

  // Única puerta de entrada: API Gateway
  // Frontend → Gateway (:8000) → BFF (:8001) → Microservicios
  apiGateway: 'http://localhost:8000'
};
