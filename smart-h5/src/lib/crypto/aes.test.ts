import CryptoJS from 'crypto-js'
import { beforeEach, describe, expect, it } from 'vitest'
import { decryptFromHex, encryptFields } from './aes'

const TEST_KEY = 'abcdef0123456789' // 16-byte test key, NOT the production key

beforeEach(() => {
  window.__SMART_CONFIG__ = { securityEncodeKey: TEST_KEY }
})

/** Oracle: verbatim port of the legacy encryption.js default export. */
function legacyEncrypt(plain: string, key: string): string {
  const parsedKey = CryptoJS.enc.Latin1.parse(key)
  return CryptoJS.AES.encrypt(plain, parsedKey, {
    iv: parsedKey,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.ZeroPadding,
  }).toString()
}

/** Inverse oracle: builds the hex ciphertext the legacy decryption() expects. */
function legacyEncryptForDecrypt(plain: string, key: string): string {
  const parsedKey = CryptoJS.enc.Utf8.parse(key)
  const encrypted = CryptoJS.AES.encrypt(plain, parsedKey, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7,
  })
  return encrypted.ciphertext.toString(CryptoJS.enc.Hex)
}

// '0123456789abcdef' is exactly one AES block — ZeroPadding's block-aligned
// behavior is a classic divergence point and must stay pinned.
const SAMPLES = ['123456', '888000', '动态码中文', 'a-long-sample-text-with-多字节-🔒', '1', '0123456789abcdef']

/**
 * Hardcoded anchors generated once with crypto-js against TEST_KEY. Unlike the
 * oracle functions below (same library as the implementation, so they only
 * catch future refactors), these pin the absolute byte-level output — if the
 * algorithm port was transcribed wrong, these fail.
 */
const ANCHORS = {
  ecbHexOf123456: '55f75f199dd3e4995bf1eadebbcfc3db',
  cbcBase64Of123456: '5cihxtJb8addilvY3RdVvQ==',
  cbcBase64Of16Bytes: '6OHH8UZtF2hB9wZ+NIvVNg==',
}

describe('hardcoded cipher anchors', () => {
  it('decryptFromHex 还原固定密文', () => {
    expect(decryptFromHex(ANCHORS.ecbHexOf123456)).toBe('123456')
  })
  it('encryptFields 产出固定密文（含整块对齐样本）', () => {
    expect(encryptFields({ v: '123456' }, ['v']).v).toBe(ANCHORS.cbcBase64Of123456)
    expect(encryptFields({ v: '0123456789abcdef' }, ['v']).v).toBe(ANCHORS.cbcBase64Of16Bytes)
  })
})

describe('encryptFields vs legacy oracle', () => {
  it('同明文同 key 密文完全一致（CBC/ZeroPadding/IV=key）', () => {
    for (const plain of SAMPLES) {
      const result = encryptFields({ value: plain }, ['value'])
      expect(result.value).toBe(legacyEncrypt(plain, TEST_KEY))
    }
  })

  it('Base64 模式与旧实现一致', () => {
    const result = encryptFields({ v: 'hello' }, ['v'], 'Base64')
    expect(result.v).toBe(window.btoa('hello'))
  })

  it('不修改原对象与未列字段', () => {
    const src = { a: '1', b: '2' }
    const out = encryptFields(src, ['a'])
    expect(src.a).toBe('1')
    expect(out.b).toBe('2')
    expect(out.a).not.toBe('1')
  })
})

describe('decryptFromHex vs legacy oracle', () => {
  it('oracle 构造的 hex 密文能还原明文（ECB/Pkcs7/Utf8-key）', () => {
    for (const plain of SAMPLES) {
      expect(decryptFromHex(legacyEncryptForDecrypt(plain, TEST_KEY))).toBe(plain)
    }
  })

  it('非法密文返回空串（防御，不抛错）', () => {
    expect(decryptFromHex('zzzz-not-hex')).toBe('')
  })

  it('错误 key 解密返回空串而非乱码异常', () => {
    const cipher = legacyEncryptForDecrypt('123456', TEST_KEY)
    window.__SMART_CONFIG__ = { securityEncodeKey: 'wrongkey90123456' }
    expect(decryptFromHex(cipher)).toBe('')
  })
})

describe('key 缺失', () => {
  it('无 runtime key 且无 env key 时抛错（快速失败）', () => {
    window.__SMART_CONFIG__ = {}
    expect(() => decryptFromHex('00')).toThrow()
  })
})
