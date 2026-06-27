import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

function readPanelSource(fileName) {
  return readFileSync(resolve(process.cwd(), `src/views/platform/panel/${fileName}`), 'utf8')
}

describe('panel refresh static guards', () => {
  it('does not import Node timers into browser panel components', () => {
    expect(readPanelSource('bigdata.vue')).not.toContain('from "timers"')
  })

  it('does not assign global window.onresize from accessto panel', () => {
    expect(readPanelSource('accessto.vue')).not.toContain('window.onresize =')
  })
})
