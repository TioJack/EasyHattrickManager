import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {Observable} from 'rxjs';
import {jwtDecode} from 'jwt-decode';
import {SaveResponse} from '../save/model/save-response';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/backend/login';

  constructor(private http: HttpClient, private router: Router) {
  }

  check(body: { username: string; password: string }): Observable<any> {
    return this.http.post(this.apiUrl + '/check', body, {responseType: 'text'});
  }

  save(body: { oauthToken: string; oauthVerifier: string }): Observable<SaveResponse> {
    return this.http.post<SaveResponse>(this.apiUrl + '/save', body);
  }

  login(username: string, password: string): Observable<any> {
    const body = {username, password};
    return this.http.post(this.apiUrl + '/authenticate', body, {responseType: 'text'});
  }

  saveToken(token: string): void {
    localStorage.setItem('authToken', token);
  }

  getToken(): string | null {
    return localStorage.getItem('authToken');
  }

  logout(): void {
    localStorage.removeItem('authToken');
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  isTokenExpired(): boolean {
    const token = this.getToken();
    if (!token) {
      return true;
    }
    try {
      const decodedToken: any = jwtDecode(token);
      const currentTime = Math.floor(Date.now() / 1000);
      return decodedToken.exp < currentTime;
    } catch (error) {
      return true;
    }
  }

}
