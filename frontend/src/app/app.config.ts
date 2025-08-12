import {ApplicationConfig, provideZoneChangeDetection} from '@angular/core';
import {provideRouter} from '@angular/router';
import {routes} from './app.routes';
import {HTTP_INTERCEPTORS, HttpClient, provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {AuthInterceptor} from './interceptors/auth.interceptor';
import {provideTranslateService, TranslateLoader} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(),
    provideZoneChangeDetection({eventCoalescing: true}),
    provideHttpClient(withInterceptorsFromDi()),
    provideRouter(routes),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    provideTranslateService({
      loader: {
        provide: TranslateLoader,
        useFactory: httpLoaderFactory,
        deps: [HttpClient],
      },
      defaultLanguage: getBrowserLanguage(),
    }),
  ],
};

export function httpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http, 'assets/i18n/', `.json?v=${new Date().getTime()}`);
}

function getBrowserLanguage(): string {
  const code2LanguageId: Record<string, number> = {
    "ar": 22,
    "az": 100,
    "bg": 43,
    "ca": 66,
    "cs": 35,
    "da": 8,
    "de": 3,
    "el": 34,
    "en": 2,
    "es": 6,
    "et": 36,
    "eu": 110,
    "fa": 75,
    "fi": 9,
    "fr": 5,
    "gl": 74,
    "he": 40,
    "hu": 33,
    "id": 38,
    "it": 4,
    "ja": 12,
    "ko": 17,
    "lt": 56,
    "lv": 37,
    "nb": 7,
    "nl": 10,
    "pl": 13,
    "pt": 11,
    "ro": 23,
    "ru": 14,
    "sk": 45,
    "sl": 53,
    "sq": 85,
    "sv": 1,
    "tr": 19,
    "uk": 57,
    "zh": 15
  }
  const browserLanguage = navigator.languages && navigator.languages.length ? navigator.languages[0] : navigator.language;
  const code = browserLanguage.split('-')[0];
  return code2LanguageId[code] ? code2LanguageId[code].toString() : '2';
}
