import { useEffect, useState } from 'react';
import { fetchDescription } from '../api/photos';

const POLL_INTERVAL_MS = 3000;
const MAX_POLLS = 40; // ~2 minutes

/**
 * Polls the backend for a photo's AI-generated description until it is ready,
 * the photo disappears, or the poll budget is exhausted.
 *
 * @param photoId  Photo id to poll for.
 * @param enabled  When false (AI disabled or already known), no polling occurs.
 * @param initial  Description already known at mount time, if any.
 */
export function usePhotoDescription(
  photoId: string,
  enabled: boolean,
  initial: string | null,
): { description: string | null; pending: boolean } {
  const [description, setDescription] = useState<string | null>(initial);
  const [pending, setPending] = useState<boolean>(
    enabled && !(initial && initial.trim().length > 0),
  );

  useEffect(() => {
    const hasInitial = !!(initial && initial.trim().length > 0);
    if (!enabled || hasInitial) {
      setDescription(initial);
      setPending(false);
      return;
    }

    let cancelled = false;
    let polls = 0;

    const tick = async () => {
      polls += 1;
      if (polls > MAX_POLLS) {
        if (!cancelled) setPending(false);
        clearInterval(timer);
        return;
      }
      try {
        const status = await fetchDescription(photoId);
        if (cancelled) return;
        if (status.ready && status.description) {
          setDescription(status.description);
          setPending(false);
          clearInterval(timer);
        }
      } catch {
        // network error – stop polling silently
        if (!cancelled) setPending(false);
        clearInterval(timer);
      }
    };

    void tick();
    const timer = setInterval(() => void tick(), POLL_INTERVAL_MS);

    return () => {
      cancelled = true;
      clearInterval(timer);
    };
  }, [photoId, enabled, initial]);

  return { description, pending };
}
