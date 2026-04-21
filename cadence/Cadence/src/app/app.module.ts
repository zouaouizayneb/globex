// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppComponent } from './app.component';
import { appRoutes} from './app.routes'; // Import du fichier de routes
import { HomeComponent } from './homepage/home/home.component'; // Le composant Home
import { RouterModule } from '@angular/router'; // Import du RouterModule
import { provideHttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@NgModule({
  declarations: [
    
  ],
  imports: [
    BrowserModule,
    RouterModule.forRoot(appRoutes), // Ajout du RouterModule avec appRoutes
  ],
  providers: [],
  bootstrap: []
})
export class AppModule { }
