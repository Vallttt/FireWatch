import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

/* ------------------------------------------------------------------ */
/*  Interfaces (match backend DTOs)                                    */
/* ------------------------------------------------------------------ */
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
  type: string;
  userId: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

export interface RegisterResponse {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  status: 'ACTIVE' | 'INCATIVE' | 'SUSPENDED';
  role: 'USER' | 'ADMIN';
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  email: string;
  code: string;
  newPassword: string;
}

/* ------------------------------------------------------------------ */
/*  Service                                                            */
/* ------------------------------------------------------------------ */
@Injectable({ providedIn: 'root' })
export class AuthService {

  private authUrl = `${environment.apiGateway}/auth`;
  private usersUrl = `${environment.apiGateway}/api/users`;
  private passwordUrl = `${environment.apiGateway}/api/auth/password`;

  constructor(private http: HttpClient) {}

  login(body: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.authUrl}/login`, body).pipe(
      tap(res => {
        localStorage.setItem('jwt_token', res.token);
        localStorage.setItem('userId', res.userId);
        localStorage.setItem('userEmail', res.email);
        localStorage.setItem('userRole', res.role === 'ADMIN' ? 'admin' : 'citizen');
      })
    );
  }

  register(body: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.usersUrl}/register`, body);
  }

  logout(): void {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userRole');
    localStorage.removeItem('emergencyMode');
  }

  forgotPassword(email: string) {
  return this.http.post('/api/users/password/forgot', { email });
  }

  resetPassword(email: string, code: string, newPassword: string) {
    return this.http.post('/api/users/password/reset', {
      email,
      code,
      newPassword
    });
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
