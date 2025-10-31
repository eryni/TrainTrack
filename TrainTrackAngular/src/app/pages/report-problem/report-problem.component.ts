import { Component, OnDestroy, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { ReportService } from '../../services/report.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-report-problem',
  templateUrl: './report-problem.component.html',
  styleUrls: ['./report-problem.component.css']
})
export class ReportProblemComponent implements OnInit, OnDestroy {
  currentUser: any = null;
  currentDateTime = {
    date: '',
    time: ''
  };
  private clockInterval: any;

  reportTitle = '';
  reportBody = '';
  successMessage = '';
  errorMessage = '';
  isSubmitting = false;

  constructor(
    private authService: AuthService,
    private reportService: ReportService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => {
      this.currentUser = user;
      if (!user) {
        this.router.navigate(['/login']);
      }
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

  submitReport(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (!this.reportTitle.trim()) {
      this.errorMessage = 'Please enter a title for your report.';
      return;
    }

    if (!this.reportBody.trim()) {
      this.errorMessage = 'Please enter a description for your report.';
      return;
    }

    if (!this.currentUser) {
      this.errorMessage = 'You must be logged in to submit a report.';
      return;
    }

    this.isSubmitting = true;

    const userId = this.currentUser.id || this.currentUser.userId;
    const report = {
      title: this.reportTitle.trim(),
      description: this.reportBody.trim(),
      userId: userId
    };

    this.reportService.submitReport(report).subscribe({
      next: (response) => {
        console.log('Report submitted successfully:', response);
        this.successMessage = 'Report submitted successfully! Thank you for your feedback.';
        this.reportTitle = '';
        this.reportBody = '';
        this.isSubmitting = false;

        setTimeout(() => {
          this.successMessage = '';
        }, 5000);
      },
      error: (err) => {
        console.error('Failed to submit report:', err);
        this.errorMessage = err.error?.error || 'Failed to submit report. Please try again.';
        this.isSubmitting = false;
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}