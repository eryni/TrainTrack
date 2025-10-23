import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/users';
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser: Observable<User | null>;

  constructor(private http: HttpClient) {
    const storedUser = this.getStoredUser();
    this.currentUserSubject = new BehaviorSubject<User | null>(storedUser);
    this.currentUser = this.currentUserSubject.asObservable();
  }

  private getStoredUser(): User | null {
    try {
      const user = localStorage.getItem('currentUser');
      return user ? JSON.parse(user) : null;
    } catch (error) {
      console.error('Error parsing stored user:', error);
      localStorage.removeItem('currentUser');
      return null;
    }
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    console.error('HTTP Error:', error);
    
    let errorMessage = 'An error occurred. Please try again.';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = 'Network error. Please check your connection.';
    } else if (error.error && typeof error.error === 'object') {
      if (error.error.error) {
        errorMessage = error.error.error;
      }
      return throwError(() => error);
    } else if (error.status === 0) {
      errorMessage = 'Cannot connect to server. Please check if the server is running.';
    } else if (error.status === 500) {
      errorMessage = 'Server error. Please try again later.';
    } else if (error.message) {
      errorMessage = error.message;
    }

    return throwError(() => ({ 
      error: { 
        error: errorMessage,
        status: error.status 
      } 
    }));
  }

  register(user: User): Observable<any> {
    console.log('Registering user:', { ...user, passwordHash: '***' });
    return this.http.post(`${this.apiUrl}/register`, user).pipe(
      tap(response => console.log('Registration response:', response)),
      catchError(this.handleError)
    );
  }

  login(email: string, password: string): Observable<User> {
    console.log('Attempting login for:', email);
    return this.http.post<any>(`${this.apiUrl}/login`, { email, password })
      .pipe(
        map(response => {
          console.log('Login response:', response);
          if (response && response.user) {
            localStorage.setItem('currentUser', JSON.stringify(response.user));
            this.currentUserSubject.next(response.user);
            return response.user;
          }
          throw new Error('Invalid response from server');
        }),
        catchError(this.handleError)
      );
  }

  logout(): void {
    console.log('Logging out user');
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  isAuthenticated(): boolean {
    const isAuth = this.currentUserValue !== null;
    console.log('Is authenticated:', isAuth);
    return isAuth;
  }

  updateProfile(userId: number, user: User): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${userId}`, user).pipe(
      tap(updatedUser => {
        console.log('Profile updated:', updatedUser);
        localStorage.setItem('currentUser', JSON.stringify(updatedUser));
        this.currentUserSubject.next(updatedUser);
      }),
      catchError(this.handleError)
    );
  }

  verifyEmail(email: string, code: string): Observable<any> {
    console.log('Verifying email:', email, 'with code:', code);
    return this.http.post(`${this.apiUrl}/verify`, { email, code }).pipe(
      tap(response => console.log('Verification response:', response)),
      catchError(this.handleError)
    );
  }

  resendVerificationCode(email: string): Observable<any> {
    console.log('Resending verification code to:', email);
    return this.http.post(`${this.apiUrl}/resend-verification`, { email }).pipe(
      tap(response => console.log('Resend response:', response)),
      catchError(this.handleError)
    );
  }

  forgotPassword(email: string): Observable<any> {
    console.log('Forgot password for:', email);
    return this.http.post(`${this.apiUrl}/forgot-password`, { email }).pipe(
      tap(response => console.log('Forgot password response:', response)),
      catchError(this.handleError)
    );
  }

  resetPassword(email: string, token: string, newPassword: string): Observable<any> {
    console.log('Resetting password for:', email, 'with token:', token);
    return this.http.post(`${this.apiUrl}/reset-password`, { 
      email, 
      token, 
      newPassword 
    }).pipe(
      tap(response => console.log('Reset password response:', response)),
      catchError(this.handleError)
    );
  }
}