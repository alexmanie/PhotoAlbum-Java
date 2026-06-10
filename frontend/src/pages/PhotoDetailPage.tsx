import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { deletePhoto, fetchPhoto, photoUrl } from '../api/photos';
import type { Photo } from '../types';
import { formatDateLong, formatFileSize } from '../utils/format';
import { usePhotoDescription } from '../hooks/usePhotoDescription';

interface PhotoDetailPageProps {
  aiEnabled: boolean;
}

/** Full-size photo view with metadata, prev/next navigation and delete. */
export default function PhotoDetailPage({ aiEnabled }: PhotoDetailPageProps) {
  const { id = '' } = useParams();
  const navigate = useNavigate();
  const [photo, setPhoto] = useState<Photo | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setNotFound(false);
    fetchPhoto(id)
      .then((data) => {
        if (!cancelled) setPhoto(data);
      })
      .catch(() => {
        if (!cancelled) setNotFound(true);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  const { description, pending } = usePhotoDescription(
    id,
    aiEnabled && !!photo,
    photo?.description ?? null,
  );

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this photo?')) return;
    try {
      await deletePhoto(id);
      navigate('/');
    } catch {
      window.alert('Failed to delete photo. Please try again.');
    }
  };

  if (loading) {
    return (
      <div className="text-center text-muted py-5">
        <div className="spinner-border" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  if (notFound || !photo) {
    return (
      <div className="alert alert-warning">
        <h4>Photo not found</h4>
        <p>The photo you&apos;re looking for doesn&apos;t exist or has been deleted.</p>
        <Link to="/" className="btn btn-primary">
          Back to Gallery
        </Link>
      </div>
    );
  }

  const uploaded = formatDateLong(photo.uploadedAt);

  return (
    <div className="photo-detail-container">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <Link to="/" className="btn btn-outline-secondary">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
            fill="currentColor"
            className="bi bi-arrow-left"
            viewBox="0 0 16 16"
          >
            <path
              fillRule="evenodd"
              d="M15 8a.5.5 0 0 0-.5-.5H2.707l3.147-3.146a.5.5 0 1 0-.708-.708l-4 4a.5.5 0 0 0 0 .708l4 4a.5.5 0 0 0 .708-.708L2.707 8.5H14.5A.5.5 0 0 0 15 8z"
            />
          </svg>{' '}
          Back to Gallery
        </Link>

        <button type="button" className="btn btn-danger" onClick={handleDelete}>
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
            fill="currentColor"
            className="bi bi-trash"
            viewBox="0 0 16 16"
          >
            <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z" />
            <path
              fillRule="evenodd"
              d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z"
            />
          </svg>{' '}
          Delete
        </button>
      </div>

      <div className="row">
        <div className="col-lg-8 mb-4">
          <div className="card">
            <div className="card-body p-0">
              <img
                src={photoUrl(photo.id)}
                alt={photo.originalFileName}
                className="img-fluid w-100 photo-detail-image"
                style={{
                  maxHeight: '80vh',
                  objectFit: 'contain',
                  backgroundColor: '#f8f9fa',
                }}
              />
            </div>
          </div>

          <div className="d-flex justify-content-between mt-3">
            <div>
              {photo.previousPhotoId && (
                <Link
                  to={`/detail/${photo.previousPhotoId}`}
                  className="btn btn-outline-primary"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    fill="currentColor"
                    className="bi bi-chevron-left"
                    viewBox="0 0 16 16"
                  >
                    <path
                      fillRule="evenodd"
                      d="M11.354 1.646a.5.5 0 0 1 0 .708L5.707 8l5.647 5.646a.5.5 0 0 1-.708.708l-6-6a.5.5 0 0 1 0-.708l6-6a.5.5 0 0 1 .708 0z"
                    />
                  </svg>{' '}
                  Previous Photo
                </Link>
              )}
            </div>
            <div>
              {photo.nextPhotoId && (
                <Link
                  to={`/detail/${photo.nextPhotoId}`}
                  className="btn btn-outline-primary"
                >
                  Next Photo{' '}
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    fill="currentColor"
                    className="bi bi-chevron-right"
                    viewBox="0 0 16 16"
                  >
                    <path
                      fillRule="evenodd"
                      d="M4.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L10.293 8 4.646 2.354a.5.5 0 0 1 0-.708z"
                    />
                  </svg>
                </Link>
              )}
            </div>
          </div>
        </div>

        <div className="col-lg-4">
          <div className="card">
            <div className="card-header">
              <h5 className="mb-0">Photo Information</h5>
            </div>
            <div className="card-body">
              <dl className="row mb-0">
                <dt className="col-sm-5">Filename:</dt>
                <dd className="col-sm-7 text-break">
                  {photo.originalFileName}
                </dd>

                <dt className="col-sm-5">Uploaded:</dt>
                <dd className="col-sm-7">
                  {uploaded.day}
                  <br />
                  <small className="text-muted">{uploaded.time}</small>
                </dd>

                <dt className="col-sm-5">File Size:</dt>
                <dd className="col-sm-7">{formatFileSize(photo.fileSize)}</dd>

                {photo.width && photo.height && (
                  <>
                    <dt className="col-sm-5">Dimensions:</dt>
                    <dd className="col-sm-7">
                      {photo.width} x {photo.height} px
                    </dd>
                  </>
                )}

                <dt className="col-sm-5">Type:</dt>
                <dd className="col-sm-7">
                  <span className="badge bg-secondary">{photo.mimeType}</span>
                </dd>

                {aiEnabled && (
                  <>
                    <dt className="col-sm-5 mt-2">Description:</dt>
                    <dd className="col-sm-7 mt-2">
                      {pending ? (
                        <span className="text-muted fst-italic">
                          <span
                            className="spinner-border spinner-border-sm me-1"
                            style={{ width: '.65em', height: '.65em' }}
                            role="status"
                          />
                          Generating&hellip;
                        </span>
                      ) : (
                        description || (
                          <span className="text-muted fst-italic">
                            Description not available.
                          </span>
                        )
                      )}
                    </dd>
                  </>
                )}
              </dl>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
