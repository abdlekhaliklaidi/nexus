import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../app/services/auth.service';
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authserv = inject(AuthService)

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {

      if (error.status === 401) {
        
        authserv.removeToken();
        router.navigate(['/unauthorized']);
      } 
      else if (error.status === 403) {
        router.navigate(['/forbidden']);
      } 
      else if (error.status === 500) {
        router.navigate(['/server-error']);
      }

      return throwError(() => error);
    })
  );
};