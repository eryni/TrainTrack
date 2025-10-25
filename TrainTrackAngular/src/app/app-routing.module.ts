import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReportProblemComponent } from './pages/report-problem/report-problem.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';

const routes: Routes = [
  { path: 'report', component: ReportProblemComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' } // default route
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
