import { useRef, useState } from 'react';
import { uploadPhotos } from '../api/photos';
import type { Photo } from '../types';

interface UploadZoneProps {
  onUploaded: (photos: Photo[]) => void;
}

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
const MAX_SIZE = 10 * 1024 * 1024; // 10MB

/** Drag-and-drop / click upload zone with client-side validation. */
export default function UploadZone({ onUploaded }: UploadZoneProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [highlight, setHighlight] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [errors, setErrors] = useState<string[]>([]);

  const validate = (files: FileList | File[]): File[] => {
    const valid: File[] = [];
    const errs: string[] = [];
    Array.from(files).forEach((file) => {
      if (!ALLOWED_TYPES.includes(file.type)) {
        errs.push(
          `${file.name}: File type not supported. Please upload JPEG, PNG, GIF, or WebP images.`,
        );
      } else if (file.size > MAX_SIZE) {
        errs.push(`${file.name}: File size exceeds 10MB limit.`);
      } else {
        valid.push(file);
      }
    });
    if (errs.length > 0) setErrors(errs);
    return valid;
  };

  const handleFiles = async (incoming: FileList | File[]) => {
    setErrors([]);
    setSuccessMsg(null);
    const valid = validate(incoming);
    if (valid.length === 0) return;

    setUploading(true);
    try {
      const result = await uploadPhotos(valid);
      if (result.uploadedPhotos.length > 0) {
        setSuccessMsg(
          `Successfully uploaded ${result.uploadedPhotos.length} photo(s)!`,
        );
        onUploaded(result.uploadedPhotos);
        window.setTimeout(() => setSuccessMsg(null), 5000);
      }
      if (result.failedUploads.length > 0) {
        setErrors(
          result.failedUploads.map((f) => `${f.fileName}: ${f.error}`),
        );
      }
    } catch {
      setErrors(['An error occurred during upload. Please try again.']);
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  return (
    <div className="card mb-4">
      <div className="card-body">
        <h5 className="card-title">Upload Photos</h5>

        <div
          className={`drop-zone mb-3${highlight ? ' drop-zone-highlight' : ''}`}
          onClick={() => fileInputRef.current?.click()}
          onDragEnter={(e) => {
            e.preventDefault();
            setHighlight(true);
          }}
          onDragOver={(e) => {
            e.preventDefault();
            setHighlight(true);
          }}
          onDragLeave={(e) => {
            e.preventDefault();
            setHighlight(false);
          }}
          onDrop={(e) => {
            e.preventDefault();
            setHighlight(false);
            void handleFiles(e.dataTransfer.files);
          }}
        >
          <div className="drop-zone-content">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="48"
              height="48"
              fill="currentColor"
              className="bi bi-cloud-upload mb-3"
              viewBox="0 0 16 16"
            >
              <path
                fillRule="evenodd"
                d="M4.406 1.342A5.53 5.53 0 0 1 8 0c2.69 0 4.923 2 5.166 4.579C14.758 4.804 16 6.137 16 7.773 16 9.569 14.502 11 12.687 11H10a.5.5 0 0 1 0-1h2.688C13.979 10 15 8.988 15 7.773c0-1.216-1.02-2.228-2.313-2.228h-.5v-.5C12.188 2.825 10.328 1 8 1a4.53 4.53 0 0 0-2.941 1.1c-.757.652-1.153 1.438-1.153 2.055v.448l-.445.049C2.064 4.805 1 5.952 1 7.318 1 8.785 2.23 10 3.781 10H6a.5.5 0 0 1 0 1H3.781C1.708 11 0 9.366 0 7.318c0-1.763 1.266-3.223 2.942-3.593.143-.863.698-1.723 1.464-2.383z"
              />
              <path
                fillRule="evenodd"
                d="M7.646 4.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1-.708.708L8.5 5.707V14.5a.5.5 0 0 1-1 0V5.707L5.354 7.854a.5.5 0 1 1-.708-.708l3-3z"
              />
            </svg>
            <p className="mb-2">
              <strong>Drag and drop photos here</strong>
            </p>
            <p className="text-muted small">or click to select files</p>
            <p className="text-muted small">
              Supports: JPEG, PNG, GIF, WebP (max 10MB each)
            </p>
          </div>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/jpeg,image/png,image/gif,image/webp"
            multiple
            hidden
            onChange={(e) => {
              if (e.target.files) void handleFiles(e.target.files);
            }}
          />
        </div>

        {uploading && (
          <div className="alert alert-info">
            <div
              className="spinner-border spinner-border-sm me-2"
              role="status"
            >
              <span className="visually-hidden">Uploading...</span>
            </div>
            Uploading photos...
          </div>
        )}

        {successMsg && (
          <div className="alert alert-success">{successMsg}</div>
        )}

        {errors.length > 0 && (
          <div className="alert alert-danger">
            <strong>Upload errors:</strong>
            <ul className="mb-0 mt-2">
              {errors.map((err, i) => (
                <li key={i}>{err}</li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  );
}
