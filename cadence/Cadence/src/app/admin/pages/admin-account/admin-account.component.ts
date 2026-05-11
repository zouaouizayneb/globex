import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

interface AdminProfile {
  username: string;
  email: string;
  fullName: string;
  role: string;
  phone: string;
  createdAt: string;
}

@Component({
  selector: 'app-admin-account',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-account.component.html',
  styleUrls: ['./admin-account.component.css']
})
export class AdminAccountComponent implements OnInit {
  adminProfile: AdminProfile = {
    username: 'admin',
    email: 'admin@globex.com',
    fullName: 'Administrator',
    role: 'Super Admin',
    phone: '+216 XX XXX XXX',
    createdAt: '2024-01-01'
  };

  isEditing = false;
  isSaving = false;
  
  editForm: AdminProfile = {
    username: '',
    email: '',
    fullName: '',
    role: '',
    phone: '',
    createdAt: ''
  };

  showPasswordModal = false;
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.loadAdminProfile();
  }

  loadAdminProfile(): void {
    // In a real app, this would come from the backend
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        this.adminProfile = {
          username: user.username || 'admin',
          email: user.email || 'admin@globex.com',
          fullName: user.fullName || 'Administrator',
          role: user.role || 'Super Admin',
          phone: user.phone || '+216 XX XXX XXX',
          createdAt: user.createdAt || '2024-01-01'
        };
      } catch (e) {
        console.error('Error parsing user data:', e);
      }
    }
    this.resetForm();
  }

  resetForm(): void {
    this.editForm = { ...this.adminProfile };
  }

  enableEdit(): void {
    this.isEditing = true;
    this.resetForm();
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.resetForm();
  }

  saveProfile(): void {
    if (!this.editForm.fullName.trim() || !this.editForm.email.trim()) {
      alert('Please fill in all required fields');
      return;
    }

    this.isSaving = true;

    // Simulate API call
    setTimeout(() => {
      this.adminProfile = { ...this.editForm };
      
      // Update localStorage
      const userStr = localStorage.getItem('user');
      if (userStr) {
        const user = JSON.parse(userStr);
        user.fullName = this.adminProfile.fullName;
        user.email = this.adminProfile.email;
        user.phone = this.adminProfile.phone;
        localStorage.setItem('user', JSON.stringify(user));
      }

      this.isEditing = false;
      this.isSaving = false;
      alert('Profile updated successfully!');
    }, 500);
  }

  openPasswordModal(): void {
    this.showPasswordModal = true;
    this.currentPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
  }

  closePasswordModal(): void {
    this.showPasswordModal = false;
    this.currentPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
  }

  changePassword(): void {
    if (!this.currentPassword || !this.newPassword || !this.confirmPassword) {
      alert('Please fill in all password fields');
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      alert('New passwords do not match');
      return;
    }

    if (this.newPassword.length < 6) {
      alert('Password must be at least 6 characters long');
      return;
    }

    // Simulate password change
    alert('Password changed successfully!');
    this.closePasswordModal();
  }

  getInitials(name: string): string {
    return name.split(' ').map(n => n.charAt(0)).join('').toUpperCase().slice(0, 2);
  }
}
