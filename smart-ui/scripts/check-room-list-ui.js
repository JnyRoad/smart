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
    file: 'src/views/platform/dormitory/room/list.vue',
    required: /<room-tree-panel[\s\S]*:tree-data="treeData"[\s\S]*:default-props="defaultProps"[\s\S]*@node-click="handleNodeClick"[\s\S]*@node-action="treeNodeOption"/,
    message: 'room list must keep the dormitory tree props and event wiring'
  },
  {
    file: 'src/views/platform/dormitory/room/components/RoomTreePanel.vue',
    custom: (content) => [
      /<el-tree/,
      /class="my-menu-tree"/,
      /:data="treeData"/,
      /:filter-node-method="filterNode"/,
      /@node-click="emitNodeClick"/
    ].every(pattern => pattern.test(content)),
    message: 'room tree panel must keep the Element tree, filter hook, and node click handler'
  },
  {
    file: 'src/views/platform/dormitory/room/components/RoomTreePanel.vue',
    custom: (content) => [
      /emitNodeAction\(data,\s*'EDI',\s*node\)/,
      /emitNodeAction\(data,\s*'DEL',\s*node\)/,
      /emitNodeAction\(data,\s*'APP',\s*node\)/,
      /新增楼栋/,
      /新增楼层/,
      /选择楼栋及楼层/,
      /输入关键字进行过滤/
    ].every(pattern => pattern.test(content)),
    message: 'room tree panel must keep tree action labels and emitted actions'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    required: /<room-search-toolbar[\s\S]*:search-form="searchForm"[\s\S]*:all-dorm-type-list="allDormTypeList"[\s\S]*:has-data="hasData"[\s\S]*:export-loading="exportLoading"[\s\S]*@update-search-field="updateSearchField"[\s\S]*@search="searchSubmit\(searchForm\)"[\s\S]*@reset="resetFrom"[\s\S]*@export="export2Excel"[\s\S]*@batch-edit="handleBatchEdit"[\s\S]*@sd-batch-edit="handleSDBatchEdit"/,
    message: 'room list must keep search toolbar props and event wiring'
  },
  {
    file: 'src/views/platform/dormitory/room/components/RoomSearchToolbar.vue',
    custom: (content) => [
      /@click="\$emit\('search'\)"/,
      /@click="\$emit\('reset'\)"/,
      /v-if="hasData"/,
      /@click="\$emit\('export'\)"/,
      /@click="\$emit\('batch-edit'\)"/,
      /@click="\$emit\('sd-batch-edit'\)"/,
      /class="topForm"/,
      /label="是否参与分配"/,
      /label="是否参与计算"/,
      /label="宿舍分类"/,
      /label="房间属性"/
    ].every(pattern => pattern.test(content)),
    message: 'room search toolbar must keep search fields and action buttons'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    required: /<room-grid-panel[\s\S]*:has-data="hasData"[\s\S]*:table-data="tableData"[\s\S]*:checked-room="checkedRoom"[\s\S]*:check-all="checkAll"[\s\S]*:is-indeterminate="isIndeterminate"[\s\S]*@update-checked-room="checkedRoom = \$event"[\s\S]*@update-check-all="checkAll = \$event"[\s\S]*@check-all-change="checkAllChange"[\s\S]*@room-change="roomChange"[\s\S]*@edit-room="handleEdit"[\s\S]*@delete-room="rowDel"/,
    message: 'room list must keep room grid props and event wiring'
  },
  {
    file: 'src/views/platform/dormitory/room/components/RoomGridPanel.vue',
    custom: (content) => [
      /class="block1 block2 room-grid-panel"/,
      /<el-checkbox[\s\S]*:indeterminate="isIndeterminate"[\s\S]*:value="checkAll"[\s\S]*@input="emitCheckAllInput"[\s\S]*@change="emitCheckAllChange"/,
      /<el-checkbox-group[\s\S]*:value="checkedRoom"[\s\S]*class="room-list"[\s\S]*@input="emitCheckedRoomInput"[\s\S]*@change="emitRoomChange"/,
      /v-for="\(\s*item,\s*index\s*\)\s+in\s+tableData"/,
      /:label="item\.id"/,
      /emitEditRoom\(item\)/,
      /emitDeleteRoom\(item\)/
    ].every(pattern => pattern.test(content)),
    message: 'room grid panel must keep select-all, room multi-select, and row action wiring'
  },
  {
    file: 'src/views/platform/dormitory/room/components/RoomGridPanel.vue',
    custom: (content) => [
      /全选/,
      /男宿/,
      /女宿/,
      /夫妻\/混住/,
      /不参与分配/,
      /编辑房间/,
      /删除房间/,
      /当前条件下暂无住宿信息（请选择具体楼层）/
    ].every(pattern => pattern.test(content)),
    message: 'room grid panel must keep room list legend, row action labels, and empty state'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    required: /<el-dialog title="编辑房间"[\s\S]*:visible\.sync="editFormVisible"[\s\S]*@click="resetEditForm\('editForm'\)"[\s\S]*@click="editSubmit\('editForm'\)"/,
    message: 'edit room dialog must keep visible state and submit/reset handlers'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    required: /<el-dialog :title="editTitle"[\s\S]*:visible\.sync="batchEditFormVisible"[\s\S]*@click="resetBatchEditForm\('batchEditForm'\)"[\s\S]*@click="batchEditSubmit\('batchEditForm'\)"/,
    message: 'batch edit dialog must keep visible state and submit/reset handlers'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    required: /<el-dialog :title="floorTitle"[\s\S]*:visible\.sync="floorVisible"[\s\S]*@click="resetFloorForm\('floorForm'\)"[\s\S]*@click="floorSubmit\('floorForm'\)"/,
    message: 'floor dialog must keep visible state and submit/reset handlers'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    required: /<el-dialog :title="dormTitle"[\s\S]*:visible\.sync="dormVisible"[\s\S]*@click="resetDormForm\('dormForm'\)"[\s\S]*@click="dormSubmit\('dormForm'\)"/,
    message: 'dormitory dialog must keep visible state and submit/reset handlers'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    custom: (content) => {
      const start = content.indexOf('getList(params)')
      const end = content.indexOf('rowDel(row)', start)
      const block = start >= 0 && end >= 0 ? content.slice(start, end) : ''
      return /fetchRoomList\(\s*buildRoomListQuery\(\s*\{[\s\S]*parkId:\s*this\.parkId[\s\S]*dormitoryId:\s*this\.dormitoryId[\s\S]*floorId:\s*this\.floorId[\s\S]*\},\s*params\s*\)/.test(block)
    },
    message: 'room list query must keep room-name ascending order and tree scope filters'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    custom: (content) => {
      const start = content.indexOf('resetFrom()')
      const end = content.indexOf('getList(params)', start)
      const block = start >= 0 && end >= 0 ? content.slice(start, end) : ''
      return /this\.getList\(\)/.test(block) && /this\.\$refs\.searchToolbar\.resetFields\(\)/.test(block)
    },
    message: 'room list reset must keep querying and clearing the toolbar form'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    required: /putSDBatchObj\(this\.batchEditForm\)[\s\S]*putBatchObj\(this\.batchEditForm\)/,
    message: 'batch submit must keep separate sd-template and room-attribute update APIs'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    required: /import \{ fetchRoomList,\s*floorList,\s*delObj,\s*putObj,\s*putBatchObj,\s*putSDBatchObj,\s*dormTypeApi,\s*allDormitoryType,\s*getBedNum,\s*fetchSDTempList \} from '@\/api\/platform\/dormitory\/room'/,
    message: 'room list must keep existing room API dependency surface'
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
  console.error('Room list UI check failed:')
  failures.forEach(failure => console.error(`- ${failure}`))
  process.exit(1)
}

console.log('Room list UI check passed.')
