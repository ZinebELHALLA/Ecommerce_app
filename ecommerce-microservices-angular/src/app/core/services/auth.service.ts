import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
export interface User {
  id: number;
  username: string;
  email: string;
  role: 'MANAGER' | 'USER';
}
export interface LoginRequest {
  username: string;
  password: string;
}
export interface LoginResponse {
  token: string;
  username: string;
  role: string;
  userId: number;
}
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role: 'MANAGER' | 'USER';
}
@Injectable({
  providedIn: 'root'
})
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  
  constructor(
    private router: Router,
    private http: HttpClient
  ) {
    this.loadUserFromStorage();
  }

  private loadUserFromStorage(): void {
    const token = this.getToken();
    const storedUser = localStorage.getItem('current_user');
    
    if (token && storedUser) {
      try {
        const user = JSON.parse(storedUser);
        this.currentUserSubject.next(user);
      } catch (e) {
        console.error('Failed to parse user from storage', e);
        this.logout();
      }
    } else if (token) {
       // Fallback if token exists but no user (e.g. old session), try decode or force login
       this.logout(); 
    }
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${environment.apiUrl}/client-service/api/auth/login`,
      { username: credentials.username, password: credentials.password }
    ).pipe(
      tap(response => {
        // 1. Construct User object from explicit response fields
        const user: User = {
          id: response.userId,
          username: response.username,
          email: response.username, // or from response if available
          role: response.role as 'MANAGER' | 'USER'
        };

        // 2. Save complete session
        this.saveSession(response.token, user);
      })
    );
  }

  register(data: RegisterRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${environment.apiUrl}/client-service/api/auth/register`,
      {
        username: data.username,
        email: data.email || `${data.username}@example.com`,
        password: data.password,
        firstName: data.username,
        lastName: '',
        phone: '0000000000'
      }
    );
  }

  logout(): void {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('current_user');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  private saveSession(token: string, user: User): void {
    localStorage.setItem('auth_token', token);
    localStorage.setItem('current_user', JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  getToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }

  isManager(): boolean {
    const user = this.getCurrentUser();
    return user?.role === 'MANAGER';
  }

  getUserRole(): 'MANAGER' | 'USER' | null {
    return this.getCurrentUser()?.role || null;
  }
}