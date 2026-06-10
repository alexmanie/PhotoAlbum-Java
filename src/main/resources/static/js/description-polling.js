/**
 * description-polling.js
 * Polls /api/photos/{id}/description for every photo that is still awaiting
 * an AI-generated description and updates the UI without a full page refresh.
 *
 * Works on both the gallery page (multiple cards with class "desc-pending")
 * and the detail page (single element with id "detail-description").
 */
(function () {
    'use strict';

    var POLL_INTERVAL_MS = 3000;   // poll every 3 seconds
    var MAX_POLLS = 40;            // give up after ~2 minutes per photo

    /** Map of photoId -> {element, pollCount, timerId} for active polls */
    var activePolls = {};

    /**
     * Fetch description for one photo and update the DOM.
     * Stops polling when the description arrives or the max poll count is reached.
     */
    function fetchAndUpdate(photoId, descElement, isDetailPage) {
        var state = activePolls[photoId];
        if (!state) return;

        state.pollCount += 1;
        if (state.pollCount > MAX_POLLS) {
            stopPolling(photoId, descElement, isDetailPage, null);
            return;
        }

        fetch('/api/photos/' + photoId + '/description')
            .then(function (res) {
                if (!res.ok) {
                    // Photo deleted or not found – stop polling silently
                    clearPoll(photoId);
                    return null;
                }
                return res.json();
            })
            .then(function (data) {
                if (!data) return;
                if (data.ready && data.description) {
                    stopPolling(photoId, descElement, isDetailPage, data.description);
                }
            })
            .catch(function () {
                // Network error – will retry on next interval
            });
    }

    /**
     * Update the DOM with the received description and stop the timer.
     */
    function stopPolling(photoId, descElement, isDetailPage, description) {
        clearPoll(photoId);

        if (isDetailPage) {
            if (description) {
                descElement.innerHTML = escapeHtml(description);
                descElement.setAttribute('data-description-ready', 'true');
            } else {
                descElement.innerHTML = '<span class="text-muted fst-italic">Description not available.</span>';
            }
        } else {
            // Gallery card
            var placeholder = descElement.querySelector('.desc-placeholder');
            if (placeholder) {
                if (description) {
                    var truncated = description.length > 90 ? description.substring(0, 87) + '…' : description;
                    placeholder.innerHTML = escapeHtml(truncated);
                    placeholder.classList.remove('desc-placeholder');
                } else {
                    placeholder.innerHTML = 'Description not available.';
                    placeholder.classList.remove('desc-placeholder');
                }
            }
            descElement.classList.remove('desc-pending');
        }
    }

    function clearPoll(photoId) {
        var state = activePolls[photoId];
        if (state && state.timerId) {
            clearInterval(state.timerId);
        }
        delete activePolls[photoId];
    }

    /**
     * Kick off polling for a single photo.
     * @param {string}  photoId     - UUID of the photo
     * @param {Element} descElement - element that will be updated
     * @param {boolean} isDetail    - true when on the detail page
     */
    function startPolling(photoId, descElement, isDetail) {
        if (activePolls[photoId]) return; // already polling

        var state = { element: descElement, pollCount: 0, timerId: null, isDetail: isDetail };
        activePolls[photoId] = state;

        // Start immediately then repeat
        fetchAndUpdate(photoId, descElement, isDetail);
        state.timerId = setInterval(function () {
            fetchAndUpdate(photoId, descElement, isDetail);
        }, POLL_INTERVAL_MS);
    }

    function escapeHtml(str) {
        var div = document.createElement('div');
        div.appendChild(document.createTextNode(str));
        return div.innerHTML;
    }

    // -------------------------------------------------------------------------
    // Initialise on DOMContentLoaded
    // -------------------------------------------------------------------------
    document.addEventListener('DOMContentLoaded', function () {

        // --- Detail page ---
        var detailDesc = document.getElementById('detail-description');
        if (detailDesc) {
            var ready = detailDesc.getAttribute('data-description-ready');
            if (ready !== 'true') {
                var photoId = detailDesc.getAttribute('data-photo-id');
                if (photoId) {
                    startPolling(photoId, detailDesc, true);
                }
            }
        }

        // --- Gallery page ---
        var pendingCards = document.querySelectorAll('.desc-pending');
        pendingCards.forEach(function (card) {
            var pid = card.getAttribute('data-photo-id');
            var descEl = card.querySelector('.photo-description');
            if (pid && descEl) {
                startPolling(pid, descEl, false);
            }
        });
    });

    /**
     * Public helper so upload.js can immediately start polling for
     * newly uploaded photos that are added to the DOM dynamically.
     */
    window.startDescriptionPolling = startPolling;

})();

