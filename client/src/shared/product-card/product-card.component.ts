import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Product } from '../../app/models/product'; 

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-card.component.html'
})
export class ProductCardComponent {
  @Input() product!: Product;
  @Input() currentUserId: string | null = null;
  @Input() currentUserRole: string | null = null;

  @Output() edit = new EventEmitter<Product>();
  @Output() delete = new EventEmitter<string>();
  
  showDeleteConfirm = false;
  
  constructor(private router: Router) {}

  // Simple getter: returns true if product belongs to current user
  get isOwner(): boolean {
    return !!this.currentUserId && this.product?.userId === this.currentUserId;
  }

  get truncatedDescription(): string {
    if (!this.product?.description) return '';
    return this.product.description.length > 20 
      ? this.product.description.substring(0, 20) + '...' 
      : this.product.description;
  }

  get primaryImage(): string {
    if (this.product?.imageUrls && Array.isArray(this.product.imageUrls) && this.product.imageUrls.length > 0) {
      return this.product.imageUrls[0];
    }
    return 'noimage.png';
  }

  onImageError(event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    if (imgElement) {
      imgElement.src = './noimage.png';
    }
  }

  getProductId(): string {
    return this.product?.id || (this.product as any)?._id || '';
  }

  navigateToDetails(): void {
    const productId = this.getProductId();
    if (productId) {
      this.router.navigate(['/products', productId]);
    } else {
      console.error('Cannot navigate: Product ID is missing', this.product);
    }
  }

  onEdit(event: Event): void {
    event.stopPropagation();
    this.edit.emit(this.product);
  }

  openDeleteConfirm(event: Event): void {
    event.stopPropagation();
    this.showDeleteConfirm = true;
  }

  cancelDelete(event: Event): void {
    event.stopPropagation();
    this.showDeleteConfirm = false;
  }

  confirmDelete(event: Event): void {
    event.stopPropagation();
    const productId = this.getProductId();
    if (productId) {
      this.delete.emit(productId);
    }
    this.showDeleteConfirm = false;
  }
}