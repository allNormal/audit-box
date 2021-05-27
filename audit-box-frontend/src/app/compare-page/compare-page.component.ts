import {ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {HomepageComponent} from '../homepage/homepage.component';
import {Provenance, ProvenanceList} from '../entitiy/endpoint-entity';
import {Observable, Subscription} from 'rxjs';

@Component({
  selector: 'app-compare-page',
  templateUrl: './compare-page.component.html',
  styleUrls: ['./compare-page.component.css']
})
export class ComparePageComponent implements OnInit, OnDestroy {

  provenance1: Provenance =  new Provenance();
  provenance2: Provenance =  new Provenance();
  provenanceList: Observable<Provenance[]>;
  compareMessage: string[] = [];
  compareMessage2: string[] = [];
  subscription1: Subscription = new Subscription();
  subscription2: Subscription = new Subscription();
  notCalculated = true;

  constructor(private homepage:HomepageComponent, private cdrf:ChangeDetectorRef) { }

  ngOnInit(): void {
    this.provenance1 = this.homepage.provenanceClicked;
    this.provenanceList = this.homepage.provenances;
    this.subscription2 = this.provenanceList.subscribe(
      (provenence: Provenance[]) =>{
        this.provenance2 = provenence[0]
      }
    )
  }

  ngOnDestroy() {
    this.subscription1.unsubscribe();
    this.subscription2.unsubscribe();
  }

  changeProvenence2(title) {
    this.subscription2 = this.provenanceList.subscribe(provenences => {
      this.provenance2 = provenences.find(provenence => provenence.execution_time === title)
    })
    console.log(this.provenance2)
  }

  changeProvenence1(title) {
    this.subscription1 = this.provenanceList.subscribe(provenences => {
      this.provenance1 = provenences.find(provenence => provenence.execution_time === title)
    })
  }

  async checkingDiff() {
    let count = 0;
    this.compareMessage = []
    this.compareMessage2 = []
    let check = false;
    this.notCalculated = false;
    for(let item of this.provenance1.entities) {
      let alreadyFound = []
      check = false;
      for(let key of Object.keys(item.values)) {
        let temp = Object.values(item.values)
        for(let item2 of this.provenance2.entities){
          if(alreadyFound.find(x => x === temp[count]+key+item.name)) {
            check = true;
            break;
          }
          if(Array.from(Object.values(item2.values)).includes(temp[count])) {
            alreadyFound.push(temp[count]+key+item.name)
            check = true;
            break;
          }
          else {
            if(this.compareMessage.find(x => x == "couldnt find matching value for " + item.name + " " + key)) {
              break;
            }
          }
        }
        count = count + 1;
        if(!check) {
          this.compareMessage.push("couldnt find matching value for " + item.name + " " + key);
        }
        check = false;
      }
      count = 0;
    }


    for(let item of this.provenance2.entities) {
      let alreadyFound = []
      check = false;
      for(let key of Object.keys(item.values)) {
        let temp = Object.values(item.values)
        for(let item2 of this.provenance1.entities){
          if(alreadyFound.find(x => x === temp[count]+key+item.name)) {
            check = true;
            break;
          }
          if(Array.from(Object.values(item2.values)).includes(temp[count])) {
            alreadyFound.push(temp[count]+key+item.name)
            check = true;
            break;
          }
          else {
            if(this.compareMessage2.find(x => x == "couldnt find matching value for " + item.name + " " + key)) {
              break;
            }
          }
        }
        count = count + 1;
        if(!check) {
          this.compareMessage2.push("couldnt find matching value for " + item.name + " " + key);
        }
        check = false;
      }
      count = 0;
    }


    for(let item of this.provenance1.agents) {
      let alreadyFound = []
      check = false;
      for(let key of Object.keys(item.values)) {
        let temp = Object.values(item.values)
        for(let item2 of this.provenance2.agents){
          if(alreadyFound.find(x => x === temp[count]+key+item.name)) {
            check = true;
            break;
          }
          if(Array.from(Object.values(item2.values)).includes(temp[count])) {
            alreadyFound.push(temp[count]+key+item.name)
            check = true;
            break;
          }
          else {
            if(this.compareMessage.find(x => x == "couldnt find matching value for " + item.name + " " + key)) {
              break;
            }
          }
        }
        count = count + 1;
        if(!check) {
          this.compareMessage.push("couldnt find matching value for " + item.name + " " + key);
        }
        check = false;
      }
      count = 0;
    }


    for(let item of this.provenance2.agents) {
      let alreadyFound = []
      check = false;
      for(let key of Object.keys(item.values)) {
        let temp = Object.values(item.values)
        for(let item2 of this.provenance1.agents){
          if(alreadyFound.find(x => x === temp[count]+key+item.name)) {
            check = true;
            break;
          }
          if(Array.from(Object.values(item2.values)).includes(temp[count])) {
            alreadyFound.push(temp[count]+key+item.name)
            check = true;
            break;
          }
          else {
            if(this.compareMessage2.find(x => x == "couldnt find matching value for " + item.name + " " + key)) {
              break;
            }
          }
        }
        count = count + 1;
        if(!check) {
          this.compareMessage2.push("couldnt find matching value for " + item.name + " " + key);
        }
        check = false;
      }
      count = 0;
    }
    this.cdrf.detectChanges()
    await this.delay(300);
    this.notCalculated = true;
  }

  private delay(ms: number)
  {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  goToResult() {

  }

  backToMainPage() {
    this.homepage.comparePage = false;
  }

}
