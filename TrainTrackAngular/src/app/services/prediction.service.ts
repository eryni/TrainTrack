import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PredictionService {
  private apiUrl = 'http://localhost:8080/api/predict'; // Spring Boot endpoint

  constructor(private http: HttpClient) {}

  getPrediction(station: string): Observable<any> {
    return this.http.get(`${this.apiUrl}?station=${station}`);
  }
}
