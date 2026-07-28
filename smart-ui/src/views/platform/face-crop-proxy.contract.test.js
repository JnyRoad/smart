import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'

const uploadComponents = [
  ['./resume/_upload.vue', '/platform/regist/face/crop'],
  ['./basic/personnel_manage/_upload.vue', '/platform/face/crop'],
  ['./outsourcing/onwork/_upload.vue', '/platform/face/crop']
]
const deprecatedAlgorithmRoute = ['/', 'algorithm', 'out', 'face', 'cut'].join('/')

describe('后台人脸裁剪代理契约', () => {
  it.each(uploadComponents)('%s 只能通过受控平台代理提交人脸数据', (sourcePath, expectedRoute) => {
    const source = readFileSync(fileURLToPath(new URL(sourcePath, import.meta.url)), 'utf8')

    expect(source).toContain("from '@/router/axios'")
    expect(source).toContain(`url: '${expectedRoute}'`)
    expect(source).not.toContain(deprecatedAlgorithmRoute)
    expect(source).not.toMatch(/from\s+["']axios["']/)
  })
})
