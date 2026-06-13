const fs = require('fs')
const path = require('path')

const root = path.resolve(__dirname, '..')

const checks = [
  {
    file: 'src/const/crud/platform/records/isc_card_task.js',
    required: /menu:\s*false/,
    message: 'ISC card task list must not render an operation column'
  },
  {
    file: 'src/const/crud/platform/records/isc_card_task.js',
    required: /label:\s*'任务ID'[\s\S]*prop:\s*'id'[\s\S]*hide:\s*true/,
    message: 'ISC card task list must keep task id hidden from the table'
  },
  {
    file: 'src/views/platform/records/isc_card_task/index.vue',
    forbidden: /slot=["']menu["']|<el-dialog[^>]*任务详情|openDetail\s*\(/,
    message: 'ISC card task page must not expose a detail action or dialog'
  },
  {
    file: 'src/const/crud/platform/business/isc_park_config.js',
    required: /formatter:\s*cardSyncEnabledText/,
    message: 'ISC park config card sync column must render readable text'
  },
  {
    file: 'src/const/crud/platform/business/isc_park_config.js',
    required: /启用卡片同步|停用卡片同步/,
    message: 'ISC park config card sync text must describe what the switch controls'
  },
  {
    file: 'src/views/platform/basic/staff_info/detail.vue',
    required: /\[0-9A-Z\]\{8,20\}/,
    message: 'staff detail card form must enforce Hikvision 8-20 digit or uppercase-letter card number format'
  },
  {
    file: 'src/views/platform/basic/staff_info/detail.vue',
    required: /8-20位数字或大写字母/,
    message: 'staff detail card form must tell users the Hikvision card number rule'
  },
  {
    file: 'src/views/platform/basic/staff_info/detail.vue',
    forbidden: /\\d\{1,31\}|1-31位数字/,
    message: 'staff detail card form must not keep the old 1-31 digit-only card rule'
  },
  {
    file: 'src/views/platform/basic/staff_info/detail.vue',
    required: /label="同步状态"[\s\S]*cardSyncStatusType\(scope\.row\.syncStatus\)[\s\S]*cardSyncStatusText\(scope\.row\)/,
    message: 'staff detail ISC card table must show card sync status'
  },
  {
    file: 'src/views/platform/basic/staff_info/detail.vue',
    required: /cardSyncStatusText\(row\)[\s\S]*待同步[\s\S]*已同步[\s\S]*同步失败[\s\S]*本地取消[\s\S]*cardSyncStatusType\(syncStatus\)/,
    message: 'staff detail must render all known staff card sync statuses'
  },
  {
    file: 'src/views/platform/basic/staff_info/detail.vue',
    forbidden: /this\.responseMessage\(res,\s*'保存ISC卡片失败'\)/,
    message: 'staff detail card save must not duplicate the global backend business error toast'
  },
  {
    file: 'src/router/axios.js',
    required: /res\.data\.code === 1[\s\S]*Message\(\{[\s\S]*message:\s*message[\s\S]*type:\s*'error'/,
    message: 'global axios interceptor must display backend business error message for staff detail card save'
  }
]

const failures = checks.flatMap(check => {
  const content = fs.readFileSync(path.join(root, check.file), 'utf8')
  if (check.required && !check.required.test(content)) {
    return [`${check.file}: ${check.message}`]
  }
  if (check.forbidden && check.forbidden.test(content)) {
    return [`${check.file}: ${check.message}`]
  }
  return []
})

if (failures.length > 0) {
  console.error('ISC card UI check failed:')
  failures.forEach(failure => console.error(`- ${failure}`))
  process.exit(1)
}

console.log('ISC card UI check passed.')
