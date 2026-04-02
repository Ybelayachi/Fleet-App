/**
 * Represents a paginated response from the backend.
 * Generic type T represents the type of items in the content array.
 */
export interface Page<T> {
  /** Array of items for the current page */
  content: T[];

  /** Total number of elements across all pages */
  totalElements: number;

  /** Total number of pages */
  totalPages: number;

  /** Size of the current page (number of items requested) */
  size: number;

  /** Current page number (zero-indexed) */
  number: number;

  /** Number of items in the current page */
  numberOfElements: number;

  /** Whether this is the first page */
  first: boolean;

  /** Whether this is the last page */
  last: boolean;

  /** Whether the page is empty */
  empty: boolean;
}

/**
 * Pagination request parameters
 */
export interface PaginationParams {
  /** Page number (zero-indexed) */
  page?: number;

  /** Number of items per page */
  size?: number;

  /** Sort order (example: "email,asc" or "id,desc") */
  sort?: string;
}
