import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-home-redirect',
  standalone: true,
  template: ''
})
export class HomeRedirectComponent implements OnInit {
  constructor(private readonly auth: AuthService, private readonly router: Router) {}

  ngOnInit(): void {
    this.auth.ensureProfile().subscribe((role) => {
      if (role === 'ROLE_ADMIN') {
        this.router.navigate(['/admin/users']);
      } else if (role === 'ROLE_FLEET_MANAGER') {
        this.router.navigate(['/fleet/dashboard']);
      } else {
        this.router.navigate(['/driver/vehicles']);
      }
    });
  }
}
