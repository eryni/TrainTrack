import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface ReportedIssue {
  id?: number;
  title: string;
  description: string;
  userId: number;
  status?: string;
  reportedAt?: Date;
  stationId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private apiUrl = 'http://localhost:8080/api/reports';

  constructor(private http: HttpClient) {}

  submitReport(report: ReportedIssue): Observable<any> {
    return this.http.post(`${this.apiUrl}`, report)
      .pipe(catchError(this.handleError));
  }

  getUserReports(userId: number): Observable<ReportedIssue[]> {
    return this.http.get<ReportedIssue[]>(`${this.apiUrl}/user/${userId}`)
      .pipe(catchError(this.handleError));
  }

  getAllReports(): Observable<ReportedIssue[]> {
    return this.http.get<ReportedIssue[]>(`${this.apiUrl}`)
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
    }

    return throwError(() => ({ 
      error: { 
        error: errorMessage,
        status: error.status 
      } 
    }));
  }
}