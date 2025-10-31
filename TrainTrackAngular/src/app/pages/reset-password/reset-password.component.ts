import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit, AfterViewInit {
  @ViewChild('codeInput') codeInputElement!: ElementRef;
  
  resetPasswordForm!: FormGroup;
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
    this.resetPasswordForm = this.formBuilder.group({
      resetCode: ['', [Validators.required, Validators.pattern(/^\d{6}$/), Validators.minLength(6), Validators.maxLength(6)]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    }, {
      validators: this.passwordMatchValidator
    });
    
    this.route.queryParams.subscribe(params => {
      this.email = params['email'] || '';
      if (!this.email) {
        this.router.navigate(['/forgot-password']);
      }
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
    this.resetPasswordForm.patchValue({ resetCode: value });
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('newPassword');
    const confirmPassword = control.get('confirmPassword');

    if (!password || !confirmPassword) {
      return null;
    }

    if (password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    } else {
      const errors = confirmPassword.errors;
      if (errors) {
        delete errors['passwordMismatch'];
        confirmPassword.setErrors(Object.keys(errors).length ? errors : null);
      }
      return null;
    }
  }

  get f() { 
    return this.resetPasswordForm.controls; 
  }

  onSubmit(): void {
    this.submitted = true;
    this.error = '';
    this.success = '';

    if (this.resetPasswordForm.invalid) {
      this.error = 'Please fill in all fields correctly';
      return;
    }

    this.loading = true;
    const token = this.f['resetCode'].value;
    const newPassword = this.f['newPassword'].value;
    
    this.authService.resetPassword(this.email, token, newPassword).subscribe({
      next: (response: any) => {
        this.success = 'Password reset successfully! Redirecting to login...';
        this.loading = false;
        
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (error: any) => {
        this.error = error.error?.error || 'Failed to reset password. Please try again.';
        this.loading = false;
        this.resetPasswordForm.patchValue({ resetCode: '' });
        this.codeInputElement?.nativeElement.focus();
      }
    });
  }
}
