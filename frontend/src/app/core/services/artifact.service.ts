import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Artifact } from '../models/artifact.model';

@Injectable({ providedIn: 'root' })
export class ArtifactService {
  private base = '/api/artifacts';
  constructor(private http: HttpClient) {}

  list(museumId?: number): Observable<Artifact[]> {
    let params = new HttpParams();
    if (museumId) params = params.set('museumId', String(museumId));
    return this.http.get<{ artifacts: Artifact[] }>(this.base, { params })
      .pipe(map(r => r.artifacts));
  }

  create(payload: { name: string; era?: string; description?: string }): Observable<Artifact> {
    return this.http.post<Artifact>(this.base, payload);
  }
}