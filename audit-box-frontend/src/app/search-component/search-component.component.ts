import { Component, OnInit } from '@angular/core';
import {HomepageComponent} from '../homepage/homepage.component';

@Component({
  selector: 'app-search-component',
  templateUrl: './search-component.component.html',
  styleUrls: ['./search-component.component.css']
})
export class SearchComponentComponent implements OnInit {

  constructor(private homepage:HomepageComponent) { }

  searchWord: String = ""
  ngOnInit(): void {
  }

  changeList() {
    this.homepage.updateFilteredProvenance(this.searchWord)
  }

}
