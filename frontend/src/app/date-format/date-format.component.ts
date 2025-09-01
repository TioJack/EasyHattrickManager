import {Component, OnInit} from '@angular/core';
import {UserConfigService} from '../services/user-config.service';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-date-format',
  imports: [
    FormsModule
  ],
  templateUrl: './date-format.component.html'
})
export class DateFormatComponent implements OnInit {
  dateFormat: string | null = null;

  constructor(private userConfigService: UserConfigService) {
  }

  ngOnInit(): void {
    this.userConfigService.userConfig$.subscribe(config => {
      if (config && config.dateFormat != null) {
        this.dateFormat = config.dateFormat;
      }
    });
  }

  onDateFormatChange(value: string): void {
    this.dateFormat = value;
    const currentConfig = this.userConfigService.getUserConfig();
    if (currentConfig) {
      const updatedConfig = {...currentConfig, dateFormat: this.dateFormat};
      this.userConfigService.setAndSaveUserConfig(updatedConfig);
    }
  }

}
