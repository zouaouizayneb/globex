import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { ServicesService } from '../../services/services.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.scss']
})
export class HomePageComponent implements OnInit, OnDestroy {
  
  // Données
  products: any[] = [];
  loading: boolean = true;
  error: string = '';
  
  // Subscriptions pour éviter memory leaks
  private subscriptions: Subscription = new Subscription();

  constructor(
    private servicesService: ServicesService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  ngOnDestroy(): void {
    // Nettoyer les subscriptions
    this.subscriptions.unsubscribe();
  }

  /**
   * Charger les produits depuis la base de données
   */
  loadProducts(): void {
    this.loading = true;
    this.error = '';
    
    const sub = this.servicesService.getAllProducts().subscribe({
      next: (data) => {
        console.log('✅ Produits reçus de la DB:', data);
        this.products = this.mapProducts(data);
        this.loading = false;
      },
      error: (error) => {
        console.error('❌ Erreur chargement produits:', error);
        this.error = 'Impossible de charger les produits. Veuillez réessayer.';
        this.loading = false;
      }
    });
    
    this.subscriptions.add(sub);
  }

  /**
   * Mapper les données de la DB vers le format attendu par le template
   */
  mapProducts(dbProducts: any[]): any[] {
    return dbProducts.map(product => ({
      // ID du produit
      id: product.idProduct,
      
      // Nom du produit
      name: product.name,
      
      // Image principale (depuis product_images)
      image: this.getPrimaryImage(product.images),
      
      // Prix (prix minimum des variants)
      price: this.getMinPrice(product.variants),
      
      // Ancien prix (si discount)
      oldPrice: this.getOldPrice(product.variants),
      
      // Couleurs disponibles
      colors: this.getColors(product.variants),
      
      // Rating (par défaut 4.5 si pas dans la DB)
      rating: product.rating || 4.5,
      
      // Badge "New" si créé récemment
      isNew: this.isNewProduct(product.createdAt),
      
      // Pourcentage de réduction
      discount: this.calculateDiscount(product.variants),
      
      // Stock disponible (pour désactiver "Add to cart" si rupture)
      inStock: this.hasStock(product.variants)
    }));
  }

  /**
   * Obtenir l'image principale
   */
  getPrimaryImage(images: any[]): string {
    if (!images || images.length === 0) {
      return 'assets/images/no-product-image.jpg';
    }
    
    // Chercher l'image marquée comme principale
    const primaryImage = images.find(img => img.isPrimary === true);
    if (primaryImage) {
      return primaryImage.imageUrl;
    }
    
    // Sinon prendre la première image
    return images[0].imageUrl;
  }

  /**
   * Obtenir le prix minimum parmi les variants
   */
  getMinPrice(variants: any[]): number {
    if (!variants || variants.length === 0) {
      return 0;
    }
    
    const prices = variants
      .map(v => parseFloat(v.price))
      .filter(p => !isNaN(p) && p > 0);
    
    return prices.length > 0 ? Math.min(...prices) : 0;
  }

  /**
   * Obtenir l'ancien prix (pour afficher la réduction)
   */
  getOldPrice(variants: any[]): number | null {
    if (!variants || variants.length === 0) {
      return null;
    }
    
    // Chercher un variant qui a un oldPrice
    const variantWithOldPrice = variants.find(v => v.oldPrice && v.oldPrice > v.price);
    
    return variantWithOldPrice ? variantWithOldPrice.oldPrice : null;
  }

  /**
   * Extraire les couleurs uniques des variants
   */
  getColors(variants: any[]): string[] {
    if (!variants || variants.length === 0) {
      return [];
    }
    
    // Obtenir les couleurs uniques et les convertir en hex
    const uniqueColors = [...new Set(variants.map(v => v.color))];
    return uniqueColors.map(color => this.colorNameToHex(color));
  }

  /**
   * Convertir nom de couleur en code hexadécimal
   */
  colorNameToHex(colorName: string): string {
    const colorMap: {[key: string]: string} = {
      'Black': '#000000',
      'White': '#FFFFFF',
      'Red': '#FF0000',
      'Blue': '#0000FF',
      'Navy': '#000080',
      'Green': '#00FF00',
      'Yellow': '#FFFF00',
      'Orange': '#FFA500',
      'Pink': '#FFC0CB',
      'Purple': '#800080',
      'Gray': '#808080',
      'Grey': '#808080',
      'Brown': '#8B4513',
      'Beige': '#F5F5DC',
      'Cream': '#FFFDD0',
      'Gold': '#FFD700',
      'Silver': '#C0C0C0',
      'Khaki': '#C3B091'
    };
    
    return colorMap[colorName] || '#CCCCCC';
  }

  /**
   * Vérifier si le produit est nouveau (moins de 30 jours)
   */
  isNewProduct(createdAt: string | Date): boolean {
    if (!createdAt) {
      return false;
    }
    
    const productDate = new Date(createdAt);
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
    
    return productDate > thirtyDaysAgo;
  }

  /**
   * Calculer le pourcentage de réduction
   */
  calculateDiscount(variants: any[]): number | null {
    if (!variants || variants.length === 0) {
      return null;
    }
    
    const variantWithDiscount = variants.find(v => 
      v.oldPrice && v.oldPrice > v.price
    );
    
    if (variantWithDiscount) {
      const discount = ((variantWithDiscount.oldPrice - variantWithDiscount.price) 
        / variantWithDiscount.oldPrice) * 100;
      return Math.round(discount);
    }
    
    return null;
  }

  /**
   * Vérifier si le produit a du stock
   */
  hasStock(variants: any[]): boolean {
    if (!variants || variants.length === 0) {
      return false;
    }
    
    return variants.some(v => v.stockQuantity > 0);
  }

  /**
   * Générer les étoiles pour le rating
   */
  getStars(rating: number): string {
    const fullStars = Math.floor(rating);
    const halfStar = (rating % 1) >= 0.5 ? 1 : 0;
    const emptyStars = 5 - fullStars - halfStar;
    
    return '★'.repeat(fullStars) + 
           (halfStar ? '☆' : '') + 
           '☆'.repeat(emptyStars);
  }

  /**
   * Ajouter au panier
   */
  addToCart(product: any): void {
    console.log('Ajout au panier:', product);
    
    // TODO: Implémenter avec votre CartService
    // Exemple:
    // this.cartService.addToCart(product.id, 1);
    
    alert(`${product.name} ajouté au panier!`);
  }

  /**
   * Vérifier si le produit est dans le panier
   */
  isInCart(product: any): boolean {
    // TODO: Implémenter avec votre CartService
    // Exemple:
    // return this.cartService.isInCart(product.id);
    
    return false;
  }

  /**
   * Gestion erreur image
   */
  onImageError(event: any): void {
    event.target.src = 'assets/images/no-product-image.jpg';
  }

  /**
   * Réessayer de charger les produits
   */
  retry(): void {
    this.loadProducts();
  }
}