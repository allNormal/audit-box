import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { HomepageComponent } from './homepage/homepage.component';
import { SearchComponentComponent } from './search-component/search-component.component';
import { CardResultComponent } from './card-result/card-result.component';
import {RouterModule} from '@angular/router';
import {Endpoint} from './endpoint/endpoint';
import { NavbarComponent } from './navbar/navbar.component';
import { FooterComponent } from './footer/footer.component';
import {AppRoutingModule} from './app-routing/app-routing.module';
import {HttpClientModule} from '@angular/common/http';
import { ComparePageComponent } from './compare-page/compare-page.component';
import {FormsModule} from '@angular/forms';
import {StoreModule} from '@ngrx/store';
import {provenenceReducer} from './state/provenence.reducer';

@NgModule({
  declarations: [
    AppComponent,
    HomepageComponent,
    SearchComponentComponent,
    CardResultComponent,
    NavbarComponent,
    FooterComponent,
    ComparePageComponent,
  ],
    imports: [
        BrowserModule,
        RouterModule,
        AppRoutingModule,
        HttpClientModule,
        FormsModule,
        StoreModule.forRoot({
          provenence: provenenceReducer
        })
    ],
  providers: [Endpoint],
  bootstrap: [AppComponent]
})
export class AppModule { }
