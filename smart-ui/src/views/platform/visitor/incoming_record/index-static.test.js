import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const source = readFileSync(resolve(process.cwd(), 'src/views/platform/visitor/incoming_record/index.vue'), 'utf8')

describe('incoming_record 作废入口', () => {
  it('展示已作废状态筛选，并由独立按钮权限控制作废操作', () => {
    expect(source).toContain('<el-option label="已作废" value="7"></el-option>')
    expect(source).toContain("permissions['platform_visitor_incoming_revoke']")
    expect(source).toContain('@click.stop="revokeApply(item)"')
    expect(source).toContain('作废申请')
  })
})
