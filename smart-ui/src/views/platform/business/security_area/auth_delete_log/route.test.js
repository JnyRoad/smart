import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const routeFile = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../../../../router/platform/business.js')

describe('保密区自动删权报表本地路由', () => {
  it('将报表入口映射到独立组件路径', () => {
    const source = fs.readFileSync(routeFile, 'utf8')

    expect(source).toContain("path: 'auth_delete_log/index'")
    expect(source).toContain("@/views/platform/business/security_area/auth_delete_log/index")
  })
})
