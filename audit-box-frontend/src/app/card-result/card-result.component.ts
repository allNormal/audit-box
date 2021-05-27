import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {HomepageComponent} from '../homepage/homepage.component';
import {Provenance, Provenance1, ProvenanceList} from '../entitiy/endpoint-entity';
import {Router} from '@angular/router';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-card-result',
  templateUrl: './card-result.component.html',
  styleUrls: ['./card-result.component.css']
})
export class CardResultComponent implements OnInit {

  constructor(private homepage: HomepageComponent, private _router: Router) { }

  //provenenceList: ProvenenceList;
  //time= ""
  provenanceClicked: Provenance = new Provenance()
  finishLoading = false
  provenanceTest: Observable<Provenance[]>;
  provenances: ProvenanceList;

  ngOnInit(): void {
    //this.provenenceList = this.homepage.provenenceList;
    this.provenanceTest = this.homepage.provenances;
    this.provenances = this.homepage.filteredProvenance;
    //console.log(this.provenenceTest)
    this.finishLoading = true
  }

  closeForm() {
    document.getElementById('cardModal').style.display = 'none';
    this.provenanceClicked = new Provenance()
  }

  openForm(provenence:Provenance) {
    this.provenanceClicked = provenence
    /*
    this.time = time
    for(let provenence of this.provenenceList.provenenceList) {
      if(time === provenence.execution_time) {
        this.provenenceClicked = provenence;
      }
    }

     */
    document.getElementById('cardModal').style.display = 'block';
  }

  compare() {
    this.homepage.provenanceClicked = this.provenanceClicked
    this.homepage.comparePage = true;
  }

}
