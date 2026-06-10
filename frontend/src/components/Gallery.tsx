import { Link } from 'react-router-dom';
import type { Photo } from '../types';

interface GalleryProps {
  photos: Photo[];
  aiEnabled: boolean;
}

function formatDate(raw: string) {
  return new Date(raw).toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
    hour12: true
  });
}

export function Gallery({ photos, aiEnabled }: GalleryProps) {
  if (!photos.length) {
    return <div className="alert alert-info text-center">No photos yet. Upload your first photo to get started.</div>;
  }

  return (
    <div className="row" id="photo-gallery">
      {photos.map((photo) => (
        <div key={photo.id} className="col-12 col-sm-6 col-md-4 col-lg-3 mb-4">
          <div className="card photo-card h-100">
            <Link to={`/detail/${photo.id}`} className="photo-link">
              <img src={`/photo/${photo.id}`} className="card-img-top" alt={photo.originalFileName} loading="lazy" />
            </Link>
            <div className="card-body">
              <p className="card-text text-truncate" title={photo.originalFileName}>
                <small>
                  <Link className="text-decoration-none text-dark" to={`/detail/${photo.id}`}>
                    {photo.originalFileName}
                  </Link>
                </small>
              </p>
              <p className="card-text mb-1">
                <small className="text-muted">{formatDate(photo.uploadedAt)}</small>
              </p>
              <p className="card-text mb-0">
                <small className="text-muted">
                  {Math.round(photo.fileSize / 1024)} KB
                  {photo.width && photo.height ? ` • ${photo.width} x ${photo.height}` : ''}
                </small>
              </p>
              {aiEnabled && (
                <p className="card-text photo-description mt-2 mb-0">
                  <small className="text-muted fst-italic">
                    {photo.description?.trim() ? photo.description : 'Generating description...'}
                  </small>
                </p>
              )}
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
