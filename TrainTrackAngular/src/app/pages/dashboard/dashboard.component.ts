import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
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
