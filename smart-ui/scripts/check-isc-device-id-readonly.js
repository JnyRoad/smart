const fs = require('fs')
const path = require('path')
const assert = require('assert')

const rootDir = path.resolve(__dirname, '..')

const devicePages = [
  'src/views/platform/device/attendance.vue',
  'src/views/platform/device/entrance_guard.vue',
  'src/views/platform/device/gate.vue'
]

function read(relativePath) {
  return fs.readFileSync(path.join(rootDir, relativePath), 'utf8')
}

function getEditFieldByProp(source, relativePath, prop) {
  const editDialogIndex = source.indexOf('title="编辑')
  assert(editDialogIndex >= 0, `${relativePath} must contain an edit dialog`)

  const propIndex = source.indexOf(`prop="${prop}"`, editDialogIndex)
  assert(propIndex >= 0, `${relativePath} edit dialog must contain ${prop} field`)

  const fieldStart = source.lastIndexOf('<el-form-item', propIndex)
  const fieldEnd = source.indexOf('</el-form-item>', propIndex)
  assert(fieldStart >= 0 && fieldEnd >= 0, `${relativePath} must wrap ${prop} in el-form-item`)

  return source.slice(fieldStart, fieldEnd + '</el-form-item>'.length)
}

for (const page of devicePages) {
  const source = read(page)
  const idField = getEditFieldByProp(source, page, 'id')
  assert(
    /v-if="isIscDevice\(editForm\)"/.test(idField),
    `${page} must show device table id only for ISC-synced edit devices`
  )
  assert(
    /label="设备ID"/.test(idField),
    `${page} must label ISC-synced edit id as 设备ID`
  )

  const idInput = idField.match(/<el-input\b[^>]*>/)
  assert(idInput, `${page} edit id field must render an input`)
  assert(
    /v-model="editForm\.id"/.test(idInput[0]),
    `${page} edit id input must bind editForm.id`
  )
  assert(
    /\sdisabled(\s|>|=)/.test(idInput[0]),
    `${page} ISC-synced edit id must be read-only`
  )

  const deviceCodeField = getEditFieldByProp(source, page, 'deviceCode')
  assert(
    /v-if="!isIscDevice\(editForm\)"/.test(deviceCodeField),
    `${page} must show deviceCode only for non-ISC edit devices`
  )
  assert(
    /label="设备序列号"/.test(deviceCodeField),
    `${page} must keep non-ISC edit deviceCode label as 设备序列号`
  )

  const input = deviceCodeField.match(/<el-input\b[^>]*>/)
  assert(input, `${page} edit deviceCode field must render an input`)
  assert(
    /v-model="editForm\.deviceCode"/.test(input[0]),
    `${page} edit deviceCode input must bind editForm.deviceCode`
  )
  assert(
    !/:disabled="isIscDevice\(editForm\)"/.test(input[0]),
    `${page} edit deviceCode must not stand in for the ISC device id`
  )
}
