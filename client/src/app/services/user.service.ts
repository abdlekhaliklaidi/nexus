import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { ProfileResponse } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private userSubject = new BehaviorSubject<ProfileResponse | null>(null);
  public user$: Observable<ProfileResponse | null> = this.userSubject.asObservable();

  public get currentUser(): ProfileResponse | null {
    return this.userSubject.value;
  }

  public setUser(userData: Partial<ProfileResponse>): void {
    const current = this.userSubject.value || ({} as ProfileResponse);
    
    // Normalize properties
    const rawAvatar = userData.avatarUrl || (userData as any)?.avatar || current.avatarUrl || '';
    const formattedAvatar = this.formatAvatarUrl(rawAvatar);

    const updated: ProfileResponse = {
      ...current,
      ...userData,
      name: userData.name || (userData as any)?.username || current.name || 'Curator',
      avatarUrl: formattedAvatar
    };

    this.userSubject.next(updated);
  }

  public clearUser(): void {
    this.userSubject.next(null);
  }

  private formatAvatarUrl(url: string): string {
    if (!url) return '';
    if (url.startsWith('http') || url.startsWith('data:')) return url;
    return `https://localhost:8443${url.startsWith('/') ? '' : '/'}${url}`;
  }
}