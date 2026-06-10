// Thin API client wrapping the Spring Boot REST endpoints.
import type {
  AppConfig,
  DescriptionStatus,
  Photo,
  UploadResponse,
} from '../types';

async function handleJson<T>(res: Response): Promise<T> {
  if (!res.ok) {
    throw new Error(`Request failed with status ${res.status}`);
  }
  return (await res.json()) as T;
}

export async function fetchConfig(): Promise<AppConfig> {
  return handleJson<AppConfig>(await fetch('/api/config'));
}

export async function fetchPhotos(): Promise<Photo[]> {
  return handleJson<Photo[]>(await fetch('/api/photos'));
}

export async function fetchPhoto(id: string): Promise<Photo> {
  return handleJson<Photo>(await fetch(`/api/photos/${id}`));
}

export async function uploadPhotos(files: File[]): Promise<UploadResponse> {
  const formData = new FormData();
  files.forEach((file) => formData.append('files', file));
  const res = await fetch('/api/photos/upload', {
    method: 'POST',
    body: formData,
  });
  return handleJson<UploadResponse>(res);
}

export async function deletePhoto(id: string): Promise<void> {
  const res = await fetch(`/api/photos/${id}`, { method: 'DELETE' });
  if (!res.ok) {
    throw new Error(`Delete failed with status ${res.status}`);
  }
}

export async function fetchDescription(id: string): Promise<DescriptionStatus> {
  return handleJson<DescriptionStatus>(
    await fetch(`/api/photos/${id}/description`),
  );
}

/** URL for the binary photo content served by the backend. */
export function photoUrl(id: string, bust = false): string {
  return bust ? `/photo/${id}?_t=${Date.now()}` : `/photo/${id}`;
}
