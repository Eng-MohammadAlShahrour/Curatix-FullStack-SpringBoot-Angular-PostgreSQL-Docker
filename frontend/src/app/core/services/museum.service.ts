import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Museum } from '../models/museum.model';

@Injectable({ providedIn: 'root' })
export class MuseumService {
  private base = '/api/museums';
  constructor(private http: HttpClient) {}

  list(): Observable<Museum[]> {
    return this.http.get<{ museums: Museum[] }>(this.base).pipe(map(r => r.museums));
  }

  create(payload: { name: string; country: string; city?: string }): Observable<Museum> {
    return this.http.post<Museum>(this.base, payload);
  }
}