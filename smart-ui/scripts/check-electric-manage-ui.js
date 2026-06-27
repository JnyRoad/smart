// 电表管理页（device/electric_manage/index.vue）UI 接线静态守卫（安全网）。
// 目的：在后续把这个 god file 逐步抽成纯函数 / 受控子组件之前，先锁住页面对外的
// 关键接线契约——搜索区按钮与事件、批量操作下拉、搜索表单字段、设备卡片行内操作、
// 分页、新增/编辑弹窗与提交、绑定房间 / 设置标签子组件、导入弹窗、以及 _service API 依赖面。
// 任何一处接线在重构中被悄悄改名或丢失，本守卫都会红，逼迫执行者把契约同步迁移到新结构。
// 只读源码做正则断言，零运行时副作用；接入 pnpm gate 的 electric-manage-ui 步骤。
const fs = require('fs')
const path = require('path')

const root = path.resolve(__dirname, '..')

function readRequired(file) {
  const filePath = path.join(root, file)
  if (!fs.existsSync(filePath)) {
    throw new Error(`${file} is missing`)
  }
  return fs.readFileSync(filePath, 'utf8')
}

const INDEX = 'src/views/platform/device/electric_manage/index.vue'

const checks = [
  {
    file: INDEX,
    custom: (content) => [
      /@click="searchSubmit\(searchForm\)"/,
      /@click="resetFrom\('searchForm', searchForm\)"/,
      /@click="addFormVisible = true"/,
      /@click="supplierImportDialog\.visible = true"/,
      /@click="export2Excel"/,
      /搜索/, /清空/, /添加电表/, /导入电表/, /导出电表/
    ].every(pattern => pattern.test(content)),
    message: 'electric manage must keep top action buttons and their click wiring'
  },
  {
    file: INDEX,
    custom: (content) => [
      /@click="batchDelete"/,
      /@click="meterRead"/,
      /@click="reDownload"/,
      /@click="handelChange\(1\)"/,
      /@click="handelChange\(0\)"/,
      /@click="handelSetTag"/,
      /批量删除/, /批量手动抄表/, /批量下载档案/, /批量开闸/, /批量关闸/, /批量设置标签/
    ].every(pattern => pattern.test(content)),
    message: 'electric manage must keep the batch operation dropdown items and handlers'
  },
  {
    file: INDEX,
    custom: (content) => [
      /ref="searchForm"[\s\S]*:model="searchForm"/,
      /prop="parkId"/,
      /prop="place"/,
      /prop="status"/,
      /prop="address"/,
      /prop="concenId"/,
      /prop="tagIds"/
    ].every(pattern => pattern.test(content)),
    message: 'electric manage must keep the search form fields'
  },
  {
    file: INDEX,
    custom: (content) => [
      /@change="valveChange\(item\)"/,
      /@click="handleEdit\(item, false\)"/,
      /@click="handleEdit\(item, true\)"/,
      /@click="handleDel\(item\)"/,
      /@click="logsList\(item\)"/,
      /@click="permittedList\(item\)"/,
      /returnColor\(item\.status\)/
    ].every(pattern => pattern.test(content)),
    message: 'electric manage must keep device card valve switch and row action wiring'
  },
  {
    file: INDEX,
    custom: (content) => [
      /@size-change="handleSizeChange"/,
      /@current-change="handleCurrentChange"/
    ].every(pattern => pattern.test(content)),
    message: 'electric manage must keep pagination size/current change wiring'
  },
  {
    file: INDEX,
    custom: (content) => [
      /:visible\.sync="addFormVisible"[\s\S]*@close="closeDialog"/,
      /<el-form[\s\S]*:rules="addRules"[\s\S]*ref="addForm"[\s\S]*:model="addForm"/,
      /prop="name"/, /prop="seq"/, /prop="ratio"/, /prop="address"/, /prop="port"/,
      /prop="concentratorId"/, /prop="placeType"/, /prop="roomName"/, /prop="areaIdArray"/,
      /@click="bindRoom"/,
      /@click="addSubmit\('addForm'\)"/
    ].every(pattern => pattern.test(content)),
    message: 'electric manage must keep add/edit dialog form fields, bind-room entry, and submit wiring'
  },
  {
    file: INDEX,
    custom: (content) => [
      /<BindRoom ref="bindRoom" @done="selectBed"/,
      /<setTag ref="setTag" :deviceIds="ckItem" @refresh="refresh"/
    ].every(pattern => pattern.test(content)),
    message: 'electric manage must keep BindRoom and setTag child component wiring'
  },
  {
    file: INDEX,
    custom: (content) => [
      /:visible\.sync="supplierImportDialog\.visible"/,
      /@close="resetUploadSupplierForm\('uploadSupplierForm'\)"/,
      /ref="fileSupplier"[\s\S]*@change="fileChangeSupplier"/,
      /@click="uploadSupplier"/,
      /@click="uploadSupplierSubmit\('uploadSupplierForm'\)"/
    ].every(pattern => pattern.test(content)),
    message: 'electric manage must keep the excel import dialog file input and submit wiring'
  },
  {
    file: INDEX,
    // 锁定 index.vue 直接依赖的 _service 导入名集合（顺序不变）；用 \s* 容忍空格，
    // 避免无谓地把历史排版怪癖写死，后续 eslint 格式化不会误红，但增删 API 名仍会红。
    required: /import \{\s*concentratorList,\s*getTagList,\s*addObj,\s*fetchList,\s*batchDelete,\s*putObj,\s*changeObj,\s*supplierImport,\s*flushProgress,\s*putValve,\s*putValves,\s*meterRead,\s*reDownload\s*\} from '\.\/_service'/,
    message: 'electric manage must keep the _service API dependency surface'
  },
  {
    file: INDEX,
    // 锁定纯展示/格式化函数已抽到 electric-manage-rules.js 并被委托使用，防止重构回退到内联实现。
    custom: (content) => [
      /import \{\s*returnColor,\s*formatJson,\s*placeTypeDesc\s*\} from '\.\/electric-manage-rules'/,
      /item\.placeTypeDesc = placeTypeDesc\(item\.placeType\)/
    ].every(pattern => pattern.test(content)),
    message: 'electric manage must keep delegating display/format helpers to electric-manage-rules'
  }
]

const failures = []

checks.forEach(check => {
  try {
    const content = readRequired(check.file)
    if (check.required && !check.required.test(content)) {
      failures.push(`${check.file}: ${check.message}`)
    }
    if (check.custom && !check.custom(content)) {
      failures.push(`${check.file}: ${check.message}`)
    }
  } catch (error) {
    failures.push(error.message)
  }
})

if (failures.length > 0) {
  console.error('Electric manage UI check failed:')
  failures.forEach(failure => console.error(`- ${failure}`))
  process.exit(1)
}

console.log('Electric manage UI check passed.')
