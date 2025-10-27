import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ScheduleService, SavedSchedule } from 'src/app/services/schedule.service';
import { User } from '../../models/user.model';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  currentUser: User | null = null;
  savedSchedules: SavedSchedule[] = [];
  activePrediction: any = null;
  loading = false;
  errorMessage = '';

  private readonly BASE_URL = 'http://localhost:8080/api';

  constructor(
    private authService: AuthService,
    private scheduleService: ScheduleService,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => {
      this.currentUser = user;
      if (!user) {
        this.router.navigate(['/login']);
        return;
      }
      this.loadSavedSchedules();
    });
  }

  /** ✅ Load all user schedules */
  loadSavedSchedules(): void {
    if (!this.currentUser) return;
    const userId = (this.currentUser as any).id || (this.currentUser as any).userId;

    this.scheduleService.getUserSchedules(userId).subscribe({
      next: (data) => {
        this.savedSchedules = data;
      },
      error: (err) => {
        console.error('❌ Failed to load schedules:', err);
        this.errorMessage = 'Unable to load your saved schedules.';
      }
    });
  }

  /** ✅ When user clicks a saved schedule */
  viewPredictionForSaved(s: SavedSchedule): void {
  // 🧩 If same station is clicked again, toggle it closed
  if (this.activePrediction && this.activePrediction.station?.id === s.station?.id) {
    this.activePrediction = null;
    return;
  }

  if (!s.station?.id) return;

  this.activePrediction = null;
  this.loading = true;
  this.errorMessage = '';

  // 🕒 Extract station ID
  const stationId = s.station.id;

  // 🕓 Compute minutesAhead (same as PredictionComponent)
  const [selectedHour, selectedMinute] = s.timestamp.split(':').map(Number);

  const now = new Date();
  const nowMinutes = now.getHours() * 60 + now.getMinutes();
  const selectedMinutes = selectedHour * 60 + selectedMinute;
  let minutesAhead = selectedMinutes - nowMinutes;
  if (minutesAhead < 0) minutesAhead += 24 * 60;

  // 🧠 Fetch prediction from same endpoint as PredictionComponent
  this.http
    .get(`${this.BASE_URL}/predict?stationId=${stationId}&minutesAhead=${minutesAhead}`)
    .subscribe({
      next: (data) => {
        this.activePrediction = {
          ...data,
          station: s.station,
          // use display-friendly time range label (same as dropdown)
          predictedTime: this.formatHourRange(String(selectedHour))
        };
        this.loading = false;
        console.log('✅ Dashboard prediction result:', this.activePrediction);
      },
      error: (err) => {
        console.error('❌ Failed to fetch prediction for saved schedule:', err);
        this.loading = false;
        this.errorMessage = 'Could not fetch prediction.';
      }
    });
}

  /** ✅ Helper: mimic hour labels from Prediction page dropdown */
 formatHourRange(timestamp: string | undefined): string {
  if (!timestamp) return '';

  // Extract the hour safely (works with ISO timestamps too)
  const parts = timestamp.match(/T(\d{2}):(\d{2})/);
  if (!parts) return '';

  const hour = parseInt(parts[1], 10); // e.g. "11"
  const pad = (n: number) => n.toString().padStart(2, '0');

  // Show same-hour range like "11:00 – 11:59"
  return `${pad(hour)}:00 – ${pad(hour)}:59`;
}
  /** ✅ Reuse helpers for styling */
  getCongestionTextClass(level?: string): string {
    if (!level) return '';
    const lower = level.toLowerCase();
    if (lower.includes('heavy')) return 'bad-time';
    if (lower.includes('moderate')) return 'okay-time';
    return 'good-time';
  }

  getCongestionStatus(level?: string): string {
    if (!level) return 'UNKNOWN';
    const lower = level.toLowerCase();
    if (lower.includes('heavy')) return 'BAD TIME TO TRAVEL';
    if (lower.includes('moderate')) return 'FAIR TIME TO TRAVEL';
    return 'GOOD TIME TO TRAVEL';
  }
  
  getCongestionClass(level: string): string {
  if (!level) return '';
  return level.toLowerCase().replace(/\s+/g, '-');
}

  /** ✅ Logout user */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
  getSelectedHourLabel(value: string): string {
  const timeOptions = [
    { label: '00:00 – 00:59', value: '00:00' },
    { label: '01:00 – 01:59', value: '01:00' },
    { label: '02:00 – 02:59', value: '02:00' },
    { label: '03:00 – 03:59', value: '03:00' },
    { label: '04:00 – 04:59', value: '04:00' },
    { label: '05:00 – 05:59', value: '05:00' },
    { label: '06:00 – 06:59', value: '06:00' },
    { label: '07:00 – 07:59', value: '07:00' },
    { label: '08:00 – 08:59', value: '08:00' },
    { label: '09:00 – 09:59', value: '09:00' },
    { label: '10:00 – 10:59', value: '10:00' },
    { label: '11:00 – 11:59', value: '11:00' },
    { label: '12:00 – 12:59', value: '12:00' },
    { label: '13:00 – 13:59', value: '13:00' },
    { label: '14:00 – 14:59', value: '14:00' },
    { label: '15:00 – 15:59', value: '15:00' },
    { label: '16:00 – 16:59', value: '16:00' },
    { label: '17:00 – 17:59', value: '17:00' },
    { label: '18:00 – 18:59', value: '18:00' },
    { label: '19:00 – 19:59', value: '19:00' },
    { label: '20:00 – 20:59', value: '20:00' },
    { label: '21:00 – 21:59', value: '21:00' },
    { label: '22:00 – 22:59', value: '22:00' },
    { label: '23:00 – 23:59', value: '23:00' }
  ];

  const match = timeOptions.find(t => value.includes(t.value));
  return match ? match.label : value;
}
  
}
