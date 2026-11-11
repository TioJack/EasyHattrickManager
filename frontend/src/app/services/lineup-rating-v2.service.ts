import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {PlayerInfo} from './model/data-response';
import {MatchDetail, Ratings} from './model/team-training';

/** V2 is intentionally a fixed XI: it has no substitutions or penalty order. */
export interface LineupRatingV2Request {
  fieldPlayers: Array<{
    player: PlayerInfo;
    role: string;
    behaviour: string;
  }>;
  matchDetail: MatchDetail;
}

export interface RatingsV2 {
  ratings: Ratings;
}

/** Dedicated client for the independent, data-driven lineup model. */
@Injectable({providedIn: 'root'})
export class LineupRatingV2Service {
  private readonly apiUrl = '/backend/lineup/rating/v2';

  constructor(private readonly http: HttpClient) {}

  getRatingsV2(request: LineupRatingV2Request): Observable<RatingsV2> {
    return this.http.post<RatingsV2>(this.apiUrl, request);
  }
}
