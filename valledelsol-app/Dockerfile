# 1. Compilamos el código de React usando Node
FROM node:20-alpine AS builder
WORKDIR /app

COPY package*.json ./

# Instalamos dependencias
RUN npm install

# Forzamos tipos de jest
RUN npm install --save-dev @types/jest

COPY . .

RUN npm run build

# 2. Servidor Web (Nginx)
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
# Reemplazamos la configuración por defecto de Nginx con la nuestra personalizada
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]