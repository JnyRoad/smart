import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const sourcePath = resolve(process.cwd(), 'src/views/platform/device/electric_manage/index.vue')
const source = readFileSync(sourcePath, 'utf8')

describe('electric_manage scope wiring', () => {
  it('uses the captured component scope when refreshing after cancel or failure', () => {
    expect(source).not.toContain('_this.getList(this.page, this.searchForm)')
    expect(source.match(/_this\.getList\(_this\.page, _this\.searchForm\)/g).length).toBeGreaterThanOrEqual(6)
  })
})
