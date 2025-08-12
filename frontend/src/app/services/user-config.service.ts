import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {UserConfig} from './model/data-response';
import {DataService} from './data.service';

@Injectable({
  providedIn: 'root'
})
export class UserConfigService {
  private userConfigSubject = new BehaviorSubject<UserConfig | null>(null);

  userConfig$: Observable<UserConfig | null> = this.userConfigSubject.asObservable();

  constructor(private dataService: DataService) {
  }

  setUserConfig(config: UserConfig): void {
    this.userConfigSubject.next(config);
  }

  getUserConfig(): UserConfig | null {
    return this.userConfigSubject.value;
  }

  saveUserConfig(config: UserConfig): void {
    this.dataService.saveUserConfig(config).subscribe({
      next: () => console.log('User config saved successfully'),
      error: (error) => console.error('Error saving user config', error),
    });
  }

  setAndSaveUserConfig(config: UserConfig): void {
    this.setUserConfig(config);
    this.saveUserConfig(config);
  }

}
