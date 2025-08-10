import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {UserConfig} from './model/data-response';

@Injectable({
  providedIn: 'root'
})
export class UserConfigService {
  private userConfigSubject = new BehaviorSubject<UserConfig | null>(null);

  userConfig$: Observable<UserConfig | null> = this.userConfigSubject.asObservable();

  setUserConfig(config: UserConfig): void {
    this.userConfigSubject.next(config);
  }

  getUserConfig(): UserConfig | null {
    return this.userConfigSubject.value;
  }
}
