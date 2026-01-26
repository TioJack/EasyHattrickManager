import {Component, OnInit} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {UserConfigService} from '../services/user-config.service';

@Component({
  selector: 'app-show-sub-skills',
  imports: [
    FormsModule
  ],
  templateUrl: './show-sub-skills.component.html'
})
export class ShowSubSkillsComponent implements OnInit {
  showSubSkills: boolean = false;

  constructor(private userConfigService: UserConfigService) {
  }

  ngOnInit(): void {
    this.userConfigService.userConfig$.subscribe(config => {
      if (config) {
        this.showSubSkills = config.showSubSkills;
      }
    });
  }

  onShowSubSkillsChange(value: boolean): void {
    this.showSubSkills = value;
    const currentConfig = this.userConfigService.getUserConfig();
    if (currentConfig) {
      const updatedConfig = {...currentConfig, showSubSkills: this.showSubSkills};
      this.userConfigService.setAndSaveUserConfig(updatedConfig);
    }
  }
}
