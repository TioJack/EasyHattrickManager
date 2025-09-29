import {Component, OnInit} from '@angular/core';
import {UserConfigService} from '../services/user-config.service';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-show-training-info',
  imports: [
    FormsModule
  ],
  templateUrl: './show-training-info.component.html'
})
export class ShowTrainingInfoComponent implements OnInit {
  showTrainingInfo: boolean = false;

  constructor(private userConfigService: UserConfigService) {
  }

  ngOnInit(): void {
    this.userConfigService.userConfig$.subscribe(config => {
      if (config) {
        this.showTrainingInfo = config.showTrainingInfo;
      }
    });
  }

  onShowTrainingInfoChange(value: boolean): void {
    this.showTrainingInfo = value;
    const currentConfig = this.userConfigService.getUserConfig();
    if (currentConfig) {
      const updatedConfig = {...currentConfig, showTrainingInfo: this.showTrainingInfo};
      this.userConfigService.setAndSaveUserConfig(updatedConfig);
    }
  }

}
