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
        console.log('✅ Categories:', data);
        const imageMap: { [key: number]: string } = {
          4: 'https://images.unsplash.com/photo-1471193945509-9ad0617afabf?w=800&h=600&fit=crop',
          3: 'https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?w=800&h=600&fit=crop',
          2: 'https://images.unsplash.com/photo-1445205170230-053b83016050',
          1: 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&h=600&fit=crop'
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
        console.error('❌ Error loading categories:', err);
        this.error = 'Unable to load categories.';
        this.loading = false;
      }
    });
  }

  onImageError(event: any): void {
    event.target.src = 'assets/images/no-category.jpg';
  }
}