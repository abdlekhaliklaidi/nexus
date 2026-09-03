import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { AuthService } from '../app/services/auth.service';
import { UserService } from '../app/services/user.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const userService = inject(UserService);
  const router = inject(Router);
  
  // 1. Unauthenticated check
  if (!authService.isLoggedIn()) {
    console.warn('Access Denied: Unauthenticated user routed to login.');
    authService.removeToken();
    router.navigate(['/login']);
    return false;
  }

  const requiresSeller = state.url.includes('/products/add');
  if (!requiresSeller) {
    return true;
  }

  // Helper function to validate role
  const checkSellerRole = (role?: string): boolean => {
    const isSeller = role === 'SELLER';
    if (!isSeller) {
      console.warn('Access Denied: User lacks SELLER permissions.');
      router.navigate(['/forbidden']);
    }
    return isSeller;
  };

  const cachedUser = userService.currentUser;
  if (cachedUser?.role) {
    return checkSellerRole(cachedUser.role);
  }

  // If profile is not cached, fetch from backend and check role
  return authService.getProfile().pipe(
    map((profile) => checkSellerRole(profile?.role)),
    catchError(() => {
      router.navigate(['/forbidden']);
      return of(false);
    })
  );
};