export interface LockRefreshUploadResult {
  code?: number
  data?: unknown
  resultData?: { base64?: string | null }
}

export function resolveLockRefreshFacePic(
  upload: LockRefreshUploadResult,
  uploadedBase64: string,
): string {
  return uploadedBase64 || upload.resultData?.base64 || ''
}
