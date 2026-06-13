/**
 * Pulls the photo id out of a checkFace response `data`. The gateway nests it
 * as `{ photoId }` (legacy `res.data.photoId`); some callers receive the id
 * directly. Returns '' when no usable id is present.
 */
export function extractPhotoId(data: unknown): string {
  if (data === undefined || data === null || data === '') return ''
  if (typeof data === 'object') {
    const pid = (data as { photoId?: unknown }).photoId
    return pid === undefined || pid === null ? '' : String(pid)
  }
  return String(data)
}

/** Gateway binary-image endpoint (legacy GET_IMAGE_URL). */
export const PHOTO_VIEW_BASE = '/platform/image/view'

export function photoViewUrl(photoId: string): string {
  return `${PHOTO_VIEW_BASE}/${photoId}`
}
