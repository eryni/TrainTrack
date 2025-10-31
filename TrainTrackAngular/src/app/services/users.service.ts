import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) {}

  changeUsername(userId: number, newUsername: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${userId}/username`, { newUsername })
      .pipe(catchError(this.handleError));
  }

  changePassword(userId: number, currentPassword: string, newPassword: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${userId}/password`, { currentPassword, newPassword })
      .pipe(catchError(this.handleError));
  }

  updateBio(userId: number, bio: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${userId}/bio`, { bio })
      .pipe(catchError(this.handleError));
  }

  uploadProfileImage(userId: number, formData: FormData): Observable<any> {
    return this.http.post(`${this.apiUrl}/${userId}/profile-image`, formData)
      .pipe(catchError(this.handleError));
  }

  checkUsernameAvailability(username: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/check-username`, { 
      params: { username } 
    }).pipe(catchError(this.handleError));
  }

  getUserStatistics(userId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${userId}/statistics`)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
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
}