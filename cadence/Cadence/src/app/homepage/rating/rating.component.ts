import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Testimonial {
  name: string;
  initials: string;
  rating: number;
  text: string;
  category: string;
  categoryIcon: string;
  date: string;
}

@Component({
  selector: 'app-rating',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './rating.component.html',
  styleUrls: ['./rating.component.css']
})
export class RatingComponent implements OnInit, OnDestroy {

  intervalMs = 5000;
  activeIndex = 0;
  exitIndex = -1;
  isRunning = false;

  private timer: ReturnType<typeof setInterval> | null = null;
  private progressTimer: ReturnType<typeof setTimeout> | null = null;

  testimonials: Testimonial[] = [
    {
      name: 'Sarah M.',
      initials: 'SM',
      rating: 5,
      text: 'The wall art I ordered completely transformed my living room. Packaging was perfect, delivery was fast, and the quality exceeded every expectation.',
      category: 'Home Décor',
      categoryIcon: '',
      date: 'April 2025'
    },
    {
      name: 'Karim B.',
      initials: 'KB',
      rating: 5,
      text: 'Ordered a full sportswear set and I was blown away. The fabric feels premium, the fit is spot on, and it arrived two days earlier than expected.',
      category: 'Sports & Fitness',
      categoryIcon: '',
      date: 'March 2025'
    },
    {
      name: 'Nadia R.',
      initials: 'NR',
      rating: 5,
      text: 'I got a backpack and a full stationery kit for my daughter\'s new school year — everything arrived in one clean package. She absolutely loves it.',
      category: 'School Supplies',
      categoryIcon: '',
      date: 'August 2024'
    },
    {
      name: 'Tarek H.',
      initials: 'TH',
      rating: 4,
      text: 'The linen shirt I ordered fits beautifully. The fabric is high quality, the colour is exactly as shown, and the checkout experience was seamless.',
      category: 'Clothing',
      categoryIcon: '',
      date: 'February 2025'
    },
    {
      name: 'Lina C.',
      initials: 'LC',
      rating: 5,
      text: 'I\'ve been ordering home accessories for months now and every single item has been worth it. This is genuinely my go-to store for everything in the house.',
      category: 'Home Décor',
      categoryIcon: '',
      date: 'January 2025'
    },
    {
      name: 'Youssef A.',
      initials: 'YA',
      rating: 5,
      text: 'Fast shipping, great prices, and real quality. Bought sports shoes and a gym bag — both look exactly like the photos. Very satisfied customer.',
      category: 'Sports & Fitness',
      categoryIcon: '',
      date: 'March 2025'
    }
  ];

  ngOnInit(): void {
    this.startCarousel();
  }

  ngOnDestroy(): void {
    this.clearTimers();
  }

  startCarousel(): void {
    this.resetProgress();
    this.timer = setInterval(() => this.advance(), this.intervalMs);
  }

  advance(): void {
    this.exitIndex = this.activeIndex;
    this.activeIndex = (this.activeIndex + 1) % this.testimonials.length;
    setTimeout(() => { this.exitIndex = -1; }, 700);
    this.resetProgress();
  }

  goTo(index: number): void {
    if (index === this.activeIndex) return;
    this.clearTimers();
    this.exitIndex = this.activeIndex;
    this.activeIndex = index;
    setTimeout(() => { this.exitIndex = -1; }, 700);
    this.resetProgress();
    this.timer = setInterval(() => this.advance(), this.intervalMs);
  }

  resetProgress(): void {
    this.isRunning = false;
    if (this.progressTimer) clearTimeout(this.progressTimer);
    this.progressTimer = setTimeout(() => { this.isRunning = true; }, 30);
  }

  clearTimers(): void {
    if (this.timer) clearInterval(this.timer);
    if (this.progressTimer) clearTimeout(this.progressTimer);
  }

  getStars(rating: number): number[] {
    return Array(rating).fill(0);
  }

  getEmptyStars(rating: number): number[] {
    return Array(5 - rating).fill(0);
  }
}