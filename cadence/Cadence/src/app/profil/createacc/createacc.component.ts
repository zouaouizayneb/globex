import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router'; 


@Component({
  selector: 'app-createacc',
  templateUrl: './createacc.component.html',
  styleUrls: ['./createacc.component.css'],
  standalone: true,
  imports: [FormsModule, CommonModule, RouterModule]
})
export class CreateaccComponent {
  formData = {
    firstName: '',
    lastName: '',
    email: '',
    password: ''
  };

  onSubmit() {
    // Handle form submission here
    console.log('Form submitted:', this.formData);
  }
}
