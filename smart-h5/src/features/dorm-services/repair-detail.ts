export interface RepairPhotoSource {
  imgs?: unknown[]
  faultImgs?: unknown[]
}

function cleanPhotos(photos: unknown[] | undefined): string[] {
  return (photos ?? []).filter((photo): photo is string => typeof photo === 'string' && photo !== '').slice(0, 3)
}

/** Legacy detail returns `imgs`; current tests and some mocks use `faultImgs`. */
export function repairDetailPhotos(detail: RepairPhotoSource | undefined): string[] {
  const legacyPhotos = cleanPhotos(detail?.imgs)
  if (legacyPhotos.length > 0) return legacyPhotos
  return cleanPhotos(detail?.faultImgs)
}
