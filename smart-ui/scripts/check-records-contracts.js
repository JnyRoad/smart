const fs = require('fs')
const path = require('path')
const assert = require('assert')

const rootDir = path.resolve(__dirname, '..')
function read(relativePath) {
  return fs.readFileSync(path.join(rootDir, relativePath), 'utf8')
}

function assertServiceType(source, label, value, message) {
  const tablePattern = new RegExp(`label:\\s*['"]${label}['"]\\s*,\\s*value:\\s*${value}`)
  const selectPattern = new RegExp(`<el-option\\s+label="${label}"\\s+value="${value}"><\\/el-option>`)
  assert(tablePattern.test(source) || selectPattern.test(source), message)
}

const personRecordFiles = [
  'src/const/crud/platform/records/person_isc.js',
  'src/views/platform/records/person_isc/index.vue',
  'src/const/crud/platform/records/person.js',
  'src/views/platform/records/person/index.vue'
]

for (const recordFile of personRecordFiles) {
  const source = read(recordFile)
  assertServiceType(source, '访客预约', 3, `${recordFile} must map serviceType 3 to 访客预约`)
  assertServiceType(source, '入厂申请', 6, `${recordFile} must map serviceType 6 to 入厂申请`)
}

const vehicleRecordFiles = [
  'src/const/crud/platform/records/vehicle.js',
  'src/views/platform/records/vehicle/index.vue'
]

for (const recordFile of vehicleRecordFiles) {
  const source = read(recordFile)
  assertServiceType(source, '访客预约', 4, `${recordFile} must map serviceType 4 to 访客预约`)
  assertServiceType(source, '入厂申请', 6, `${recordFile} must map serviceType 6 to 入厂申请`)
}
