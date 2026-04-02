import { Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AsyncPipe, NgIf } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    NgIf,
    AsyncPipe,
    ButtonModule
  ],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.css'
})
export class ShellComponent implements OnInit {
  private readonly auth = inject(AuthService);

  readonly role$ = this.auth.role$;
  readonly email = this.auth.getEmailFromToken();
  sidebarOpen = true;

  ngOnInit(): void {
    this.auth.ensureProfile().subscribe();
  }

  logout(): void {
    this.auth.logout();
  }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  isFleetRole(role: string | null): boolean {
    return role === 'ROLE_FLEET_MANAGER' || role === 'ROLE_ADMIN';
  }

  isAdmin(role: string | null): boolean {
    return role === 'ROLE_ADMIN';
  }
}
