interface AreaEnumItem {
  code: string | number
  desc: string
}

export interface PassCodeAreaSource {
  areaType?: Array<string | number> | string
  permitArea?: string
  permitOldArea?: string
}

export interface PassCodeAreaDisplay {
  newAreas: string
  oldAreas: string
}

function splitAreaCodes(areaType: PassCodeAreaSource['areaType']): string[] {
  if (Array.isArray(areaType)) return areaType.map(String).filter(Boolean)
  if (typeof areaType === 'string') return areaType.split(',').map((code) => code.trim()).filter(Boolean)
  return []
}

function appendText(list: string[], value: string | undefined): void {
  const text = value?.trim()
  if (text) list.push(text)
}

/**
 * 通行码详情沿用旧版契约：areaType 保存标准授权区域 code，
 * permitArea/permitOldArea 只保存手填的自定义区域文本，二者需要合并展示。
 */
export function buildPassCodeAreaDisplay(
  source: PassCodeAreaSource,
  newAreaOptions: AreaEnumItem[],
  oldAreaOptions: AreaEnumItem[],
): PassCodeAreaDisplay {
  const newNames = new Map(newAreaOptions.map((item) => [String(item.code), item.desc]))
  const oldNames = new Map(oldAreaOptions.map((item) => [String(item.code), item.desc]))
  const newAreas: string[] = []
  const oldAreas: string[] = []

  for (const code of splitAreaCodes(source.areaType)) {
    const newName = newNames.get(code)
    if (newName) newAreas.push(newName)
    const oldName = oldNames.get(code)
    if (oldName) oldAreas.push(oldName)
  }

  appendText(newAreas, source.permitArea)
  appendText(oldAreas, source.permitOldArea)

  return {
    newAreas: newAreas.join('，'),
    oldAreas: oldAreas.join('，'),
  }
}
