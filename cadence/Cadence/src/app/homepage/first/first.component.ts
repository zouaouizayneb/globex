import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-first',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './first.component.html',
  styleUrls: ['./first.component.css']
})
export class FirstComponent implements OnInit {

  loaded = false;

  categories = [
    { label: 'Home Décor'  },
    { label: 'Clothing'    },
    { label: 'Sports'      },
    { label: 'School'      },
    { label: 'Worldwide'   },
  ];

  stats = [
    { value: '150+', label: 'Countries' },
    { value: '10k+', label: 'Products'  },
    { value: '4.9★', label: 'Rating'    },
  ];

  ngOnInit(): void {
    requestAnimationFrame(() => {
      setTimeout(() => { this.loaded = true; }, 80);
    });
  }
}