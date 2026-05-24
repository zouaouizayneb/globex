import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-supplier-slider',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './promotional-banner.component.html',
  styleUrl: './promotional-banner.component.css'
})
export class SupplierSliderComponent {

  suppliers = [
    { name: 'Global Textiles', style: 'serif' },
    { name: 'Leather Pro', style: 'handwritten' },
    { name: 'Electro Supply', style: 'clean' },
    { name: 'Home Decor', style: 'elegant' },
    { name: 'Sport Fit', style: 'bold' },
    { name: 'Beauty Store', style: 'handwritten' },
    { name: 'Office Hub', style: 'serif' },
    { name: 'Audio Tech', style: 'clean' },
    { name: 'Fashion World', style: 'elegant' },
    { name: 'Garden Living', style: 'bold' }
  ];

}