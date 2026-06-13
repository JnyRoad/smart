import CryptoJS from 'crypto-js'
import { getTenantConfig } from '@/lib/config/tenant'

/**
 * Legacy-compatible AES helpers (cleanroom port of the old encryption.js).
 * The two directions are deliberately asymmetric, matching the legacy app:
 * - encrypt: AES-CBC, ZeroPadding, key parsed as Latin1, IV = key, -> base64
 * - decrypt: AES-ECB, Pkcs7, key parsed as Utf8, input is HEX ciphertext
 * Compatibility with the backend is enforced by oracle tests that replicate
 * the legacy algorithm verbatim (src/lib/crypto/aes.test.ts).
 */
function getKey(): string {
  const key = getTenantConfig().securityEncodeKey ?? process.env.NEXT_PUBLIC_SECURITY_ENCODE_KEY
  if (!key) throw new Error('securityEncodeKey is not configured')
  return key
}

/** Encrypts the listed fields in place of a shallow copy; 'Base64' mode mirrors legacy btoa. */
export function encryptFields<T extends Record<string, unknown>>(
  data: T,
  fields: (keyof T)[],
  type?: 'Base64',
): T {
  const result = { ...data }
  if (type === 'Base64') {
    for (const field of fields) {
      result[field] = window.btoa(String(result[field])) as T[keyof T]
    }
    return result
  }
  const parsedKey = CryptoJS.enc.Latin1.parse(getKey())
  for (const field of fields) {
    result[field] = CryptoJS.AES.encrypt(String(result[field]), parsedKey, {
      iv: parsedKey,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.ZeroPadding,
    }).toString() as T[keyof T]
  }
  return result
}

/** Decrypts the gateway's hex ciphertext; returns '' on malformed input or key mismatch. */
export function decryptFromHex(hexStr: string): string {
  const key = CryptoJS.enc.Utf8.parse(getKey())
  try {
    return CryptoJS.AES.decrypt(
      CryptoJS.enc.Base64.stringify(CryptoJS.enc.Hex.parse(hexStr)),
      key,
      { mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7 },
    ).toString(CryptoJS.enc.Utf8)
  } catch {
    return ''
  }
}
