/**
 * Backend-provided URLs (announcement links, PDF previews) are rendered into
 * iframes or navigated to directly; restrict them to http(s) so a compromised
 * record cannot inject a `javascript:` scheme.
 */
export function isHttpUrl(value: string | undefined | null): value is string {
  if (!value) return false
  const base = typeof window === 'undefined' ? 'http://localhost' : window.location.origin
  try {
    const protocol = new URL(value, base).protocol
    return protocol === 'http:' || protocol === 'https:'
  } catch {
    return false
  }
}
