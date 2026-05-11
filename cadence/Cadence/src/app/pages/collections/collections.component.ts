import { Component, OnInit } from '@angular/core';
import { ServicesService } from '../../services/services.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-collections',
  imports: [CommonModule, RouterModule],
  templateUrl: './collections.component.html',
  styleUrl: './collections.component.css',
  standalone: true
})
export class CollectionsComponent implements OnInit {

  categories: any[] = [];
  loading: boolean = true;
  error: string = '';

  constructor(public services: ServicesService) {}

  ngOnInit(): void {
    this.getCategories();
  }

  getCategories(): void {
    this.loading = true;
    this.services.getAllCategories().subscribe({
      next: (data: any[]) => {
        console.log(' Categories:', data);
        const imageMap: { [key: number]: string } = {
          9: 'https://i.imgur.com/hybotyb_d.jpeg?maxwidth=520&shape=thumb&fidelity=high',
          5: 'https://i.imgur.com/Mopl9DD_d.jpeg?maxwidth=520&shape=thumb&fidelity=high',
          4: 'https://i.imgur.com/5lqvCZt.jpeg',
          8: 'https://i.imgur.com/otXElq0.jpeg',
          2: 'https://i.imgur.com/Rf1IdQB.jpeg',
          1: 'https://i.imgur.com/8jcPbrF.jpeg'
        };

        this.categories = data.map(cat => {
          const id = cat.idCategory || cat.id_category || cat.id;
          return {
            id:    id,
            label: cat.name       || cat.label       || cat.categoryName,
            image: imageMap[id] || cat.image || cat.imageUrl || cat.image_url || 'assets/images/no-category.jpg'
          };
        });
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading categories:', err);
        this.error = 'Unable to load categories.';
        this.loading = false;
      }
    });
  }

  onImageError(event: any): void {
    event.target.src = 'assets/images/no-category.jpg';
  }
}