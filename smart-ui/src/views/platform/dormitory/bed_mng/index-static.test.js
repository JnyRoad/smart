import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const here = path.dirname(fileURLToPath(import.meta.url))
const pagePath = path.resolve(here, 'index.vue')
const page = fs.readFileSync(pagePath, 'utf8')

function componentTag(name) {
  const match = page.match(new RegExp(`<${name}\\b[\\s\\S]*?\\/>`))
  return match ? match[0] : ''
}

describe('bed_mng page static wiring', () => {
  it('keeps the split edit dialogs wired through their dedicated row props', () => {
    expect(componentTag('dlgEditTime')).toEqual(expect.stringContaining(':visible="timeVisible"'))
    expect(componentTag('dlgEditTime')).toEqual(expect.stringContaining(':row="timeForm"'))
    expect(componentTag('dlgEditTime')).toEqual(expect.stringContaining('@dlgdo="val => timeVisible = val"'))
    expect(componentTag('dlgEditTime')).toEqual(expect.stringContaining('@refresh="refreshList"'))

    expect(componentTag('dlgEditRemark')).toEqual(expect.stringContaining(':visible="simpleRemarkVisible"'))
    expect(componentTag('dlgEditRemark')).toEqual(expect.stringContaining(':row="simpleRemarkForm"'))
    expect(componentTag('dlgEditRemark')).toEqual(expect.stringContaining('@dlgdo="val => simpleRemarkVisible = val"'))
    expect(componentTag('dlgEditRemark')).toEqual(expect.stringContaining('@refresh="refreshList"'))

    expect(componentTag('dlgEditCheckIn')).toEqual(expect.stringContaining(':visible="editCheckInVisible"'))
    expect(componentTag('dlgEditCheckIn')).toEqual(expect.stringContaining(':row="editCheckInForm"'))
    expect(componentTag('dlgEditCheckIn')).toEqual(expect.stringContaining('@dlgdo="val => editCheckInVisible = val"'))
    expect(componentTag('dlgEditCheckIn')).toEqual(expect.stringContaining('@refresh="refreshList"'))
  })

  it('does not keep dead data fields left behind by the dialog split', () => {
    expect(page).not.toMatch(/\n\s{6}checkInLoading:\s*false,/)
    expect(page).not.toMatch(/\n\s{6}obj:\s*\{\},/)
    expect(page).not.toMatch(/\n\s{6}rangIDTemp:\s*\[\],/)
  })
})
