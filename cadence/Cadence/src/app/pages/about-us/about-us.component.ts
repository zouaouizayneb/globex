import { Component } from '@angular/core';
import { AfterViewInit, ElementRef, QueryList, ViewChildren } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-about-us',
  imports: [CommonModule,RouterModule],
  templateUrl: './about-us.component.html',
  styleUrl: './about-us.component.css'
})
export class AboutUsComponent {
  @ViewChildren('countEl') countElements!: QueryList<ElementRef>;

  counters = [
    { target: 310, start: 0, text: 'With 310+ dedicated workers, our team delivers top-notch results with unmatched dedication and expertise.' },
    { target: 23, start: 0, text: 'Backed by 23+ experienced employees, we ensure every project is handled with precision and care.' },
    { target: 99, start: 0, text: 'With a fleet of 99+ advanced machineries, we execute tasks efficiently and effectively,meeting the highest standards of quality' }
  ];

  ngAfterViewInit(): void {
    const observer = new IntersectionObserver(entries => {
      entries.forEach((entry, i) => {
        if (entry.isIntersecting) {
          this.animateCount(i, this.counters[i].target);
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.5 });

    this.countElements.forEach(el => observer.observe(el.nativeElement));
  }

  animateCount(index: number, target: number): void {
    const duration = 2000;
    const steps = 60;
    let current = 0;
    const increment = target / steps;
    const interval = duration / steps;

    const counter = setInterval(() => {
      current += increment;
      if (current >= target) {
        current = target;
        clearInterval(counter);
      }
      this.counters[index].start = Math.floor(current);
    }, interval);
  }
}
