import type { PhotoDetailResponse, PhotosResponse } from './types';

export async function fetchPhotos(): Promise<PhotosResponse> {
  const response = await fetch('/api/photos');
  if (!response.ok) {
    throw new Error('Failed to fetch photos');
  }
  return response.json();
}

export async function fetchPhotoDetail(id: string): Promise<PhotoDetailResponse> {
  const response = await fetch(`/api/photos/${id}`);
  if (!response.ok) {
    throw new Error('Failed to fetch photo detail');
  }
  return response.json();
}

export async function uploadPhotos(files: File[]): Promise<any> {
  const formData = new FormData();
  files.forEach((file) => formData.append('files', file));

  const response = await fetch('/api/photos/upload', {
    method: 'POST',
    body: formData
  });

  if (!response.ok) {
    throw new Error('Failed to upload photos');
  }

  return response.json();
}

export async function deletePhoto(id: string): Promise<void> {
  const response = await fetch(`/api/photos/${id}`, { method: 'DELETE' });
  if (!response.ok) {
    throw new Error('Failed to delete photo');
  }
}

export async function fetchDescription(id: string): Promise<{ ready: boolean; description: string | null }> {
  const response = await fetch(`/api/photos/${id}/description`);
  if (!response.ok) {
    throw new Error('Failed to fetch description');
  }
  return response.json();
}
