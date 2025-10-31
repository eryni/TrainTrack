import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-verify-email',
  templateUrl: './verify-email.component.html',
  styleUrls: ['./verify-email.component.css']
})
export class VerifyEmailComponent implements OnInit, AfterViewInit {
  @ViewChild('codeInput') codeInputElement!: ElementRef;
  
  verifyForm!: FormGroup;
  loading = false;
  submitted = false;
  error = '';
  success = '';
  email = '';

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.verifyForm = this.formBuilder.group({
      verificationCode: ['', [Validators.required, Validators.pattern(/^\d{6}$/), Validators.minLength(6), Validators.maxLength(6)]]
    });
    
    this.route.queryParams.subscribe(params => {
      this.email = params['email'] || 'your email';
    });
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.codeInputElement?.nativeElement.focus();
    }, 100);
  }

  onCodeInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');
    
    if (value.length > 6) {
      value = value.substring(0, 6);
    }
    
    input.value = value;
    this.verifyForm.patchValue({ verificationCode: value });
  }

  get f() { 
    return this.verifyForm.controls; 
  }

  onSubmit(): void {
    this.submitted = true;
    this.error = '';
    this.success = '';

    if (this.verifyForm.invalid) {
      this.error = 'Please enter a valid 6-digit verification code';
      return;
    }

    this.loading = true;
    const code = this.f['verificationCode'].value;
    
    this.authService.verifyEmail(this.email, code).subscribe({
      next: (response: any) => {
        this.success = 'Email verified successfully! Redirecting to login...';
        this.loading = false;
        
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (error: any) => {
        this.error = error.error?.error || 'Invalid verification code. Please try again.';
        this.loading = false;
        this.verifyForm.patchValue({ verificationCode: '' });
        this.codeInputElement?.nativeElement.focus();
      }
    });
  }

  resendCode(): void {
    this.error = '';
    this.success = '';
    this.loading = true;
    
    this.authService.resendVerificationCode(this.email).subscribe({
      next: (response: any) => {
        this.success = 'Verification code resent! Check your email.';
        this.loading = false;
        this.verifyForm.patchValue({ verificationCode: '' });
        this.codeInputElement?.nativeElement.focus();
        setTimeout(() => {
          this.success = '';
        }, 3000);
      },
      error: (error: any) => {
        this.error = 'Failed to resend code. Please try again.';
        this.loading = false;
      }
    });
  }
}