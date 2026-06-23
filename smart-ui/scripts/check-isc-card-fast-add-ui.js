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

const checks = [
  {
    file: 'src/router/platform/index.js',
    forbidden: /path:\s*['"]\/platform\/basic\/isc_card_fast_add(?:\/index)?['"]/,
    message: 'ISC card fast-add page must not bypass role menu permissions through a static platform route'
  },
  {
    file: 'src/api/platform/basic/staff_info.js',
    required: /getStaffByBadge[\s\S]*\/platform\/staff\/define\/badge/,
    message: 'staff API must expose exact badge lookup for fast card binding'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /ISC卡片快速维护/,
    message: 'page title must match the dedicated ISC card fast-add function'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /先选择园区和员工，再刷卡或批量粘贴卡号；确认无误后提交，系统会自动同步到ISC。/,
    message: 'page description must explain the user workflow in concise user-facing copy'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    forbidden: /保存后沿用现有人员卡片链路自动生成ISC同步任务/,
    message: 'page description must not expose implementation details to users'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /读卡器[\s\S]*<queue-table[\s\S]*:rows="queue"[\s\S]*@submit="submitQueue"[\s\S]*<paste-dialog|<queue-table[\s\S]*:rows="queue"[\s\S]*@submit="submitQueue"[\s\S]*读卡器[\s\S]*<paste-dialog/,
    message: 'page must mount scanner input, queue table, and batch paste workflow'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /<staff-panel[\s\S]*:selected-staff="selectedStaff"[\s\S]*:staff-candidates="staffCandidates"[\s\S]*:staff-cards="staffCards"[\s\S]*@select-staff="selectStaff"[\s\S]*@open-detail="goStaffDetail"[\s\S]*@remove-card="removeStaffCard"/,
    message: 'page must delegate current staff, candidate, and staff card display to StaffPanel without dropping parent actions'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/StaffPanel.vue',
    required: /当前录入[\s\S]*先输入工号或姓名定位员工[\s\S]*匹配人员[\s\S]*已有ISC卡片[\s\S]*ISC平台[\s\S]*\$emit\('remove-card',\s*scope\.row\)/,
    message: 'staff panel component must keep selected staff, candidate, existing card, ISC platform, and delete action UI'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/QueueTable.vue',
    required: /待提交队列[\s\S]*共\{\{ rows\.length \}\}条，可提交\{\{ readyCount \}\}条，异常\{\{ invalidCount \}\}条[\s\S]*卡号 \/ 结果[\s\S]*清除成功行[\s\S]*清空队列[\s\S]*提交队列/,
    message: 'queue table component must keep queue summary, result column, and footer actions'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /<el-form-item label="园区" prop="parkId">/,
    message: 'park selector must be labeled as park only'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /:disabled="!selectedPark \|\| staffLoading"[\s\S]*@click="searchStaff"/,
    message: 'header staff search button must be disabled until a park is selected'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /<el-input[\s\S]*v-model\.trim="searchForm\.staffKeyword"[\s\S]*:disabled="!selectedPark"[\s\S]*@keyup\.enter\.native="searchStaff"/,
    message: 'staff keyword input must be disabled until a park is selected'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /<el-button slot="append" :loading="staffLoading" :disabled="!selectedPark" @click="searchStaff">搜索<\/el-button>/,
    message: 'staff keyword append search button must be disabled until a park is selected'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /:disabled="!selectedPark"[\s\S]*@click="openPasteDialog"/,
    message: 'batch paste entry must be disabled until a park is selected'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /:class="\{ danger: !selectedPark \|\| \(selectedPark && !isParkSyncEnabled\(selectedPark\)\) \}"/,
    message: 'park status tip must be red when no park is selected or the selected park is not enabled'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /<i :class="!selectedPark \|\| \(selectedPark && !isParkSyncEnabled\(selectedPark\)\) \? 'el-icon-warning' : 'el-icon-success'"><\/i>/,
    message: 'park status tip must show a warning icon before a park is selected'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /placeholder="请选择园区"/,
    message: 'park selector placeholder must ask for park only'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/flow-rules.js',
    required: /function parkOptionLabel\(item\)[\s\S]*return parkName/,
    message: 'park option label must not append ISC platform names'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /<el-button slot="append" :loading="staffLoading" :disabled="!selectedPark" @click="searchStaff">搜索<\/el-button>/,
    message: 'staff keyword append button must use visible search text'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /staff-keyword-input[\s\S]*width:\s*320px[\s\S]*el-input-group__append[\s\S]*width:\s*84px[\s\S]*overflow:\s*hidden[\s\S]*\.el-button[\s\S]*min-width:\s*84px[\s\S]*margin:\s*0/,
    message: 'staff keyword search append button must keep a stable wider hit area without hover overflow'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    forbidden: /园区\s*\/\s*ISC平台|请选择已启用ISC卡片同步的园区/,
    message: 'park selector and empty-state copy must not repeat ISC platform wording'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    forbidden: /park-option-status|:disabled="!isParkSyncEnabled\(item\)"/,
    message: 'park dropdown must list only enabled parks without enabled/disabled status labels'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/api.js',
    required: /(?=[\s\S]*fetchList as fetchStaffList)(?=[\s\S]*fetchIscStaffCards)(?=[\s\S]*saveIscStaffCard)(?=[\s\S]*deleteIscStaffCard)(?=[\s\S]*fetchIscParkConfigs)/,
    message: 'page must reuse existing staff search, card list, card save, card delete, and park config APIs'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /loadIscParkOptions\(\)[\s\S]*records\.filter\(item => this\.isParkSyncEnabled\(item\)\)/,
    message: 'park dropdown data source must include enabled ISC parks only'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/api.js',
    required: /createStaffSearchQuery\(fieldName,\s*keyword,\s*park\)[\s\S]*query\.parkId = park\.parkId[\s\S]*searchStaffByBadge\(badge,\s*park\)[\s\S]*createStaffSearchQuery\('badges',\s*badge,\s*park\)/,
    message: 'badge search must use exact badge-list lookup scoped to the selected park instead of fuzzy paging auto-select'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /searchStaff\(\)\s*\{[\s\S]*if\s*\(!this\.selectedPark\)\s*\{[\s\S]*请先选择园区[\s\S]*return[\s\S]*const keyword/,
    message: 'staff search method must reject searches before park selection'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /onParkChange\(\)\s*\{[\s\S]*this\.selectedStaff\s*=\s*null[\s\S]*this\.staffCandidates\s*=\s*\[\][\s\S]*this\.staffCards\s*=\s*\[\]/,
    message: 'changing park must clear selected staff, candidates, and visible staff cards'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /loadStaffCards\(staffId\)\s*\{[\s\S]*const requestStaffId = staffId[\s\S]*fetchStaffCardRecords\(requestStaffId\)[\s\S]*if\s*\(!this\.selectedStaff \|\| this\.selectedStaff\.id !== requestStaffId\)\s*\{[\s\S]*return[\s\S]*this\.staffCards = cards/,
    message: 'staff card loading must ignore stale async responses after staff or park changes'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /exactMatches\s*=\s*records\.filter[\s\S]*exactMatches\.length\s*===\s*1[\s\S]*selectStaff\(exactMatches\[0\]\)[\s\S]*selectedStaff\s*=\s*null/,
    message: 'name search must auto-select only a single exact match and require manual selection for ambiguous results'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    forbidden: /selectStaff\(exact\s*\|\|\s*records\[0\]\)/,
    message: 'name search must not default to the first fuzzy staff result'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/flow-rules.js',
    required: /\[0-9A-Z\]\{8,20\}[\s\S]*startsWith\(['"]999['"]\)|startsWith\(['"]999['"]\)[\s\S]*\[0-9A-Z\]\{8,20\}/,
    message: 'page must enforce ISC card number format and reject 999 virtual cards'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /8-20位数字或大写字母/,
    message: 'page must tell users the Hikvision card number rule'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    forbidden: /\\d\{1,31\}|1-31位数字/,
    message: 'page must not keep the old 1-31 digit-only card rule'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /path:\s*['"]\/platform\/records\/isc_card_task['"]/,
    message: 'page must link to the existing ISC card sync task menu route'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    forbidden: /\/platform\/records\/isc_card_task\/index/,
    message: 'page must not navigate to the missing ISC card sync task /index route'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /removeStaffCard\(row\)[\s\S]*deleteStaffCard\(row\.id\)[\s\S]*loadStaffCards\(this\.selectedStaff\.id\)[\s\S]*loadTaskList\(\)/,
    message: 'existing staff card table must support deleting a card and refreshing cards/tasks'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/StaffPanel.vue',
    required: /label="同步状态"[\s\S]*cardSyncStatusType\(scope\.row\.syncStatus\)[\s\S]*cardSyncStatusText\(scope\.row\)/,
    message: 'existing staff card table must show card sync status'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/flow-rules.js',
    required: /cardSyncStatusText\(row\)[\s\S]*待同步[\s\S]*已同步[\s\S]*同步失败[\s\S]*本地取消[\s\S]*cardSyncStatusType\(syncStatus\)/,
    message: 'page must render all known staff card sync statuses'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /pasteResolving\s*=\s*true[\s\S]*try\s*\{[\s\S]*finally\s*\{[\s\S]*pasteResolving\s*=\s*false/,
    message: 'batch paste staff resolving must reset loading state with try/finally'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    forbidden: /&#10;|&amp;#10;/,
    message: 'batch paste textarea placeholder must not show escaped newline entities'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /:placeholder="pastePlaceholder"[\s\S]*pastePlaceholder\(\)[\s\S]*10288 1024388812\\n10290 1024388845/,
    message: 'batch paste textarea must use a real multiline placeholder string'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/PasteDialog.vue',
    required: /<el-input(?=[\s\S]*class="paste-input")(?=[\s\S]*:disabled="resolving")(?=[\s\S]*:placeholder="placeholder")/,
    message: 'batch paste textarea must be locked while resolving pasted staff rows'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/PasteDialog.vue',
    required: /custom-class="isc-paste-dialog"[\s\S]*class="paste-guide"[\s\S]*class="paste-example"/,
    message: 'batch paste dialog must use a polished guide and example layout'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/PasteDialog.vue',
    required: /:close-on-click-modal="!resolving"[\s\S]*:close-on-press-escape="!resolving"[\s\S]*:show-close="!resolving"/,
    message: 'batch paste dialog must not be closable while resolving pasted rows'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/PasteDialog.vue',
    required: /class="paste-summary"[\s\S]*paste-status[\s\S]*errors\.length[\s\S]*格式校验通过/,
    message: 'batch paste dialog must show a visible live validation status'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /pasteVisibleErrors\(\)[\s\S]*this\.pasteErrors\.slice\(0,\s*5\)/,
    message: 'batch paste dialog must cap visible inline errors to keep the dialog readable'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /parts\.length !== 2[\s\S]*每行只能填写工号和卡号/,
    message: 'batch paste parser must reject extra fields instead of silently dropping them'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/PasteDialog.vue',
    required: /:disabled="!canSubmit \|\| resolving"[\s\S]*canSubmit\(\)[\s\S]*this\.rows\.length > 0 && !this\.hasVisibleProblem/,
    message: 'batch paste confirm action must be disabled while format errors are visible'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /const rows = this\.pasteRows\.slice\(\)[\s\S]*fetchStaffMap\(rows\.map[\s\S]*rows\.forEach/,
    message: 'batch paste confirm must use a stable row snapshot across async staff resolving'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/api.js',
    required: /fetchStaffMapByBadges\(badges\)[\s\S]*badges:\s*uniqueBadges\.join\(' '\)/,
    message: 'batch paste staff lookup must send whitespace-separated badges because the backend splits by blank characters'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    forbidden: /badges:\s*uniqueBadges\.join\(','\)/,
    message: 'batch paste staff lookup must not join badges with commas'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /const pastePark = this\.selectedPark[\s\S]*this\.buildQueueRow\(staff,\s*item\.cardNo,\s*pastePark\)/,
    message: 'batch paste confirm must use a stable park snapshot across async staff resolving'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /<\/style>\s*<style lang="scss">[\s\S]*\.isc-paste-dialog[\s\S]*\.paste-guide[\s\S]*\.paste-summary[\s\S]*\.paste-errors/,
    message: 'batch paste dialog styles must be global under custom dialog class because the dialog is appended to body'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/QueueTable.vue',
    required: /:disabled="submitting"[\s\S]*remove-finished[\s\S]*:disabled="submitting"[\s\S]*\$emit\('clear'\)/,
    message: 'queue clear actions must be disabled while submitting cards'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/QueueTable.vue',
    required: /:disabled="submitting \|\| scope\.row\.status === 'saving'"[\s\S]*\$emit\('remove-row',\s*scope\.\$index\)/,
    message: 'queue row remove action must be disabled while submitting cards'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /removeQueueRow\(index\)\s*\{[\s\S]*if\s*\(this\.submitting\)[\s\S]*return[\s\S]*clearQueue\(\)\s*\{[\s\S]*if\s*\(this\.submitting\)[\s\S]*return[\s\S]*removeFinishedRows\(\)\s*\{[\s\S]*if\s*\(this\.submitting\)[\s\S]*return/,
    message: 'queue remove and clear methods must guard against submit-in-progress state'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /rowsToSubmit\s*=\s*this\.readyQueue\.slice\(\)[\s\S]*failedCount\s*=\s*0[\s\S]*for\s*\(const row of rowsToSubmit\)[\s\S]*失败\$\{failedCount\}条/,
    message: 'submit summary must count only the current submit snapshot'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    required: /row\.message\s*=\s*this\.responseMessage\(response,\s*'保存失败'\)/,
    message: 'queue save failure must show backend business error message instead of generic failure text'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/flow-rules.js',
    required: /responseMessage\(response,\s*fallback\)[\s\S]*responseData\.msg[\s\S]*responseData\.message/,
    message: 'page must parse backend response msg/message from non-throwing business errors'
  },
  {
    file: 'src/views/platform/basic/isc_card_fast_add/index.vue',
    forbidden: /this\.queue\.filter\(item => item\.status === 'failed'\)\.length/,
    message: 'submit summary must not include historical failed rows'
  }
]

const failures = []

checks.forEach(check => {
  try {
    const content = readRequired(check.file)
    if (check.required && !check.required.test(content)) {
      failures.push(`${check.file}: ${check.message}`)
    }
    if (check.forbidden && check.forbidden.test(content)) {
      failures.push(`${check.file}: ${check.message}`)
    }
  } catch (error) {
    failures.push(error.message)
  }
})

try {
  const page = readRequired('src/views/platform/basic/isc_card_fast_add/index.vue')
  const pageApi = readRequired('src/views/platform/basic/isc_card_fast_add/api.js')
  const loadTaskListStart = page.indexOf('loadTaskList()')
  const loadTaskListEnd = page.indexOf('parkOptionLabel', loadTaskListStart)
  const loadTaskListBlock = loadTaskListStart >= 0 && loadTaskListEnd >= 0 ? page.slice(loadTaskListStart, loadTaskListEnd) : ''
  if (!loadTaskListBlock || !/fetchRecentCardTaskRecords\(\{[\s\S]*parkId:\s*this\.searchForm\.parkId[\s\S]*badge:\s*this\.selectedStaff && this\.selectedStaff\.badge/.test(loadTaskListBlock)) {
    failures.push('src/views/platform/basic/isc_card_fast_add/index.vue: recent task loader is missing')
  }
  if (!/fetchRecentCardTaskRecords\(\{ parkId,\s*badge \} = \{\}\)[\s\S]*fetchIscCardTaskList\(query\)/.test(pageApi)) {
    failures.push('src/views/platform/basic/isc_card_fast_add/api.js: recent task service must call the existing ISC card task API')
  }
  if (/action:\s*[12]/.test(loadTaskListBlock) || /action:\s*[12]/.test(pageApi)) {
    failures.push('src/views/platform/basic/isc_card_fast_add/index.vue: recent task loader must not hard-code card task action because delete tasks should be visible too')
  }

  const staffPanel = readRequired('src/views/platform/basic/isc_card_fast_add/StaffPanel.vue')
  const cardListStart = staffPanel.indexOf('已有ISC卡片')
  const cardListEnd = staffPanel.indexOf('</el-table>', cardListStart)
  const cardListTable = cardListStart >= 0 && cardListEnd >= 0 ? staffPanel.slice(cardListStart, cardListEnd) : ''
  if (!cardListTable) {
    failures.push('src/views/platform/basic/isc_card_fast_add/StaffPanel.vue: existing card table is missing')
  }
  if (/prop="parkName"\s+label="园区"/.test(cardListTable)) {
    failures.push('src/views/platform/basic/isc_card_fast_add/StaffPanel.vue: existing card table must not repeat the park column')
  }
  if (!/prop="dispatcherParkName"\s+label="ISC平台"[\s\S]*\$emit\('remove-card',\s*scope\.row\)/.test(cardListTable)) {
    failures.push('src/views/platform/basic/isc_card_fast_add/StaffPanel.vue: existing card table must keep ISC platform and expose delete action')
  }

  const nameSearchStart = page.indexOf('searchStaffByName(keyword)')
  const nameSearchEnd = page.indexOf('searchExactStaffByBadge', nameSearchStart)
  const nameSearchBlock = nameSearchStart >= 0 && nameSearchEnd >= 0 ? page.slice(nameSearchStart, nameSearchEnd) : ''
  if (!nameSearchBlock) {
    failures.push('src/views/platform/basic/isc_card_fast_add/index.vue: name staff search method is missing')
  }
  if (/selectStaff\(records\[0\]\)/.test(nameSearchBlock)) {
    failures.push('src/views/platform/basic/isc_card_fast_add/index.vue: name search must not default to the first fuzzy staff result')
  }
} catch (error) {
  failures.push(error.message)
}

if (failures.length > 0) {
  console.error('ISC card fast-add UI check failed:')
  failures.forEach(failure => console.error(`- ${failure}`))
  process.exit(1)
}

console.log('ISC card fast-add UI check passed.')
