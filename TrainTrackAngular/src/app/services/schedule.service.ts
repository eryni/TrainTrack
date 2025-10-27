// src/app/services/schedule.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// ✅ Must match your Java SavedSchedule fields
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
  private baseUrl = 'http://localhost:8080/api/schedules'; // ✅ matches @RequestMapping

  constructor(private http: HttpClient) {}

  // ✅ POST /api/schedules
  saveSchedule(schedule: SavedSchedule): Observable<SavedSchedule> {
    return this.http.post<SavedSchedule>(`${this.baseUrl}`, schedule);
  }

  // ✅ GET /api/schedules/user/{userId}
  getUserSchedules(userId: number): Observable<SavedSchedule[]> {
    return this.http.get<SavedSchedule[]>(`${this.baseUrl}/user/${userId}`);
  }

  // ✅ DELETE /api/schedules/{id} (optional if you add this endpoint later)
  deleteSchedule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
