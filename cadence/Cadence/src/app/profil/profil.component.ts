import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-profil',
  standalone: true,
  imports: [RouterModule,FormsModule,CommonModule],
  templateUrl: './profil.component.html',
  styleUrls: ['./profil.component.css'],
})
export class ProfilComponent {
  loginForm = {
    email: '',
    password: ''
  };

  login() {
    console.log('Logging in with:', this.loginForm);
    // You can add login logic here
  }

  createAccount() {
    console.log('Create account clicked');
    // You can add account creation logic here
  }
}
