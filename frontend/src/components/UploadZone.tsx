import { useMemo, useState } from 'react';

interface UploadZoneProps {
  onUpload: (files: File[]) => Promise<void>;
}

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
const MAX_FILE_SIZE = 10 * 1024 * 1024;

export function UploadZone({ onUpload }: UploadZoneProps) {
  const [dragging, setDragging] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [errors, setErrors] = useState<string[]>([]);

  const zoneClassName = useMemo(() => {
    return `drop-zone ${dragging ? 'drop-zone-highlight' : ''}`;
  }, [dragging]);

  const validateFiles = (files: File[]) => {
    const valid: File[] = [];
    const failed: string[] = [];

    files.forEach((file) => {
      if (!ALLOWED_TYPES.includes(file.type)) {
        failed.push(`${file.name}: unsupported file type`);
      } else if (file.size > MAX_FILE_SIZE) {
        failed.push(`${file.name}: exceeds 10MB size limit`);
      } else {
        valid.push(file);
      }
    });

    return { valid, failed };
  };

  const runUpload = async (fileList: FileList | null) => {
    if (!fileList || fileList.length === 0) {
      return;
    }

    const files = Array.from(fileList);
    const { valid, failed } = validateFiles(files);
    setErrors(failed);

    if (!valid.length) {
      return;
    }

    setUploading(true);
    try {
      await onUpload(valid);
    } finally {
      setUploading(false);
    }
  };

  return (
    <section className="card mb-4">
      <div className="card-body">
        <h5 className="card-title">Upload Photos</h5>

        <label
          className={zoneClassName}
          onDragEnter={() => setDragging(true)}
          onDragLeave={() => setDragging(false)}
          onDragOver={(event) => {
            event.preventDefault();
            setDragging(true);
          }}
          onDrop={(event) => {
            event.preventDefault();
            setDragging(false);
            void runUpload(event.dataTransfer.files);
          }}
        >
          <input
            type="file"
            className="d-none"
            multiple
            accept={ALLOWED_TYPES.join(',')}
            onChange={(event) => {
              void runUpload(event.target.files);
              event.currentTarget.value = '';
            }}
          />
          <p className="mb-2"><strong>Drag and drop photos here</strong></p>
          <p className="text-muted mb-0">or click to select files</p>
        </label>

        {uploading && <div className="alert alert-info mt-3 mb-0">Uploading photos...</div>}
        {errors.length > 0 && (
          <div className="alert alert-danger mt-3 mb-0">
            <strong>Upload errors:</strong>
            <ul className="mb-0">
              {errors.map((error) => (
                <li key={error}>{error}</li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </section>
  );
}
