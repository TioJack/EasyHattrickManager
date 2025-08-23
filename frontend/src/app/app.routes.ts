import {Routes} from '@angular/router';
import {LoginComponent} from './login/login.component';
import {HomeComponent} from './home/home.component';
import {AuthGuard} from './guards/auth.guard';
import {RegisterComponent} from './register/register.component';
import {SaveComponent} from './save/save.component';
import {ProjectsComponent} from './projects/projects.component';
import {ManualComponent} from './manual/manual.component';

export const routes: Routes = [
  {path: 'register', component: RegisterComponent},
  {path: 'save', component: SaveComponent},
  {path: 'login', component: LoginComponent},
  {path: 'manual', component: ManualComponent},
  {path: 'home', component: HomeComponent, canActivate: [AuthGuard]},
  {path: 'projects', component: ProjectsComponent, canActivate: [AuthGuard]},
  {path: '**', redirectTo: 'login'}
];

