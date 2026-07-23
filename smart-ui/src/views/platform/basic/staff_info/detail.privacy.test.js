import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const source = readFileSync(resolve(process.cwd(), 'src/views/platform/basic/staff_info/detail.vue'), 'utf8')

describe('员工详情页隐私展示契约', () => {
  it('只使用受控管理员详情，不请求完整员工实体', () => {
    expect(source).toContain('getAdminStaffDetail')
    expect(source).not.toContain('getById(this.$route.params.id)')
  })

  it('不渲染人脸、联系方式、住址、邮箱或紧急联系人', () => {
    [
      'facePic',
      'phone',
      'residentaddress',
      'email',
      'smtStaffEmergency'
    ].forEach(field => {
      expect(source).not.toContain(field)
    })
  })
})
