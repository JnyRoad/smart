const WEIGHTS = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
const CHECK_CODES = '10X98765432'

export type IdCardResult = { ok: true } | { ok: false; message: string }

/** 18-digit Chinese ID validation: format (17 digits + digit/X) plus ISO 7064 check digit. */
export function validateIdCard(certNo: string): IdCardResult {
  if (!/^\d{17}[\dXx]$/.test(certNo)) return { ok: false, message: '证件号码格式不正确' }
  const sum = WEIGHTS.reduce((acc, weight, i) => acc + weight * Number(certNo[i]), 0)
  if (certNo[17]?.toUpperCase() !== CHECK_CODES[sum % 11]) {
    return { ok: false, message: '证件号码校验位不正确' }
  }
  return { ok: true }
}
