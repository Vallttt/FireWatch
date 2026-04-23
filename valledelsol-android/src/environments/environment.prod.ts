export const environment = {
  production: true,

  // En Docker: nginx proxy inverso redirige /api y /auth al Gateway
  // Usa ruta relativa (misma URL del frontend)
  apiGateway: ''
};
