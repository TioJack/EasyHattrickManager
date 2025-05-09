import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {DataResponse} from '../home/model/data-response';

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
}
