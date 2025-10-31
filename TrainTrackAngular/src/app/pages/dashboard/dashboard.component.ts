import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/users.service';
import { ScheduleService, SavedSchedule } from '../../services/schedule.service';
import { ReportService, ReportedIssue } from '../../services/report.service';
import { User } from '../../models/user.model';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  savedSchedules: SavedSchedule[] = [];
  userReports: ReportedIssue[] = [];
  activePrediction: any = null;
  loading = false;
  errorMessage = '';
  
  currentDateTime = {
    date: '',
    time: ''
  };
  private clockInterval: any;

  showUsernameForm = false;
  showPasswordForm = false;
  showBioForm = false;
  showImageUpload = false;
  newUsername = '';
  oldPassword = '';
  newPassword = '';
  newBio = '';
  selectedFile: File | null = null;
  imagePreview: string | null = null;
  usernameError = '';
  passwordError = '';
  bioError = '';
  imageError = '';
  usernameSuccess = '';
  passwordSuccess = '';
  bioSuccess = '';
  imageSuccess = '';
  checkingUsername = false;

  private readonly BASE_URL = 'http://localhost:8080/api';

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private scheduleService: ScheduleService,
    private reportService: ReportService,
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
      this.loadUserReports();
      this.loadUserStatistics();
    });

    this.updateDateTime();
    this.clockInterval = setInterval(() => this.updateDateTime(), 1000);
  }

  ngOnDestroy(): void {
    if (this.clockInterval) {
      clearInterval(this.clockInterval);
    }
  }

  updateDateTime(): void {
    const now = new Date();
    const options: Intl.DateTimeFormatOptions = {
      timeZone: 'Asia/Manila',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    };
    const timeOptions: Intl.DateTimeFormatOptions = {
      timeZone: 'Asia/Manila',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: true
    };
    
    this.currentDateTime.date = now.toLocaleDateString('en-PH', options);
    this.currentDateTime.time = now.toLocaleTimeString('en-PH', timeOptions);
  }

  loadSavedSchedules(): void {
    if (!this.currentUser) return;
    const userId = (this.currentUser as any).id || (this.currentUser as any).userId;

    this.scheduleService.getUserSchedules(userId).subscribe({
      next: (data) => {
        this.savedSchedules = data;
      },
      error: (err) => {
        console.error('Failed to load schedules:', err);
        this.errorMessage = 'Unable to load your saved schedules.';
      }
    });
  }

  loadUserReports(): void {
    if (!this.currentUser) return;
    const userId = (this.currentUser as any).id || (this.currentUser as any).userId;

    this.reportService.getUserReports(userId).subscribe({
      next: (reports) => {
        this.userReports = reports;
      },
      error: (err) => {
        console.error('Failed to load reports:', err);
      }
    });
  }

  deleteSchedule(scheduleId: number | undefined, event: Event): void {
    event.stopPropagation();
    
    if (!scheduleId) {
      console.error('Schedule ID is undefined');
      return;
    }

    if (!confirm('Are you sure you want to remove this saved schedule?')) {
      return;
    }

    this.scheduleService.deleteSchedule(scheduleId).subscribe({
      next: () => {
        this.savedSchedules = this.savedSchedules.filter(s => s.id !== scheduleId);
        
        if (this.activePrediction && 
            this.savedSchedules.find(s => s.id === scheduleId)?.station?.id === this.activePrediction.station?.id) {
          this.activePrediction = null;
        }
      },
      error: (err) => {
        console.error('Failed to delete schedule:', err);
        alert('Failed to delete schedule. Please try again.');
      }
    });
  }

  viewPredictionForSaved(s: SavedSchedule): void {
    if (this.activePrediction && this.activePrediction.station?.id === s.station?.id) {
      this.activePrediction = null;
      return;
    }

    if (!s.station?.id) return;

    this.activePrediction = null;
    this.loading = true;
    this.errorMessage = '';

    const stationId = s.station.id;
    const [selectedHour, selectedMinute] = s.timestamp.split(':').map(Number);

    const now = new Date();
    const nowMinutes = now.getHours() * 60 + now.getMinutes();
    const selectedMinutes = selectedHour * 60 + selectedMinute;
    let minutesAhead = selectedMinutes - nowMinutes;
    if (minutesAhead < 0) minutesAhead += 24 * 60;

    this.http
      .get(`${this.BASE_URL}/predict?stationId=${stationId}&minutesAhead=${minutesAhead}`)
      .subscribe({
        next: (data) => {
          this.activePrediction = {
            ...data,
            station: s.station,
            predictedTime: this.formatHourRange(String(selectedHour))
          };
          this.loading = false;
        },
        error: (err) => {
          console.error('Failed to fetch prediction for saved schedule:', err);
          this.loading = false;
          this.errorMessage = 'Could not fetch prediction.';
        }
      });
  }

  formatHourRange(timestamp: string | undefined): string {
    if (!timestamp) return '';

    const parts = timestamp.match(/T(\d{2}):(\d{2})/);
    if (!parts) return '';

    const hour = parseInt(parts[1], 10);
    const pad = (n: number) => n.toString().padStart(2, '0');

    return `${pad(hour)}:00 – ${pad(hour)}:59`;
  }

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

  toggleUsernameForm(): void {
    this.showUsernameForm = !this.showUsernameForm;
    this.showPasswordForm = false;
    this.showBioForm = false;
    this.showImageUpload = false;
    this.clearMessages();
  }

  togglePasswordForm(): void {
    this.showPasswordForm = !this.showPasswordForm;
    this.showUsernameForm = false;
    this.showBioForm = false;
    this.showImageUpload = false;
    this.clearMessages();
  }

  toggleBioForm(): void {
    this.showBioForm = !this.showBioForm;
    this.showUsernameForm = false;
    this.showPasswordForm = false;
    this.showImageUpload = false;
    this.clearMessages();
  }

  toggleImageUpload(): void {
    this.showImageUpload = !this.showImageUpload;
    this.showUsernameForm = false;
    this.showPasswordForm = false;
    this.showBioForm = false;
    this.clearMessages();
    this.selectedFile = null;
    this.imagePreview = null;
  }

  clearMessages(): void {
    this.usernameError = '';
    this.passwordError = '';
    this.bioError = '';
    this.imageError = '';
    this.usernameSuccess = '';
    this.passwordSuccess = '';
    this.bioSuccess = '';
    this.imageSuccess = '';
  }

  onUsernameInput(): void {
    if (this.newUsername.trim().length < 3) {
      this.usernameError = '';
      return;
    }

    this.checkingUsername = true;
    this.usernameError = '';

    this.userService.checkUsernameAvailability(this.newUsername.trim()).subscribe({
      next: (response: any) => {
        this.checkingUsername = false;
        if (!response.available) {
          this.usernameError = 'Username is already taken';
        }
      },
      error: () => {
        this.checkingUsername = false;
      }
    });
  }

  changeUsername(): void {
    this.clearMessages();

    if (!this.newUsername.trim()) {
      this.usernameError = 'Please enter a new username.';
      return;
    }

    if (this.newUsername.length < 3) {
      this.usernameError = 'Username must be at least 3 characters long.';
      return;
    }

    const userId = (this.currentUser as any).id || (this.currentUser as any).userId;

    this.userService.changeUsername(userId, this.newUsername.trim()).subscribe({
      next: (response) => {
        this.usernameSuccess = 'Username updated successfully!';
        
        if (this.currentUser && response.user) {
          this.currentUser.firstName = response.user.firstName;
          
          localStorage.setItem('currentUser', JSON.stringify(this.currentUser));
          this.authService['currentUserSubject'].next(this.currentUser);
        }
        
        this.newUsername = '';
        setTimeout(() => {
          this.showUsernameForm = false;
          this.clearMessages();
        }, 2000);
      },
      error: (err) => {
        this.usernameError = err.error?.error || 'Failed to update username. Please try again.';
      }
    });
  }

  changePassword(): void {
    this.clearMessages();

    if (!this.oldPassword || !this.newPassword) {
      this.passwordError = 'Please fill in all password fields.';
      return;
    }

    if (this.newPassword.length < 8) {
      this.passwordError = 'New password must be at least 8 characters long.';
      return;
    }

    const userId = (this.currentUser as any).id || (this.currentUser as any).userId;

    this.userService.changePassword(userId, this.oldPassword, this.newPassword).subscribe({
      next: () => {
        this.passwordSuccess = 'Password updated successfully!';
        this.oldPassword = '';
        this.newPassword = '';
        setTimeout(() => {
          this.showPasswordForm = false;
          this.clearMessages();
        }, 2000);
      },
      error: (err) => {
        this.passwordError = err.error?.error || 'Failed to update password. Please try again.';
      }
    });
  }

  updateBio(): void {
    this.clearMessages();

    if (!this.newBio.trim()) {
      this.bioError = 'Please enter a bio.';
      return;
    }

    if (this.newBio.length > 500) {
      this.bioError = 'Bio must be less than 500 characters.';
      return;
    }

    const userId = (this.currentUser as any).id || (this.currentUser as any).userId;

    this.userService.updateBio(userId, this.newBio.trim()).subscribe({
      next: (response) => {
        this.bioSuccess = 'Bio updated successfully!';
        
        if (this.currentUser && response.user) {
          this.currentUser.lastName = response.user.lastName;
          
          localStorage.setItem('currentUser', JSON.stringify(this.currentUser));
          this.authService['currentUserSubject'].next(this.currentUser);
        }
        
        this.newBio = '';
        setTimeout(() => {
          this.showBioForm = false;
          this.clearMessages();
        }, 2000);
      },
      error: (err) => {
        this.bioError = err.error?.error || 'Failed to update bio. Please try again.';
      }
    });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (!file.type.startsWith('image/')) {
        this.imageError = 'Please select an image file';
        return;
      }

      if (file.size > 5 * 1024 * 1024) {
        this.imageError = 'File size must not exceed 5MB';
        return;
      }

      this.selectedFile = file;
      this.imageError = '';

      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.imagePreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  uploadProfileImage(): void {
    if (!this.selectedFile) {
      this.imageError = 'Please select an image file';
      return;
    }

    const userId = (this.currentUser as any).id || (this.currentUser as any).userId;
    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.userService.uploadProfileImage(userId, formData).subscribe({
      next: (response) => {
        this.imageSuccess = 'Profile image updated successfully!';
        
        if (this.currentUser && response.imageUrl) {
          this.currentUser.profileImageUrl = response.imageUrl;
          
          localStorage.setItem('currentUser', JSON.stringify(this.currentUser));
          this.authService['currentUserSubject'].next(this.currentUser);
        }
        
        setTimeout(() => {
          this.showImageUpload = false;
          this.clearMessages();
          this.selectedFile = null;
          this.imagePreview = null;
        }, 2000);
      },
      error: (err) => {
        this.imageError = err.error?.error || 'Failed to upload image. Please try again.';
      }
    });
  }

  userStatistics = {
    mostSearchedStation: 'N/A',
    mostSearchedDays: 'N/A'
  };

  loadUserStatistics(): void {
    if (!this.currentUser) return;
    const userId = (this.currentUser as any).id || (this.currentUser as any).userId;

    this.userService.getUserStatistics(userId).subscribe({
      next: (stats) => {
        this.userStatistics.mostSearchedStation = stats.mostSearchedStation || 'N/A';
        this.userStatistics.mostSearchedDays = stats.mostSearchedDays || 'N/A';
      },
      error: (err) => {
        console.error('Failed to load user statistics:', err);
      }
    });
  }


  getReportStatusClass(status: string): string {
    const statusMap: {[key: string]: string} = {
      'pending': 'status-pending',
      'investigating': 'status-investigating',
      'resolved': 'status-resolved',
      'closed': 'status-closed'
    };
    return statusMap[status.toLowerCase()] || 'status-pending';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}