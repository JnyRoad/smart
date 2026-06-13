import type { FactoryAreaConfig } from './api'
import type { AreaSelection } from './flow-store'

/**
 * 旧版 visitorInfo.vue:699-701 的区域字段语义（净室对照）：只取**当前选中工厂**
 * 一个，permitArea/permitOldArea 装的是自定义输入文本（custom，按工厂 areaFlag
 * 1 新 / 0 老 分流），areaType 是该工厂勾选的区域 code 列表。
 * 注意：不是把区域 code 拼进 permitArea，custom 也不进 remark。
 */
export interface VisitorAreaFields {
  permitFactoryType: string
  permitArea: string
  permitOldArea: string
  areaType: string[]
}

export function buildVisitorAreaFields(
  permitFactoryType: string | undefined,
  areasByFactory: Record<string, AreaSelection>,
  factories: FactoryAreaConfig[],
): VisitorAreaFields {
  const factory = factories.find((f) => f.factoryType === permitFactoryType)
  const sel = permitFactoryType ? areasByFactory[permitFactoryType] : undefined
  const custom = sel?.custom ?? ''
  // 旧版 currentAreaFlag 在无当前工厂时默认 1（新）；areaFlag===0 才算老工厂。
  // areaFlag===0 才算老工厂，其余（含工厂缺失）按新工厂。提交前 permitFactoryType
  // 必选（info 页已拦截空值），所以「工厂缺失」分支实际不可达，仅作防御。
  const isOldFactory = factory?.areaFlag === 0
  return {
    permitFactoryType: permitFactoryType ?? '',
    permitArea: !isOldFactory && custom ? custom : '',
    permitOldArea: isOldFactory && custom ? custom : '',
    areaType: sel?.list ?? [],
  }
}
