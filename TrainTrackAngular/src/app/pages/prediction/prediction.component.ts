import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';
import { ScheduleService, SavedSchedule } from '../../services/schedule.service';
import { PowerBIService } from '../../services/powerbi.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-prediction',
  templateUrl: './prediction.component.html',
  styleUrls: ['./prediction.component.css']
})
export class PredictionComponent implements OnInit {
  currentUser: User | null = null;
  stations: any[] = [];
  timeOptions = [
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
  errorMessage = '';
  saveMessage: string | null = null;
  
  historicalTrendsUrl: SafeResourceUrl | null = null;
  peakHoursUrl: SafeResourceUrl | null = null;
  stationComparisonUrl: SafeResourceUrl | null = null;
  powerBILoaded = false;
  powerBIError = false;
  
  private baseHistoricalUrl: string = '';
  private basePeakHoursUrl: string = '';
  private baseStationComparisonUrl: string = '';

  private readonly BASE_URL = 'http://localhost:8080/api';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private router: Router,
    private scheduleService: ScheduleService,
    private powerBIService: PowerBIService,
    private sanitizer: DomSanitizer 
  ) {}

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => {
      this.currentUser = user;
    });

    this.loadStations();
    this.loadPowerBIConfig();
  }

  loadStations(): void {
    this.http.get<any[]>(`${this.BASE_URL}/stations`).subscribe({
      next: (data) => {
        this.stations = data.sort((a, b) => a.id - b.id);
        if (this.stations.length > 0 && !this.stationId) {
          this.stationId = this.stations[0].id;
        }
      },
      error: (err) => {
        console.error('Failed to load stations', err);
        this.errorMessage = 'Failed to load stations. Please refresh the page.';
      }
    });
  }

  fetchPrediction() {
    if (!this.stationId) {
      this.errorMessage = 'Please select a station first.';
      return;
    }

    this.stationId = Number(this.stationId);
    const [hour, minute] = this.selectedHour.split(':').map(Number);
    const now = new Date();

    const nowMinutes = now.getHours() * 60 + now.getMinutes();
    const selectedMinutes = hour * 60 + minute;
    let minutesAhead = selectedMinutes - nowMinutes;
    if (minutesAhead < 0) minutesAhead += 24 * 60;

    this.loading = true;
    this.errorMessage = '';
    this.prediction = null;

    this.http.get(`${this.BASE_URL}/predict?stationId=${this.stationId}&minutesAhead=${minutesAhead}`)
      .subscribe({
        next: (data) => {
          this.prediction = data;
          this.loading = false;
          this.updatePowerBIFilters();
        },
        error: (err) => {
          console.error('Prediction fetch failed', err);
          this.errorMessage = 'Failed to fetch prediction. Please try again.';
          this.loading = false;
        },
      });
  }

  getStationName(id: number | null): string {
    if (id == null) return 'Unknown Station';
    const found = this.stations.find(s => s.id === id);
    return found ? found.name : 'Unknown Station';
  }

  getSelectedHourLabel(value: string): string {
    const match = this.timeOptions.find(t => t.value === value);
    return match ? match.label : value;
  }

  getCongestionClass(level: string): string {
    if (!level) return '';
    return level.toLowerCase().replace(/\s+/g, '-');
  }

  saveSelection() {
    if (!this.stationId || !this.selectedHour) {
      this.saveMessage = "Please select both station and time first.";
      return;
    }
    
    if (!this.currentUser) {
      this.saveMessage = "Please login to save selections.";
      setTimeout(() => this.router.navigate(['/login']), 1500);
      return;
    }

    const userId = (this.currentUser as any).id || (this.currentUser as any).userId;
    const newSchedule: SavedSchedule = {
      congestionLevel: this.prediction?.congestionLevel || 'Unknown',
      predictedRidership: this.prediction?.predictedRidership || 0,
      confidence: this.prediction?.confidence || 0,
      timestamp: this.selectedHour,
      timeLabel: this.getSelectedHourLabel(this.selectedHour),
      station: { id: this.stationId },
      userId
    };

    this.saveMessage = 'Saving...';

    this.scheduleService.saveSchedule(newSchedule).subscribe({
      next: () => {
        this.saveMessage = 'Saved to Dashboard!';
        setTimeout(() => {
          this.router.navigate(['/dashboard']);
        }, 1000);
      },
      error: (err) => {
        console.error('Save failed:', err);
        this.saveMessage = 'Failed to save. Please try again.';
      }
    });
  }

  loadPowerBIConfig(): void {
    this.powerBIService.getPowerBIConfig().subscribe({
      next: (config) => {
        console.log('Power BI Config received:', config);
        
        this.baseHistoricalUrl = config.historicalTrendsUrl || '';
        this.basePeakHoursUrl = config.peakHoursUrl || '';
        this.baseStationComparisonUrl = config.stationComparisonUrl || '';
        
        if (!this.baseHistoricalUrl || !this.basePeakHoursUrl || !this.baseStationComparisonUrl) {
          console.warn('Some Power BI URLs are missing:', config);
          this.powerBIError = true;
        }
        
        if (this.baseHistoricalUrl) {
          this.historicalTrendsUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.baseHistoricalUrl);
        }
        if (this.basePeakHoursUrl) {
          this.peakHoursUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.basePeakHoursUrl);
        }
        if (this.baseStationComparisonUrl) {
          this.stationComparisonUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.baseStationComparisonUrl);
        }
        
        this.powerBILoaded = true;
        console.log('Power BI URLs loaded successfully');
      },
      error: (err) => {
        console.error('Failed to load Power BI config:', err);
        this.powerBILoaded = true;
        this.powerBIError = true;
      }
    });
  }

  updatePowerBIFilters(): void {
    if (!this.stationId || !this.baseHistoricalUrl || !this.basePeakHoursUrl) {
      console.warn('Cannot update filters - missing station or URLs');
      return;
    }

    const selectedStationName = this.getStationName(this.stationId);
    
    const historicalFiltered = this.addStationFilter(this.baseHistoricalUrl, selectedStationName);
    const peakHoursFiltered = this.addStationFilter(this.basePeakHoursUrl, selectedStationName);
    
    this.historicalTrendsUrl = this.sanitizer.bypassSecurityTrustResourceUrl(historicalFiltered);
    this.peakHoursUrl = this.sanitizer.bypassSecurityTrustResourceUrl(peakHoursFiltered);
    
    console.log(`Power BI reports filtered for station: ${selectedStationName}`);
  }

  private addStationFilter(baseUrl: string, stationName: string): string {
    const cleanUrl = baseUrl.split('&$filter=')[0];
    
    const filterParam = `&$filter=Station/name eq '${encodeURIComponent(stationName)}'`;
    
    return cleanUrl + filterParam;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}