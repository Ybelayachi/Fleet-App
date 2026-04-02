/**
 * Example component showing how to use pagination with the Page interface
 * This is a template for implementing pagination in admin/user list components
 */

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';
import { Page, PaginationParams } from '../../../core/models/page.model';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-paginated-list-example',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="pagination-container">
      <!-- Data Table -->
      <table class="data-table" *ngIf="!loading">
        <thead>
          <tr>
            <th>Email</th>
            <th>First Name</th>
            <th>Last Name</th>
            <th>Role</th>
            <th>Active</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let item of currentPageData">
            <td>{{ item.email }}</td>
            <td>{{ item.firstName }}</td>
            <td>{{ item.lastName }}</td>
            <td>{{ item.role }}</td>
            <td>{{ item.active ? 'Yes' : 'No' }}</td>
          </tr>
        </tbody>
      </table>

      <!-- Loading Indicator -->
      <p *ngIf="loading" class="loading">Loading...</p>

      <!-- Empty State -->
      <p *ngIf="!loading && currentPageData.length === 0" class="empty">
        No data found
      </p>

      <!-- Pagination Controls -->
      <div class="pagination-controls" *ngIf="totalPages > 0">
        <button 
          [disabled]="isFirstPage()" 
          (click)="goToPreviousPage()">
          ← Previous
        </button>

        <div class="page-info">
          Page {{ currentPage + 1 }} of {{ totalPages }}
          ({{ totalElements }} total items)
        </div>

        <button 
          [disabled]="isLastPage()" 
          (click)="goToNextPage()">
          Next →
        </button>
      </div>

      <!-- Page Size Selector -->
      <div class="page-size-selector">
        <label for="pageSize">Items per page:</label>
        <select 
          id="pageSize" 
          [(ngModel)]="pageSize" 
          (change)="onPageSizeChange()">
          <option [value]="5">5</option>
          <option [value]="10">10</option>
          <option [value]="20">20</option>
          <option [value]="50">50</option>
        </select>
      </div>

      <!-- Sort Selector -->
      <div class="sort-selector">
        <label for="sort">Sort by:</label>
        <select 
          id="sort" 
          [(ngModel)]="sortField" 
          (change)="onSortChange()">
          <option value="">None</option>
          <option value="email,asc">Email (A-Z)</option>
          <option value="email,desc">Email (Z-A)</option>
          <option value="firstName,asc">First Name (A-Z)</option>
          <option value="firstName,desc">First Name (Z-A)</option>
        </select>
      </div>
    </div>
  `,
  styles: [`
    .pagination-container {
      padding: 20px;
    }

    .data-table {
      width: 100%;
      border-collapse: collapse;
      margin-bottom: 20px;
    }

    .data-table th,
    .data-table td {
      padding: 12px;
      text-align: left;
      border-bottom: 1px solid #ddd;
    }

    .data-table th {
      background-color: #f5f5f5;
      font-weight: bold;
    }

    .data-table tbody tr:hover {
      background-color: #f9f9f9;
    }

    .pagination-controls {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 20px;
      margin: 20px 0;
    }

    .pagination-controls button {
      padding: 8px 16px;
      background-color: #007bff;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }

    .pagination-controls button:disabled {
      background-color: #ccc;
      cursor: not-allowed;
    }

    .pagination-controls button:hover:not(:disabled) {
      background-color: #0056b3;
    }

    .page-info {
      font-weight: bold;
    }

    .page-size-selector,
    .sort-selector {
      margin: 20px 0;
    }

    .page-size-selector label,
    .sort-selector label {
      margin-right: 10px;
      font-weight: bold;
    }

    .page-size-selector select,
    .sort-selector select {
      padding: 8px;
      border: 1px solid #ddd;
      border-radius: 4px;
    }

    .loading,
    .empty {
      text-align: center;
      padding: 20px;
      color: #666;
    }
  `]
})
export class PaginatedListExampleComponent implements OnInit {
  // State variables
  currentPageData: User[] = [];
  currentPage: number = 0;
  pageSize: number = 10;
  totalPages: number = 0;
  totalElements: number = 0;
  loading: boolean = false;
  sortField: string = '';

  constructor(private readonly userService: UserService) {}

  ngOnInit(): void {
    this.loadData();
  }

  /**
   * Loads data from the service
   */
  private loadData(): void {
    this.loading = true;

    const params: PaginationParams = {
      page: this.currentPage,
      size: this.pageSize,
      ...(this.sortField && { sort: this.sortField })
    };

    this.userService.getUsers(params).subscribe({
      next: (response: Page<User>) => {
        this.currentPageData = response.content;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading data:', error);
        this.loading = false;
      }
    });
  }

  /**
   * Navigate to previous page
   */
  goToPreviousPage(): void {
    if (!this.isFirstPage()) {
      this.currentPage--;
      this.loadData();
    }
  }

  /**
   * Navigate to next page
   */
  goToNextPage(): void {
    if (!this.isLastPage()) {
      this.currentPage++;
      this.loadData();
    }
  }

  /**
   * Check if on first page
   */
  isFirstPage(): boolean {
    return this.currentPage === 0;
  }

  /**
   * Check if on last page
   */
  isLastPage(): boolean {
    return this.currentPage >= this.totalPages - 1;
  }

  /**
   * Handle page size change
   */
  onPageSizeChange(): void {
    this.currentPage = 0; // Reset to first page
    this.loadData();
  }

  /**
   * Handle sort change
   */
  onSortChange(): void {
    this.currentPage = 0; // Reset to first page
    this.loadData();
  }
}
