import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ServicesService } from '../../../services/services.service';

interface Comment {
  idComment?: number;
  name: string;
  email: string;
  comment: string;
  createdAt?: Date;
  status?: 'NEW' | 'READ' | 'ARCHIVED';
}

@Component({
  selector: 'app-comments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './comments.component.html',
  styleUrls: ['./comments.component.css']
})
export class CommentsComponent implements OnInit {
  comments: Comment[] = [];
  filteredComments: Comment[] = [];
  searchQuery: string = '';
  filterStatus: string = 'ALL';
  selectedComment: Comment | null = null;
  showDetail: boolean = false;

  constructor(private services: ServicesService) {}

  ngOnInit(): void {
    this.loadComments();
  }

  loadComments(): void {
    this.services.getComments().subscribe({
      next: (data: any[]) => {
        this.comments = data.map(comment => ({
          idComment: comment.idComment,
          name: comment.name,
          email: comment.email,
          comment: comment.comment,
          createdAt: new Date(comment.createdAt),
          status: comment.status
        }));
        this.applyFilters();
      },
      error: (err) => {
        console.error('Error loading comments:', err);
      }
    });
  }

  applyFilters(): void {
    this.filteredComments = this.comments.filter(comment => {
      const matchesSearch =
        comment.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        comment.email.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        comment.comment.toLowerCase().includes(this.searchQuery.toLowerCase());

      const matchesStatus =
        this.filterStatus === 'ALL' || comment.status === this.filterStatus;

      return matchesSearch && matchesStatus;
    });
  }

  onSearch(): void {
    this.applyFilters();
  }

  onFilterChange(): void {
    this.applyFilters();
  }

  viewComment(comment: Comment): void {
    this.selectedComment = comment;
    this.showDetail = true;
    // Mark as read
    if (comment.status === 'NEW') {
      comment.status = 'READ';
      if (comment.idComment) {
        this.services.updateComment(comment.idComment, comment).subscribe({
          error: (err) => console.error('Error updating comment:', err)
        });
      }
    }
  }

  closeDetail(): void {
    this.showDetail = false;
    this.selectedComment = null;
  }

  markAsArchived(comment: Comment, event: Event): void {
    event.stopPropagation();
    comment.status = 'ARCHIVED';
    this.applyFilters();
    if (comment.idComment) {
      this.services.updateComment(comment.idComment, comment).subscribe({
        error: (err) => console.error('Error updating comment:', err)
      });
    }
  }

  deleteComment(comment: Comment, event: Event): void {
    event.stopPropagation();
    if (comment.idComment) {
      this.services.deleteComment(comment.idComment).subscribe({
        next: () => {
          const index = this.comments.indexOf(comment);
          if (index > -1) {
            this.comments.splice(index, 1);
            this.applyFilters();
          }
        },
        error: (err) => console.error('Error deleting comment:', err)
      });
    }
  }


  getStatusColor(status?: string): string {
    switch (status) {
      case 'NEW':
        return '#4d9e78';
      case 'READ':
        return '#666';
      case 'ARCHIVED':
        return '#999';
      default:
        return '#666';
    }
  }

  getStatusLabel(status?: string): string {
    switch (status) {
      case 'NEW':
        return 'New';
      case 'READ':
        return 'Read';
      case 'ARCHIVED':
        return 'Archived';
      default:
        return 'New';
    }
  }

  get newCommentsCount(): number {
    return this.comments.filter(c => c.status === 'NEW').length;
  }
}
