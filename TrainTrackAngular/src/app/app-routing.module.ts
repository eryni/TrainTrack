import { ReportProblemComponent } from './pages/report-problem/report-problem.component';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  { path: 'report', component: ReportProblemComponent },
  { path: '', redirectTo: '/report', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
