import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ServicesService } from '../services/services.service';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent implements OnInit {
  currentYear: number = new Date().getFullYear();
  categories: any[] = [];

  // Comment form state
  commentData = { name: '', email: '', comment: '' };
  commentSuccess = false;
  commentError = '';

  constructor(private services: ServicesService) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadUserInfo();
  }

  loadUserInfo(): void {
    const token = localStorage.getItem('token');
    if (token) {
      // Try to get user info from localStorage or decode token
      const userStr = localStorage.getItem('user');
      if (userStr) {
        try {
          const user = JSON.parse(userStr);
          this.commentData.name = user.name || user.username || '';
          this.commentData.email = user.email || '';
        } catch (e) {
          console.error('Error parsing user info:', e);
        }
      }
    }
  }

  loadCategories(): void {
    this.services.getAllCategories().subscribe({
      next: (data: any[]) => {
        this.categories = data;
      },
      error: (err) => {
        console.error('Error loading categories:', err);
      }
    });
  }

  submitComment(): void {
    this.commentSuccess = false;
    this.commentError = '';

    if (!this.commentData.comment) {
      this.commentError = 'Please enter your comment.';
      return;
    }

    // Ensure name and email are populated from user info
    if (!this.commentData.name || !this.commentData.email) {
      this.loadUserInfo();
      if (!this.commentData.name || !this.commentData.email) {
        this.commentError = 'Please log in to submit a comment.';
        return;
      }
    }

    // Send comment to backend
    this.services.submitComment(this.commentData).subscribe({
      next: (response) => {
        this.commentSuccess = true;
        this.commentData.comment = ''; // Only clear the comment, keep name and email

        // Hide message after 3 seconds
        setTimeout(() => {
          this.commentSuccess = false;
        }, 3000);
      },
      error: (err) => {
        console.error('Error submitting comment:', err);
        this.commentError = 'Failed to submit comment. Please try again.';
      }
    });
  }
}
