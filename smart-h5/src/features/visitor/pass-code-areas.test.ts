import { describe, expect, it } from 'vitest'
import { buildPassCodeAreaDisplay } from './pass-code-areas'

describe('buildPassCodeAreaDisplay', () => {
  it('combines saved areaType codes with custom new and old factory area text', () => {
    const display = buildPassCodeAreaDisplay(
      {
        areaType: [101, 201],
        permitArea: '新厂临时区域',
        permitOldArea: '老厂临时区域',
      },
      [{ code: 101, desc: '新厂一号门' }],
      [{ code: 201, desc: '老厂仓库' }],
    )

    expect(display.newAreas).toBe('新厂一号门，新厂临时区域')
    expect(display.oldAreas).toBe('老厂仓库，老厂临时区域')
  })

  it('accepts comma-separated areaType codes returned by older gateways', () => {
    const display = buildPassCodeAreaDisplay(
      { areaType: '101, 201' },
      [{ code: 101, desc: '新厂一号门' }],
      [{ code: 201, desc: '老厂仓库' }],
    )

    expect(display.newAreas).toBe('新厂一号门')
    expect(display.oldAreas).toBe('老厂仓库')
  })

  it('keeps custom area text when saved codes no longer exist in enums', () => {
    const display = buildPassCodeAreaDisplay(
      {
        areaType: [999],
        permitArea: ' 新厂临时区域 ',
        permitOldArea: ' 老厂临时区域 ',
      },
      [],
      [],
    )

    expect(display.newAreas).toBe('新厂临时区域')
    expect(display.oldAreas).toBe('老厂临时区域')
  })
})
