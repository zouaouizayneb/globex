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
    { name: 'Alisa Boutique', style: 'serif' },
    { name: 'The Backyard Studio', style: 'handwritten' },
    { name: 'Natural Beauty', style: 'clean' },
    { name: 'Jewelry', style: 'elegant' },
    { name: 'Hand Made Studio', style: 'bold' },
    { name: 'Heliphoto', style: 'handwritten' }
  ];

}