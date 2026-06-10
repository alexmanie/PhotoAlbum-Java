import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { deletePhoto, fetchDescription, fetchPhotoDetail } from '../api';
import type { Photo } from '../types';

function formatFileSize(size: number) {
  if (size < 1024) return `${size} bytes`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(2)} KB`;
  return `${(size / (1024 * 1024)).toFixed(2)} MB`;
}

export function PhotoDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [photo, setPhoto] = useState<Photo | null>(null);
  const [previousPhotoId, setPreviousPhotoId] = useState<string | null>(null);
  const [nextPhotoId, setNextPhotoId] = useState<string | null>(null);
  const [aiEnabled, setAiEnabled] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) {
      setError('Photo id is required.');
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);

    void fetchPhotoDetail(id)
      .then((data) => {
        setPhoto(data.photo);
        setPreviousPhotoId(data.previousPhotoId);
        setNextPhotoId(data.nextPhotoId);
        setAiEnabled(data.aiEnabled);
      })
      .catch(() => setError('Could not load photo detail.'))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    if (!aiEnabled || !photo?.id || (photo.description && photo.description.trim())) {
      return;
    }

    const intervalId = window.setInterval(() => {
      void fetchDescription(photo.id)
        .then((payload) => {
          if (payload.ready && payload.description) {
            setPhoto((current) => (current ? { ...current, description: payload.description } : current));
          }
        })
        .catch(() => {
          // Ignore transient polling errors.
        });
    }, 3000);

    return () => window.clearInterval(intervalId);
  }, [photo, aiEnabled]);

  const handleDelete = async () => {
    if (!photo || !window.confirm('Are you sure you want to delete this photo?')) {
      return;
    }

    await deletePhoto(photo.id);
    navigate('/');
  };

  if (loading) {
    return <div className="alert alert-secondary">Loading photo...</div>;
  }

  if (error || !photo) {
    return (
      <div className="alert alert-warning">
        <h4>Photo not found</h4>
        <p className="mb-3">{error ?? "The requested photo doesn't exist."}</p>
        <Link className="btn btn-primary" to="/">
          Back to Gallery
        </Link>
      </div>
    );
  }

  return (
    <div className="photo-detail-container">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <Link to="/" className="btn btn-outline-secondary">
          Back to Gallery
        </Link>
        <button className="btn btn-danger" onClick={() => void handleDelete()}>
          Delete
        </button>
      </div>

      <div className="row">
        <div className="col-lg-8 mb-4">
          <div className="card">
            <div className="card-body p-0">
              <img
                src={`/photo/${photo.id}`}
                alt={photo.originalFileName}
                className="img-fluid w-100 photo-detail-image"
                style={{ maxHeight: '80vh', objectFit: 'contain', backgroundColor: '#f8f9fa' }}
              />
            </div>
          </div>

          <div className="d-flex justify-content-between mt-3">
            <div>
              {previousPhotoId && (
                <Link className="btn btn-outline-primary" to={`/detail/${previousPhotoId}`}>
                  Previous Photo
                </Link>
              )}
            </div>
            <div>
              {nextPhotoId && (
                <Link className="btn btn-outline-primary" to={`/detail/${nextPhotoId}`}>
                  Next Photo
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
                <dd className="col-sm-7 text-break">{photo.originalFileName}</dd>

                <dt className="col-sm-5">Uploaded:</dt>
                <dd className="col-sm-7">{new Date(photo.uploadedAt).toLocaleString()}</dd>

                <dt className="col-sm-5">File Size:</dt>
                <dd className="col-sm-7">{formatFileSize(photo.fileSize)}</dd>

                {photo.width && photo.height && (
                  <>
                    <dt className="col-sm-5">Dimensions:</dt>
                    <dd className="col-sm-7">{photo.width} x {photo.height} px</dd>
                  </>
                )}

                <dt className="col-sm-5">Type:</dt>
                <dd className="col-sm-7">
                  <span className="badge bg-secondary">{photo.mimeType}</span>
                </dd>

                {aiEnabled && (
                  <>
                    <dt className="col-sm-5 mt-2">Description:</dt>
                    <dd className="col-sm-7 mt-2 text-muted fst-italic">
                      {photo.description?.trim() ? photo.description : 'Generating...'}
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
