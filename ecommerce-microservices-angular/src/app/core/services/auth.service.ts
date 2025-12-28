import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';

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

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role: 'MANAGER' | 'USER';
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  
  constructor(private router: Router) {
    // Check if user is already logged in
    this.loadUserFromStorage();
  }

  private loadUserFromStorage(): void {
    const token = this.getToken();
    if (token) {
      const user = this.decodeToken(token);
      this.currentUserSubject.next(user);
    }
  }

  login(credentials: LoginRequest): Observable<{ token: string; user: User }> {
    // Mock login - in real app, this would call backend
    // For demo: username and password can be anything
    
    // Determine role based on username (mock logic)
    const role: 'MANAGER' | 'USER' = credentials.username.toLowerCase().includes('admin') || 
                                       credentials.username.toLowerCase().includes('manager') 
                                       ? 'MANAGER' : 'USER';
    
    const user: User = {
      id: Math.floor(Math.random() * 1000000),
      username: credentials.username,
      email: `${credentials.username}@example.com`,
      role: role
    };

    // Create mock JWT token
    const token = this.createMockToken(user);
    
    // Simulate API delay
    return of({ token, user }).pipe(delay(500));
  }

  register(data: RegisterRequest): Observable<{ message: string }> {
    // Mock registration - in real app, would call backend
    return of({ message: 'Registration successful! Please login.' }).pipe(delay(500));
  }

  logout(): void {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('current_user');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  saveToken(token: string): void {
    localStorage.setItem('auth_token', token);
    const user = this.decodeToken(token);
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

  private createMockToken(user: User): string {
    // Create a mock JWT-like token (NOT secure, just for demo)
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(JSON.stringify({
      sub: user.id.toString(),
      username: user.username,
      email: user.email,
      role: user.role,
      iat: Date.now(),
      exp: Date.now() + (24 * 60 * 60 * 1000) // 24 hours
    }));
    const signature = btoa('mock-signature');
    return `${header}.${payload}.${signature}`;
  }

  private decodeToken(token: string): User {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) throw new Error('Invalid token');
      
      const payload = JSON.parse(atob(parts[1]));
      return {
        id: parseInt(payload.sub),
        username: payload.username,
        email: payload.email,
        role: payload.role
      };
    } catch (error) {
      console.error('Error decoding token:', error);
      return { id: 0, username: 'Unknown', email: '', role: 'USER' };
    }
  }
}
