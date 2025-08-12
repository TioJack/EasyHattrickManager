import {Component, Input, OnInit} from '@angular/core';
import {Currency} from '../services/model/data-response';
import {UserConfigService} from '../services/user-config.service';
import {NgForOf} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {firstValueFrom} from 'rxjs';

@Component({
  selector: 'app-currency',
  imports: [
    NgForOf,
    ReactiveFormsModule,
    FormsModule,
    TranslatePipe
  ],
  templateUrl: './currency.component.html'
})
export class CurrencyComponent implements OnInit {
  @Input() currencies: Currency[] = [];
  selectedCurrencyCountryId: number | null = null;
  selectedLanguageId: number | null = null;

  constructor(
    private userConfigService: UserConfigService,
    private translateService: TranslateService) {
  }

  ngOnInit(): void {
    this.userConfigService.userConfig$.subscribe(config => {
      if (config && config.currency.countryId != null) {
        this.selectedCurrencyCountryId = config.currency.countryId;
      }
      if (config && config.languageId != null) {
        this.selectedLanguageId = config.languageId;
      }
    });
    this.translateService.onLangChange.subscribe(event => {
      this.sortCurrenciesByTranslation();
    })
    this.sortCurrenciesByTranslation();
  }

  async sortCurrenciesByTranslation() {
    const translatedCurrencies = await Promise.all(
      this.currencies.map(async (currency) => {
        const leagueId: string = await firstValueFrom(this.translateService.get(`ht.country-league-${currency.countryId}`));
        const leagueName: string = await firstValueFrom(this.translateService.get(`ht.league-${leagueId}`));
        return {...currency, leagueName};
      })
    );
    this.currencies = translatedCurrencies.sort((a, b) => a.leagueName.localeCompare(b.leagueName));
  }

  onCurrencyChange(event: Event): void {
    this.selectedCurrencyCountryId = parseInt((event.target as HTMLSelectElement).value, 10);
    const currency = this.currencies.find(currency => currency.countryId === this.selectedCurrencyCountryId);
    const currentConfig = this.userConfigService.getUserConfig();
    if (currentConfig && currency) {
      const updatedConfig = {...currentConfig, currency};
      this.userConfigService.setAndSaveUserConfig(updatedConfig);
    }
  }

}
