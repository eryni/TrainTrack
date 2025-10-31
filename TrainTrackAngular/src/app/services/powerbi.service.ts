import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PowerBIService {
  private readonly BASE_URL = 'http://localhost:8080/api/powerbi';

  constructor(private http: HttpClient) {}

  getPowerBIConfig(): Observable<any> {
    return this.http.get(`${this.BASE_URL}/config`);
  }
}