import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  loading = false;
  submitted = false;
  error = '';
  returnUrl: string = '';

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });

    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
    
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/']);
    }
  }

  get f() { 
    return this.loginForm.controls; 
  }

  onSubmit(): void {
    this.submitted = true;
    this.error = '';

    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    
    this.authService.login(
      this.f['email'].value, 
      this.f['password'].value
    ).subscribe({
      next: (user) => {
        this.router.navigate([this.returnUrl]);
      },
      error: (error) => {
        this.loading = false;
        
        if (error.error?.emailNotVerified) {
          this.error = 'Please verify your email before logging in.';
          
          setTimeout(() => {
            this.router.navigate(['/verify'], { 
              queryParams: { email: error.error.email } 
            });
          }, 2000);
        } else {
          this.error = error.error?.error || 'Invalid email or password';
        }
        
        console.error('Login error:', error);
      }
    });
  }
}