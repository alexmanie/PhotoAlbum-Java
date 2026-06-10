export interface Photo {
  id: string;
  originalFileName: string;
  filePath: string | null;
  uploadedAt: string;
  fileSize: number;
  width: number | null;
  height: number | null;
  mimeType: string;
  description: string | null;
}

export interface PhotosResponse {
  photos: Photo[];
  aiEnabled: boolean;
}

export interface PhotoDetailResponse {
  photo: Photo;
  previousPhotoId: string | null;
  nextPhotoId: string | null;
  aiEnabled: boolean;
}
