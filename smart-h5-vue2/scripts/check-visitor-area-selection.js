const fs = require('fs')
const path = require('path')
const assert = require('assert')

const rootDir = path.resolve(__dirname, '..')
const serviceSource = fs.readFileSync(path.join(rootDir, 'src/services/visitor.js'), 'utf8')
const visitorInfoSource = fs.readFileSync(path.join(rootDir, 'src/views-mobile/pages/visitor/visitorInfo.vue'), 'utf8')
const addAreaTypeSource = fs.readFileSync(path.join(rootDir, 'src/views-mobile/pages/visitor/addAreaType.vue'), 'utf8')
const telSource = fs.readFileSync(path.join(rootDir, 'src/views-mobile/pages/visitor/tel.vue'), 'utf8')

const factorySectionStart = visitorInfoSource.indexOf('label="区域类型"')
const factorySectionEnd = visitorInfoSource.indexOf('label="授权区域"')
const factorySection = visitorInfoSource.slice(factorySectionStart, factorySectionEnd)

assert(
  /export const getAreaOptions/.test(serviceSource) &&
    /\/admittance\/apply\/app\/area-options/.test(serviceSource),
  'visitor service must expose GET /admittance/apply/app/area-options'
)

assert(
  factorySectionStart !== -1 && factorySectionEnd !== -1 && !/type="picker"/.test(factorySection),
  'visitorInfo 区域类型 must not use picker'
)

assert(
  /factory-select-inline/.test(visitorInfoSource) && /selectFactoryType/.test(visitorInfoSource),
  'visitorInfo must render inline factory selection and switch immediately'
)

assert(
  /area-chip/.test(visitorInfoSource) &&
    /toggleInlineArea/.test(visitorInfoSource) &&
    /更多区域/.test(visitorInfoSource),
  'visitorInfo must render inline area chips and keep the more area entry'
)

assert(
  /visitorAreaOptions_/.test(visitorInfoSource) && /visitorAreaOptions_/.test(addAreaTypeSource),
  'visitor area options cache key must be shared by visitorInfo and addAreaType'
)

assert(
  /getAreaOptions/.test(visitorInfoSource) &&
    /getAreaType/.test(visitorInfoSource) &&
    /getAreaOptions/.test(addAreaTypeSource) &&
    /getAreaType/.test(addAreaTypeSource),
  'visitor pages must use area-options with getAreaType fallback'
)

assert(
  /handleUnavailableAreaOptions/.test(visitorInfoSource) &&
    visitorInfoSource.indexOf('this.handleUnavailableAreaOptions()') < visitorInfoSource.indexOf('await this.loadLegacyAreaOptions()'),
  'visitorInfo must fail closed instead of legacy fallback when area-options returns empty factories'
)

assert(
  /searchValue/.test(addAreaTypeSource) && /toggleAllAreas/.test(addAreaTypeSource),
  'addAreaType must support search and full selection'
)

assert(
  /const factory = this\.findFactoryByType\(areaOptions\.factories\)/.test(addAreaTypeSource) &&
    !/String\(item\.factoryType\) === String\(this\.factoryType\) \|\| String\(item\.areaFlag\) === String\(this\.type\)/.test(addAreaTypeSource),
  'addAreaType must match backend factoryType before falling back to legacy areaFlag'
)

assert(
  /canLoadLegacyAreaType\(\) \{\s*return !this\.normalizeAreaCode\(this\.factoryType\)\s*\}/.test(addAreaTypeSource) &&
    /if \(this\.canLoadLegacyAreaType\(\)\) \{\s*await this\.loadLegacyAreaTypeList\(\)\s*\}/.test(addAreaTypeSource),
  'addAreaType must not use legacy areaFlag fallback when backend factoryType is present'
)

assert(
  /handleUnavailableAreaOptions/.test(addAreaTypeSource) &&
    addAreaTypeSource.indexOf('this.handleUnavailableAreaOptions()') < addAreaTypeSource.indexOf('if (this.canLoadLegacyAreaType())'),
  'addAreaType must fail closed instead of legacy fallback when area-options returns empty factories'
)

assert(
  /newAreaTypeList/.test(visitorInfoSource) &&
    /oldAreaTypeList/.test(visitorInfoSource) &&
    /newAreaTypeList/.test(addAreaTypeSource) &&
    /oldAreaTypeList/.test(addAreaTypeSource),
  'visitor area selection must keep newAreaTypeList/oldAreaTypeList localStorage contracts'
)

assert(
  /areaTypeByFactory/.test(visitorInfoSource) &&
    /getAreaTypeByFactory/.test(visitorInfoSource) &&
    /visitorAreaTypeByFactory_/.test(visitorInfoSource) &&
    /visitorAreaTypeByFactory_/.test(addAreaTypeSource),
  'visitor area selection must store selected areas by backend factoryType'
)

assert(
  !/permitFactoryType\s*!==\s*['"]1[56]['"]/.test(visitorInfoSource) &&
    !/String\(this\.permitFactoryType\)\s*===\s*['"]16['"]/.test(visitorInfoSource) &&
    !/pruneAreaTypeObj\(this\.newAreaTypeObj,\s*['"]15['"]\)/.test(visitorInfoSource) &&
    !/pruneAreaTypeObj\(this\.oldAreaTypeObj,\s*['"]16['"]\)/.test(visitorInfoSource),
  'visitorInfo main selection flow must not branch on hard-coded factoryType 15/16'
)

assert(
  /pruneSelectedAreaCodes/.test(visitorInfoSource) &&
    /getFactoryAreaValueSet/.test(visitorInfoSource) &&
    /pruneCheckList/.test(addAreaTypeSource) &&
    /getAvailableAreaValues/.test(addAreaTypeSource),
  'visitor area selection must prune stale localStorage area codes against current options'
)

assert(
  /getAreaOptions/.test(telSource) &&
    /getAreaType/.test(telSource) &&
    /pruneVisitorAreaTypesBeforeSubmit/.test(telSource) &&
    /await this\.pruneVisitorAreaTypesBeforeSubmit\(\)/.test(telSource),
  'tel submit page must prune stale areaType values before final applySaveApi'
)

assert(
  /const cachedAreaOptions = this\.loadAreaOptionsCache\(\)/.test(telSource) &&
    /return cachedAreaOptions/.test(telSource) &&
    telSource.indexOf('return cachedAreaOptions') < telSource.indexOf('const legacyAreaOptions = await this.loadLegacyAreaOptions()'),
  'tel submit page must use cached dynamic area options before legacy fallback'
)

assert(
  /if \(areaOptions\.factories\.length === 0\) \{\s*throw new Error\('授权区域配置不可用，请返回重试'\)\s*\}/.test(telSource) &&
    telSource.indexOf("throw new Error('授权区域配置不可用，请返回重试')") < telSource.indexOf('return cachedAreaOptions'),
  'tel submit page must fail closed instead of cached or legacy fallback when area-options returns empty factories'
)

assert(
  /const canSubmitAreaType = await this\.pruneVisitorAreaTypesBeforeSubmit\(\)/.test(telSource) &&
    /if \(!canSubmitAreaType\)/.test(telSource) &&
    /this\.visitorInfo\.areaType\.length === 0/.test(telSource),
  'tel submit page must stop submission when areaType is empty after pruning'
)

assert(
  /const factoriesForSubmit = permitFactoryType\s*\?\s*activeFactories\s*:\s*factories/.test(telSource),
  'tel submit page must fail closed when permitFactoryType has no matching factory options'
)

assert(
  /this\.visitorInfo\.areaType = normalizedAreaType\.filter\(\(code\) => valueSet\[code\]\)/.test(telSource),
  'tel submit page must clear areaType when no valid submit area options are available'
)

assert(
  /flex-wrap:\s*wrap/.test(visitorInfoSource) &&
    /flex-wrap:\s*wrap/.test(addAreaTypeSource) &&
    /(word-break|overflow-wrap)/.test(visitorInfoSource) &&
    /(word-break|overflow-wrap)/.test(addAreaTypeSource),
  'factory buttons and area chips must wrap on small screens without horizontal overflow'
)
