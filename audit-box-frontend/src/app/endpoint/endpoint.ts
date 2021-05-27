import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Injectable(
  {
    providedIn: 'root',
  }
)
export class Endpoint {

  private baseURL = 'http://localhost:2806/';
  constructor(private http: HttpClient) { }

  public getAllAuditData() {
    return this.http.get(this.baseURL + 'allAudit');
  }
}
