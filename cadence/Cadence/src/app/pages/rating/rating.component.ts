import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-rating',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './rating.component.html',
  styleUrls: ['./rating.component.css']
})
export class RatingComponent {
  testimonials = [
    {
      name: 'Harper & Co. Interior Design',
      image: 'assets/client1.jpg',
      rating: 5,
      text: 'Working with this team was an absolute pleasure. Their attention to detail and creative vision transformed our space beyond our expectations.'
    },
    {
      name: 'Home Decor Enthusiast',
      image: 'assets/client2.jpg',
      rating: 5,
      text: 'The level of professionalism and expertise shown throughout the project was exceptional. Highly recommend their services!'
    }
  ];
} 