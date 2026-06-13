import type { Page } from '@playwright/test'
import CryptoJS from 'crypto-js'

/** Deterministic key injected via config.js so AES tests run real decryption. */
export const TEST_KEY = 'abcdef0123456789'

/** Produces ciphertext the way the legacy gateway does (AES-ECB/Pkcs7, hex out). */
export function legacyCipherHex(plain: string): string {
  const key = CryptoJS.enc.Utf8.parse(TEST_KEY)
  const encrypted = CryptoJS.AES.encrypt(plain, key, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7,
  })
  return encrypted.ciphertext.toString(CryptoJS.enc.Hex)
}

export async function injectTestKey(page: Page) {
  // Overlay onto the real config.js so tenant-config changes don't drift here.
  await page.route('**/config.js', async (route) => {
    const real = await route.fetch()
    const body = await real.text()
    await route.fulfill({
      contentType: 'application/javascript',
      body: `${body}\nwindow.__SMART_CONFIG__.securityEncodeKey = '${TEST_KEY}';`,
    })
  })
}
