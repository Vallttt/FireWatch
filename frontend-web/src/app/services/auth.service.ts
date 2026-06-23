import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';
import { jwtDecode } from 'jwt-decode';

/* ================================================================ */
/*  INTERFACES                                                      */
/* ================================================================ */
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
}

export interface LoginResponse {
  token: string;
  refreshToken?: string;
  type: string;
  userId: string;
  email: string;
  role: 'USER' | 'ADMIN';
  expiresIn?: number;
}

export interface RegisterResponse {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  role: 'USER' | 'ADMIN';
}

export interface RefreshTokenResponse {
  token: string;
  refreshToken?: string;
  expiresIn?: number;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  email: string;
  code: string;
  newPassword: string;
}

export interface JWTPayload {
  sub: string;
  email: string;
  role: string;
  iat: number;
  exp: number;
}

/* ================================================================ */
/*  CONSTANTS                                                       */
/* ================================================================ */
const TOKEN_KEY = 'jwt_token';
const REFRESH_TOKEN_KEY = 'refresh_token';
const USER_ID_KEY = 'userId';
const USER_EMAIL_KEY = 'userEmail';
const USER_ROLE_KEY = 'userRole';
const TOKEN_EXPIRY_KEY = 'tokenExpiry';
const REFRESH_THRESHOLD_SECONDS = 300; // Refrescar 5 min antes de expirar

/* ================================================================ */
/*  SERVICE                                                         */
/* ================================================================ */
@Injectable({ providedIn: 'root' })
export class AuthService {

  private authUrl = `${environment.apiGateway}/auth`;
  private usersUrl = `${environment.apiGateway}/api/users`;
  private passwordUrl = `${environment.apiGateway}/api/auth/password`;

  // ✅ BehaviorSubject para notificar cambios de estado de autenticación
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.isLoggedIn());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient) {
    this.initializeAuthState();
  }

  /**
   * ✅ Inicializar estado de autenticación al cargar el servicio
   */
  private initializeAuthState(): void {
    const token = this.getToken();
    if (token && !this.isTokenExpired(token)) {
      this.isAuthenticatedSubject.next(true);
    } else if (token && this.isTokenExpired(token)) {
      // ✅ Si token expirado pero existe, limpiar
      this.logout();
    }
  }

  /**
   * ✅ LOGIN: Guardar token y refresh token
   */
  login(body: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.authUrl}/login`, body).pipe(
      tap((res) => {
        this.storeTokens(res);
        this.isAuthenticatedSubject.next(true);
      })
    );
  }

  /**
   * ✅ REGISTRO
   */
  register(body: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.usersUrl}/register`, body);
  }

  /**
   * ✅ LOGOUT: Limpiar todo
   */
  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_ID_KEY);
    localStorage.removeItem(USER_EMAIL_KEY);
    localStorage.removeItem(USER_ROLE_KEY);
    localStorage.removeItem(TOKEN_EXPIRY_KEY);
    localStorage.removeItem('emergencyMode');
    this.isAuthenticatedSubject.next(false);
  }

  /**
   * ✅ FORGOT PASSWORD
   */
  forgotPassword(email: string) {
    return this.http.post(`${this.passwordUrl}/forgot`, { email });
  }

  /**
   * ✅ RESET PASSWORD
   */
  resetPassword(email: string, code: string, newPassword: string) {
    return this.http.post(`${this.passwordUrl}/reset`, {
      email,
      code,
      newPassword
    });
  }

  /**
   * ✅ OBTENER TOKEN ACTUAL
   */
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  /**
   * ✅ OBTENER REFRESH TOKEN
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  /**
   * ✅ VALIDAR SI USUARIO ESTÁ AUTENTICADO
   */
  isLoggedIn(): boolean {
    const token = this.getToken();
    return !!token && !this.isTokenExpired(token);
  }

  /**
   * ✅ DECODIFICAR JWT Y EXTRAER PAYLOAD
   */
  decodeToken(token: string): JWTPayload | null {
    try {
      return jwtDecode<JWTPayload>(token);
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  }

  /**
   * ✅ VALIDAR SI TOKEN ESTÁ EXPIRADO
   * Considera el threshold de refresh (5 minutos antes de expirar)
   */
  isTokenExpired(token: string, offsetSeconds: number = 0): boolean {
    try {
      const payload = this.decodeToken(token);
      if (!payload || !payload.exp) {
        return true;
      }

      // ✅ Convertir segundos Unix a milisegundos
      const expiryTime = payload.exp * 1000;
      const now = Date.now();

      // ✅ Si está a menos de 5 minutos de expirar, considerarlo como expirado
      return now >= expiryTime - (REFRESH_THRESHOLD_SECONDS * 1000) - offsetSeconds;
    } catch (error) {
      console.error('Error validating token expiry:', error);
      return true;
    }
  }

  /**
   * ✅ OBTENER TIEMPO DE EXPIRACIÓN DEL TOKEN (en segundos)
   */
  getTokenExpiryTime(token: string): number | null {
    const payload = this.decodeToken(token);
    return payload ? payload.exp : null;
  }

  /**
   * ✅ OBTENER TIEMPO RESTANTE HASTA EXPIRACIÓN (en segundos)
   */
  getTimeUntilExpiry(token: string): number {
    const expiryTime = this.getTokenExpiryTime(token);
    if (!expiryTime) return 0;

    const now = Math.floor(Date.now() / 1000);
    return Math.max(0, expiryTime - now);
  }

  /**
   * ✅ REFRESCAR TOKEN: Usar refresh token para obtener uno nuevo
   */
  refreshToken(): Observable<RefreshTokenResponse> {
    const refreshToken = this.getRefreshToken();

    if (!refreshToken) {
      this.logout();
      throw new Error('No refresh token available');
    }

    return this.http.post<RefreshTokenResponse>(`${this.authUrl}/refresh`, {
      refreshToken
    }).pipe(
      tap((res) => {
        // ✅ Guardar nuevo token
        localStorage.setItem(TOKEN_KEY, res.token);

        // ✅ Actualizar refresh token si viene uno nuevo
        if (res.refreshToken) {
          localStorage.setItem(REFRESH_TOKEN_KEY, res.refreshToken);
        }

        // ✅ Guardar tiempo de expiración
        if (res.expiresIn) {
          const expiryTime = Date.now() + (res.expiresIn * 1000);
          localStorage.setItem(TOKEN_EXPIRY_KEY, expiryTime.toString());
        }

        this.isAuthenticatedSubject.next(true);
      })
    );
  }

  /**
   * ✅ OBTENER DATOS DEL USUARIO DEL TOKEN
   */
  getCurrentUser(): JWTPayload | null {
    const token = this.getToken();
    return token ? this.decodeToken(token) : null;
  }

  /**
   * ✅ OBTENER ROLE DEL USUARIO
   */
  getUserRole(): string {
    return localStorage.getItem(USER_ROLE_KEY) || 'citizen';
  }

  /**
   * ✅ OBTENER ID DEL USUARIO
   */
  getUserId(): string | null {
    return localStorage.getItem(USER_ID_KEY);
  }

  /**
   * ✅ GUARDAR TOKENS DESPUÉS DE LOGIN
   */
  private storeTokens(response: LoginResponse): void {
    localStorage.setItem(TOKEN_KEY, response.token);

    if (response.refreshToken) {
      localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
    }

    localStorage.setItem(USER_ID_KEY, response.userId);
    localStorage.setItem(USER_EMAIL_KEY, response.email);
    localStorage.setItem(USER_ROLE_KEY, response.role === 'ADMIN' ? 'admin' : 'citizen');

    // ✅ Calcular y guardar tiempo de expiración
    if (response.expiresIn) {
      const expiryTime = Date.now() + (response.expiresIn * 1000);
      localStorage.setItem(TOKEN_EXPIRY_KEY, expiryTime.toString());
    } else {
      // ✅ Si no viene expiresIn, usar el exp del JWT
      const payload = this.decodeToken(response.token);
      if (payload) {
        const expiryTime = payload.exp * 1000;
        localStorage.setItem(TOKEN_EXPIRY_KEY, expiryTime.toString());
      }
    }
  }
}
