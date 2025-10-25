import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-prediction',
  templateUrl: './prediction.component.html',
  styleUrls: ['./prediction.component.css']
})
export class PredictionComponent implements OnInit {
  currentUser: User | null = null;
  stations: any[] = [];
  times = [
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
    { label: '23:00 – 23:59', value: '23:00' },
  ];

  stationId: number | null = null;
  selectedHour: string = '08:00';
  prediction: any = null;
  loading = false;
  error = '';

  private readonly BASE_URL = 'http://localhost:8080/api';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Check if user is authenticated
    this.authService.currentUser.subscribe(user => {
      this.currentUser = user;
      if (!this.currentUser) {
        this.router.navigate(['/login']);
      }
    });

    this.loadStations();
  }

  /** Load station list from backend */
  loadStations(): void {
    this.http.get<any[]>(`${this.BASE_URL}/stations`).subscribe({
      next: (data) => {
        // Sort by station ID (route order)
        this.stations = data.sort((a, b) => a.id - b.id);
        // Preselect the first station if available
        if (this.stations.length > 0 && !this.stationId) {
          this.stationId = this.stations[0].id;
        }
      },
      error: (err) => {
        console.error('Failed to load stations', err);
        this.error = 'Failed to load stations. Please refresh the page.';
      }
    });
  }

  /** Fetch prediction for selected station/time */
  fetchPrediction() {
    if (!this.stationId) {
      this.error = 'Please select a station first.';
      return;
    }

    // Ensure stationId is a number
    this.stationId = Number(this.stationId);

    const now = new Date();
    const [hour, minute] = this.selectedHour.split(':').map(Number);
    const selected = new Date();
    selected.setHours(hour, minute, 0, 0);

    let minutesAhead = Math.floor((selected.getTime() - now.getTime()) / 60000);
    if (minutesAhead < 0) minutesAhead += 24 * 60;

    this.loading = true;
    this.error = '';
    this.prediction = null;

    this.http.get(`${this.BASE_URL}/predict?stationId=${this.stationId}&minutesAhead=${minutesAhead}`)
      .subscribe({
        next: (data) => {
          this.prediction = data;
          this.loading = false;
          console.log('Prediction received:', this.prediction);
        },
        error: (err) => {
          console.error('Prediction fetch failed', err);
          this.error = 'Failed to fetch prediction. Please try again.';
          this.loading = false;
        }
      });
  }

  getStationName(id: number | null): string {
    if (id == null) return 'Unknown Station';
    const found = this.stations.find(s => s.id === id);
    return found ? found.name : 'Unknown Station';
  }

  getSelectedHourLabel(value: string): string {
    const match = this.times.find(t => t.value === value);
    return match ? match.label : value;
  }

  getCongestionClass(level: string): string {
    if (!level) return '';
    return level.toLowerCase().replace(/\s+/g, '-');
  }

  /** Logout user */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}