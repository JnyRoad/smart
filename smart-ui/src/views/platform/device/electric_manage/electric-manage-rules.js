// 电表管理页（device/electric_manage/index.vue）的纯展示/格式化规则。
// 从 index.vue 抽出、不依赖组件实例 this，行为与原内联实现逐字等价，仅迁移位置，
// 便于单测与复用。任何历史语义（非在线/离线返回 undefined、宽松 == 区域映射）刻意保留。

// 设备在线状态文案 → 状态色 class。
// 仅“在线”“离线”有映射，其它（未连接 / 未关联等）返回 undefined（保留原 switch 无 default 行为）。
export function returnColor(status) {
  switch (status) {
    case '在线':
      return 'deviceStatus2'
    case '离线':
      return 'deviceStatus1'
  }
}

// 导出行列变换：按 filterVal 给定的字段顺序，从每行对象取值组成二维数组。
// 缺失字段取到 undefined（与原 jsonData.map(v => filterVal.map(j => v[j])) 一致）。
export function formatJson(filterVal, jsonData) {
  return jsonData.map(row => filterVal.map(key => row[key]))
}

// 区域类型描述：placeType == 0 → 宿舍，否则 厂区（保留原宽松 == 语义）。
export function placeTypeDesc(placeType) {
  return placeType == 0 ? '宿舍' : '厂区'
}

// 设备行的标签聚合：把 tagList 折叠成展示用 tagName（逗号拼接）与 tagIds 数组。
// 行为与原 getList 内联逻辑逐字一致：tagList 为 null 或空 → { tagName: '', tagIds: [] }。
// 注意：tagList 为 undefined 时与原实现一样会抛（undefined.length），调用方保证传 null 或数组。
export function meterTagFields(tagList) {
  const names = []
  const tagIds = []
  if (tagList !== null && tagList.length > 0) {
    tagList.forEach((tag) => {
      names.push(tag.tagName)
      tagIds.push(tag.id)
    })
  }
  return { tagName: names.join(','), tagIds }
}
