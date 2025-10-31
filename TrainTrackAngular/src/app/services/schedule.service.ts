import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SavedSchedule {
  id?: number;
  congestionLevel: string;
  predictedRidership: number;
  confidence: number;
  timestamp: string;
  timeLabel?: string;
  station: {
    id: number;
    name?: string;
  };
  userId: number;
}

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {
  private baseUrl = 'http://localhost:8080/api/schedules';

  constructor(private http: HttpClient) {}

  saveSchedule(schedule: SavedSchedule): Observable<SavedSchedule> {
    return this.http.post<SavedSchedule>(`${this.baseUrl}`, schedule);
  }

  getUserSchedules(userId: number): Observable<SavedSchedule[]> {
    return this.http.get<SavedSchedule[]>(`${this.baseUrl}/user/${userId}`);
  }

  deleteSchedule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}