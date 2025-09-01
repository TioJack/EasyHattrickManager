import {Component, OnInit} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {NgForOf} from '@angular/common';
import {Language} from '../services/model/data-response';
import {UserConfigService} from '../services/user-config.service';
import {TranslateService} from '@ngx-translate/core';
import {DataService} from '../services/data.service';

@Component({
  selector: 'app-language',
  standalone: true,
  imports: [NgForOf, FormsModule],
  templateUrl: './language.component.html'
})
export class LanguageComponent implements OnInit {
  languages: Language[] = [];
  selectedLanguageId: number | null = null;

  constructor(
    private dataService: DataService,
    private userConfigService: UserConfigService,
    private translateService: TranslateService) {
  }

  ngOnInit(): void {
    this.userConfigService.userConfig$.subscribe(config => {
      if (config && config.languageId != null) {
        this.selectedLanguageId = config.languageId;
        this.translateService.use(this.selectedLanguageId.toString());
      }
    });
    this.dataService.getLanguages().subscribe({
      next: (languages: Language[]) => {
        this.languages = languages;
      }
    });
  }

  onLanguageChange(event: Event): void {
    this.selectedLanguageId = parseInt((event.target as HTMLSelectElement).value, 10);
    this.translateService.use(this.selectedLanguageId.toString());
    const currentConfig = this.userConfigService.getUserConfig();
    if (currentConfig) {
      const updatedConfig = {...currentConfig, languageId: this.selectedLanguageId};
      this.userConfigService.setAndSaveUserConfig(updatedConfig);
    }
  }

}
