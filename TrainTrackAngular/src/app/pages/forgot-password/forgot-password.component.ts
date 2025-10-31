import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit {
  forgotPasswordForm!: FormGroup;
  loading = false;
  submitted = false;
  error = '';
  success = '';
  emailToShow = 'exampleemail@mail.com';

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.forgotPasswordForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]]
    });

    this.forgotPasswordForm.get('email')?.valueChanges.subscribe(value => {
      if (value && this.forgotPasswordForm.get('email')?.valid) {
        this.emailToShow = value;
      } else {
        this.emailToShow = 'sampleemail@mail.com';
      }
    });
  }

  get f() { 
    return this.forgotPasswordForm.controls; 
  }

  onSubmit(): void {
    this.submitted = true;
    this.error = '';
    this.success = '';

    if (this.forgotPasswordForm.invalid) {
      return;
    }

    this.loading = true;
    
    this.authService.forgotPassword(this.f['email'].value).subscribe({
      next: (response: any) => {
        this.success = 'Password reset code sent! Redirecting...';
        this.loading = false;
        
        // Redirect to reset password page with email
        setTimeout(() => {
          this.router.navigate(['/reset-password'], { 
            queryParams: { email: this.f['email'].value } 
          });
        }, 1500);
      },
      error: (error: any) => {
        this.error = error.error?.error || 'Failed to send reset email. Please try again.';
        this.loading = false;
        console.error('Forgot password error:', error);
      }
    });
  }
}