import { Link } from 'react-router-dom';
import type { Photo } from '../types';
import { photoUrl } from '../api/photos';
import { formatDate } from '../utils/format';
import { usePhotoDescription } from '../hooks/usePhotoDescription';

interface PhotoCardProps {
  photo: Photo;
  aiEnabled: boolean;
}

/** A single photo tile in the gallery grid. */
export default function PhotoCard({ photo, aiEnabled }: PhotoCardProps) {
  const { description, pending } = usePhotoDescription(
    photo.id,
    aiEnabled,
    photo.description,
  );

  const dimensions =
    photo.width && photo.height ? ` \u2022 ${photo.width} x ${photo.height}` : '';
  const truncated =
    description && description.length > 90
      ? `${description.substring(0, 87)}\u2026`
      : description;

  return (
    <div className="col-12 col-sm-6 col-md-4 col-lg-3 mb-4">
      <div className="card photo-card h-100">
        <Link to={`/detail/${photo.id}`} className="photo-link">
          <img
            src={photoUrl(photo.id)}
            className="card-img-top"
            alt={photo.originalFileName}
            loading="lazy"
          />
        </Link>
        <div className="card-body">
          <p className="card-text text-truncate" title={photo.originalFileName}>
            <small>
              <Link
                to={`/detail/${photo.id}`}
                className="text-decoration-none text-dark"
              >
                {photo.originalFileName}
              </Link>
            </small>
          </p>
          <p className="card-text">
            <small className="text-muted">{formatDate(photo.uploadedAt)}</small>
          </p>
          <p className="card-text">
            <small className="text-muted">
              {Math.round(photo.fileSize / 1024)} KB{dimensions}
            </small>
          </p>
          {aiEnabled && (
            <p className="card-text photo-description mt-1">
              {pending ? (
                <small className="text-muted fst-italic">
                  <span
                    className="spinner-border spinner-border-sm me-1"
                    style={{ width: '.65em', height: '.65em' }}
                    role="status"
                  />
                  Generating description&hellip;
                </small>
              ) : (
                <small className="text-muted fst-italic">
                  {truncated || 'Description not available.'}
                </small>
              )}
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
