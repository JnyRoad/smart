import { stripSpaces } from '@/lib/text'

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

/** 当前访客信息页与陪同页只收居民身份证，提交时明确类型0；不把其他证件默认成身份证。 */
export function buildIdCardFellow(
  person: { fellowName: string; fellowPhotoId: string; certNo: string },
  isMain: 0 | 1,
) {
  const certNo = stripSpaces(person.certNo).toUpperCase()
  const check = validateIdCard(certNo)
  if (!check.ok) throw new Error(check.message)
  return {
    isMain,
    fellowName: stripSpaces(person.fellowName),
    fellowPhotoId: person.fellowPhotoId,
    certNo,
    certType: 0,
  }
}
