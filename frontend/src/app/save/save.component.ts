import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {AuthService} from '../services/auth.service';
import {NgFor, NgIf} from '@angular/common';
import {SaveResponse} from './model/save-response';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';

@Component({
  selector: 'app-save',
  standalone: true,
  imports: [NgIf, NgFor, RouterLink, TranslatePipe, FirstCapitalizePipe],
  templateUrl: './save.component.html',
  styleUrls: ['./save.component.scss']
})
export class SaveComponent implements OnInit {
  loading: boolean = false;
  errorMessage: string | null = null;
  saveResponse: SaveResponse | null = null;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private translateService: TranslateService
  ) {
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      const oauthToken = params['oauth_token'];
      const oauthVerifier = params['oauth_verifier'];

      if (oauthToken && oauthVerifier) {
        this.loading = true;
        this.authService.save({oauthToken, oauthVerifier}).subscribe({
          next: (response: SaveResponse) => {
            this.loading = false;
            this.saveResponse = response;
          },
          error: (error) => {
            this.loading = false;
            this.translateService.get('ehm.error-register').subscribe((translation: string) => {
              this.errorMessage = translation;
            });
          },
        });
      } else {
        this.translateService.get('ehm.missing-params').subscribe((translation: string) => {
          this.errorMessage = translation;
        });
      }
    });
  }
}
