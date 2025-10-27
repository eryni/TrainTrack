import { Component } from '@angular/core';

@Component({
  selector: 'app-report-problem',
  templateUrl: './report-problem.component.html',
  styleUrls: ['./report-problem.component.css']
})
export class ReportProblemComponent {
  currentUser: any = null;

  ngOnInit(): void {
    // Temporary mock user — replace with your real auth data later.
    this.currentUser = { firstName: 'User00' };
  }

  logout(): void {
    // Replace with your AuthService logout logic if you have one.
    console.log('User logged out');
    // Example redirect:
    // this.router.navigate(['/login']);
  }
}


