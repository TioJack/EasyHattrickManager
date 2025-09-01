import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {Currency, DataResponse, Language, UserConfig} from './model/data-response';

@Injectable({
  providedIn: 'root'
})
export class DataService {
  private apiUrl = '/backend/data';

  constructor(private http: HttpClient) {
  }

  gatData(): Observable<DataResponse> {
    return this.http.get<DataResponse>(this.apiUrl);
  }

  getLanguages(): Observable<Language[]> {
    return this.http.get<Language[]>(`${this.apiUrl}/languages`);
  }

  getCurrencies(): Observable<Currency[]> {
    return this.http.get<Currency[]>(`${this.apiUrl}/currencies`);
  }

  saveUserConfig(config: UserConfig): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/user-config`, config);
  }

  update(): Observable<void> {
    return this.http.get<void>(`${this.apiUrl}/update`);
  }

}
