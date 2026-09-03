import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
@Injectable({
  providedIn: 'root'
})
export class MediaService {

  private http = inject(HttpClient);
  uploadPublicAvatar(file: File): Observable<{ avatarUrl: string }> {
    const formData = new FormData();
    formData.append('avatar', file);

    return this.http.post<{ avatarUrl: string }>(
      `${environment.apiUrl}/api/media/avatars/public`,
      formData
    );
  }
  uploadImages(files: File[]) {

    const formData = new FormData();

    files.forEach(file => {

      formData.append('images', file);

    });

    return this.http.post<string[]>(
      `${environment.apiUrl}/api/media/images`,
      formData
    );

  }
  deleteImage(fileName: string): Observable<void> {

    return this.http.delete<void>(
      `${environment.apiUrl}/api/media/images/${fileName}`
    );

  }
  getImageUrl(fileName: string): string {

    return `${environment.apiUrl}/api/media/images/${fileName}`;

  }

}