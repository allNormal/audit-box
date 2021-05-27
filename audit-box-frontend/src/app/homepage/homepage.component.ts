import {ChangeDetectorRef, Component, Input, OnInit, Output, OnDestroy} from '@angular/core';
import {Endpoint} from '../endpoint/endpoint';
import {plainToClass} from 'class-transformer';
import {Provenance, Provenance1, ProvenanceList} from '../entitiy/endpoint-entity';
import {Observable, ReplaySubject, Subject, Subscription} from 'rxjs';
import {Store} from '@ngrx/store';
import {AppState} from '../state/app.state';
import * as ProvenenceActions from '../state/provenence.actions'

@Component({
  selector: 'app-homepage',
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css']
})
export class HomepageComponent implements OnInit, OnDestroy {

  provenances: Observable<Provenance[]>;
  response: Provenance;
  comparePage = false;
  finishRetrievingAudit = false;
  provenanceClicked: Provenance = new Provenance()
  filteredProvenance: ProvenanceList = new ProvenanceList()
  subscription: Subscription;

  constructor(private endpoint:Endpoint, private cdrf:ChangeDetectorRef, private store:Store<AppState>) {
    this.provenances = store.select('provenence')
  }

  async ngOnInit(): Promise<void> {
    this.finishRetrievingAudit = false;
    let resp = await this.endpoint.getAllAuditData();
    this.subscription = resp.subscribe(data => {
      let keys = Object.keys(data)
      let values = Object.values(data)
      let count = 0;
      for(let key of keys) {
        this.response = plainToClass(Provenance, values[count]);
        this.response.execution_time = values[count].time
        this.store.dispatch(new ProvenenceActions.addProvenence(this.response))
        this.filteredProvenance.provenanceList.push(this.response)
        count = count + 1
      }
      this.finishRetrievingAudit = true;
    })
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  updateFilteredProvenance(searchWord) {
    this.subscription = this.provenances.subscribe(provenences => {
      this.filteredProvenance.provenanceList = provenences.filter(provenence => provenence.execution_time.toLowerCase().includes(searchWord))
    })
    this.cdrf.detectChanges()
  }
}
