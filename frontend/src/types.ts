// Shared types for the Photo Album front-end.

export interface Photo {
  id: string;
  originalFileName: string;
  filePath: string | null;
  fileSize: number;
  mimeType: string;
  uploadedAt: string;
  width: number | null;
  height: number | null;
  description: string | null;
  previousPhotoId?: string | null;
  nextPhotoId?: string | null;
}

export interface FailedUpload {
  fileName: string;
  error: string;
}

export interface UploadResponse {
  success: boolean;
  uploadedPhotos: Photo[];
  failedUploads: FailedUpload[];
  aiEnabled: boolean;
  error?: string;
}

export interface AppConfig {
  aiEnabled: boolean;
}

export interface DescriptionStatus {
  description: string | null;
  ready: boolean;
}
