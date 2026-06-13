const fs = require('fs')
const path = require('path')
const assert = require('assert')

const rootDir = path.resolve(__dirname, '..')
const legacyHefeiSuffix = 'H' + 'f'
const legacyHefeiKebabSuffix = 'h' + 'f'
const legacyHefeiNames = [
  `visitorInfo${legacyHefeiSuffix}`,
  `index${legacyHefeiSuffix}`,
  `tel${legacyHefeiSuffix}`,
  `addPerson${legacyHefeiSuffix}`,
  `add-person-${legacyHefeiKebabSuffix}`,
  `id${legacyHefeiSuffix}`,
  `enumCauseApi${legacyHefeiSuffix}`
]
const legacyHefeiNamePattern = new RegExp(legacyHefeiNames.join('|'))
const permissionPath = path.join(rootDir, 'src/permission.js')
const permissionSource = fs.readFileSync(permissionPath, 'utf8')
const phoneInputPath = path.join(rootDir, 'src/components/tce-form/form-phone.vue')
const phoneInputSource = fs.readFileSync(phoneInputPath, 'utf8')
const visitorStepsPath = path.join(rootDir, 'src/views-mobile/pages/visitor/components/visitor-steps.vue')
const visitorStepsSource = fs.readFileSync(visitorStepsPath, 'utf8')
const visitorTelPath = path.join(rootDir, 'src/views-mobile/pages/visitor/tel.vue')
const visitorTelSource = fs.readFileSync(visitorTelPath, 'utf8')
const visitorTelHefeiPath = path.join(rootDir, 'src/views-mobile/pages/visitor/telHefei.vue')
assert(fs.existsSync(visitorTelHefeiPath), 'Hefei phone verification page file must be named telHefei.vue')
assert(!fs.existsSync(path.join(rootDir, `src/views-mobile/pages/visitor/tel${legacyHefeiSuffix}.vue`)), 'Hefei phone verification page must not keep the old abbreviated file name')
const visitorTelHefeiSource = fs.readFileSync(visitorTelHefeiPath, 'utf8')
const visitorIndexHefeiPath = path.join(rootDir, 'src/views-mobile/pages/visitor/indexHefei.vue')
assert(fs.existsSync(visitorIndexHefeiPath), 'Hefei visitor host info page file must be named indexHefei.vue')
assert(!fs.existsSync(path.join(rootDir, `src/views-mobile/pages/visitor/index${legacyHefeiSuffix}.vue`)), 'Hefei visitor host info page must not keep the old abbreviated file name')
const visitorIndexHefeiSource = fs.readFileSync(visitorIndexHefeiPath, 'utf8')
const visitorAddPersonHefeiPath = path.join(rootDir, 'src/views-mobile/pages/visitor/add-person-hefei.vue')
assert(fs.existsSync(visitorAddPersonHefeiPath), 'Hefei companion page file must be named add-person-hefei.vue')
assert(!fs.existsSync(path.join(rootDir, `src/views-mobile/pages/visitor/add-person-${legacyHefeiKebabSuffix}.vue`)), 'Hefei companion page must not keep the old abbreviated file name')
const visitorAddPersonHefeiSource = fs.readFileSync(visitorAddPersonHefeiPath, 'utf8')
const visitorAddPersonListPath = path.join(rootDir, 'src/views-mobile/pages/visitor/add-person-list.vue')
const visitorAddPersonListSource = fs.readFileSync(visitorAddPersonListPath, 'utf8')
const visitorInfoPath = path.join(rootDir, 'src/views-mobile/pages/visitor/visitorInfo.vue')
const visitorInfoSource = fs.readFileSync(visitorInfoPath, 'utf8')
const visitorInfoHefeiPath = path.join(rootDir, 'src/views-mobile/pages/visitor/visitorInfoHefei.vue')
assert(fs.existsSync(visitorInfoHefeiPath), 'Hefei visitor info page file must be named visitorInfoHefei.vue')
assert(
  !fs.existsSync(path.join(rootDir, `src/views-mobile/pages/visitor/visitorInfo${legacyHefeiSuffix}.vue`)),
  'Hefei visitor info page must not keep the old abbreviated file name'
)
const visitorInfoHefeiSource = fs.readFileSync(visitorInfoHefeiPath, 'utf8')
const visitorRouterPath = path.join(rootDir, 'src/router/pages/visitor.js')
const visitorRouterSource = fs.readFileSync(visitorRouterPath, 'utf8')
const visitorServicePath = path.join(rootDir, 'src/services/visitor.js')
const visitorServiceSource = fs.readFileSync(visitorServicePath, 'utf8')
const formTimePickerPath = path.join(rootDir, 'src/components/tce-form/form-time-picker.vue')
const formTimePickerSource = fs.readFileSync(formTimePickerPath, 'utf8')
const globalClassPath = path.join(rootDir, 'src/sass/global-class.scss')
const globalClassSource = fs.readFileSync(globalClassPath, 'utf8')
const pageBottomPath = path.join(rootDir, 'src/views-mobile/components/page3-bottom.vue')
const pageBottomSource = fs.readFileSync(pageBottomPath, 'utf8')

require('./check-visitor-area-selection')

assert(
  /to\.meta\.label\s*\|\|\s*to\.meta\.lable/.test(permissionSource),
  'permission.js must support both meta.label and legacy meta.lable when setting document.title'
)

assert(
  !/type="number"\s+maxlength="11"/.test(phoneInputSource),
  'phone input must not use type="number" with maxlength because browsers ignore maxlength for number inputs'
)

assert(
  !/right:\s*(o|none)\s*;/.test(visitorStepsSource),
  'visitor steps styles must not contain invalid right values'
)

for (const [fileName, source] of [
  ['visitor/tel.vue', visitorTelSource],
  ['visitor/telHefei.vue', visitorTelHefeiSource]
]) {
  assert(
    /type="tel"\s+inputmode="numeric"\s+maxlength="11"/.test(source),
    `${fileName} visitor phone input must use tel keyboard and maxlength`
  )
  assert(
    /inputType:\s*'tel'/.test(source),
    `${fileName} sms code input must use tel keyboard`
  )
}

assert(
  /safe-area-inset-bottom/.test(globalClassSource) && /safe-area-inset-bottom/.test(pageBottomSource),
  'fixed bottom actions must reserve iOS safe-area-inset-bottom'
)

for (const [fileName, source] of [
  ['visitorInfo.vue', visitorInfoSource],
  ['visitorInfoHefei.vue', visitorInfoHefeiSource]
]) {
  for (const [field, valueData] of [
    ['startTime', 'visitorInfo.startTime'],
    ['endTime', 'visitorInfo.endTime']
  ]) {
    assert(
      new RegExp(`field="${field}"[\\s\\S]*:valueData="${valueData}"[\\s\\S]*type="time-picker"`).test(source),
      `${fileName} ${field} time picker must bind ${valueData} so cached visit times are restored after returning from companion pages`
    )
  }
  assert(
    /visitorInfo:\s*\{[\s\S]*startTime:\s*''[\s\S]*endTime:\s*''/.test(source),
    `${fileName} must initialize blank reactive visit time fields`
  )
  assert(
    /this\.visitorInfo\s*=\s*Object\.assign\(\s*\{\s*startTime:\s*'',\s*endTime:\s*''\s*\},\s*obj\s*\)/.test(source),
    `${fileName} must preserve blank reactive visit time fields when loading cached visitorInfo`
  )
  assert(
    !/即使是读缓存，开始时间都是当前时间/.test(source),
    `${fileName} must not reset cached visit times when returning to visitor info`
  )
  assert(
    !/visitorInfo\.startTime\s*=\s*dateFormat\(new Date\(\)/.test(source),
    `${fileName} must preserve the exact selected startTime from storage`
  )
  assert(
    /startTimeHandle\(time\)\s*\{[\s\S]*this\.clearEndTime\(\)/.test(source),
    `${fileName} must clear leave time after selecting a visit start time so users choose it manually`
  )
  assert(
    /clearEndTime\(\)\s*\{[\s\S]*this\.visitorInfo\.endTime\s*=\s*''/.test(source),
    `${fileName} must provide an explicit clearEndTime method`
  )
  assert(
    !/setEndTime/.test(source),
    `${fileName} must not synthesize a leave time from the visit start time`
  )
}

assert(
  /visitorInfoHefei/.test(visitorRouterSource) && !new RegExp(`visitorInfo${legacyHefeiSuffix}'\\)`).test(visitorRouterSource),
  'visitor router must import the renamed visitorInfoHefei.vue component'
)

assert(
  /path:\s*'visitorInfoHefei'/.test(visitorRouterSource),
  'visitor router must expose a clear visitorInfoHefei route for the Hefei visitor info page'
)

assert(
  /path:\s*'indexHefei'[\s\S]*import\('@views-mobile\/pages\/visitor\/indexHefei'\)/.test(visitorRouterSource),
  'visitor router must expose indexHefei for the Hefei host info page'
)

assert(
  /path:\s*'telHefei'[\s\S]*import\('@views-mobile\/pages\/visitor\/telHefei'\)/.test(visitorRouterSource),
  'visitor router must expose telHefei for the Hefei phone verification page'
)

assert(
  /path:\s*'addPersonHefei'[\s\S]*import\('@views-mobile\/pages\/visitor\/add-person-hefei'\)/.test(visitorRouterSource),
  'visitor router must expose addPersonHefei for the Hefei companion page'
)

for (const [fileName, source] of [
  ['visitor/add-person-list.vue', visitorAddPersonListSource],
  ['visitor/indexHefei.vue', visitorIndexHefeiSource],
  ['visitor/add-person-hefei.vue', visitorAddPersonHefeiSource],
  ['visitor/visitorInfoHefei.vue', visitorInfoHefeiSource],
  ['services/visitor.js', visitorServiceSource],
  ['router/pages/visitor.js', visitorRouterSource]
]) {
  assert(
    !legacyHefeiNamePattern.test(source),
    `${fileName} must use clear Hefei names instead of old abbreviations`
  )
}

assert(
  /add\(\)\s*\{[\s\S]*path:\s*'\/xuchang\/visitor\/addPersonHefei'[\s\S]*idHefei:\s*true[\s\S]*cause:\s*this\.\$route\.query\.cause/.test(visitorAddPersonListSource),
  'Hefei companion list add() must preserve idHefei when entering the Hefei companion form'
)

assert(
  /edit\(item,\s*index\)\s*\{[\s\S]*path:\s*'\/xuchang\/visitor\/addPersonHefei'[\s\S]*itemInfo:\s*JSON\.stringify\(item\)[\s\S]*itemIndex:\s*index[\s\S]*isEdit:\s*true[\s\S]*idHefei:\s*true[\s\S]*cause:\s*this\.\$route\.query\.cause/.test(visitorAddPersonListSource),
  'Hefei companion list edit() must preserve idHefei when entering the Hefei companion form'
)

assert(
  /path:\s*'\/xuchang\/visitor\/addPersonList'[\s\S]*idHefei:\s*true[\s\S]*cause:\s*this\.cause/.test(visitorAddPersonHefeiSource),
  'Hefei companion form must preserve idHefei and cause when returning to the companion list'
)

assert(
  /valueData\s*\(val\)\s*\{[\s\S]*val === ''[\s\S]*this\.formData = ''/.test(formTimePickerSource),
  'time picker must clear displayed formData when parent valueData is reset to an empty string'
)

assert(
  /height:\s*rem\(160\);/.test(pageBottomSource) &&
    /constant\(safe-area-inset-bottom\)/.test(pageBottomSource) &&
    /env\(safe-area-inset-bottom\)/.test(pageBottomSource),
  'page3-bottom must keep a base height and support both constant() and env() safe-area fallbacks'
)
