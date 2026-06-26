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
    file: 'src/views/platform/dormitory/room/list.vue',
    custom: (content) => [
      /goBedMng/,
      /roomname:\s*roomName/,
      /\/platform\/dormitory\/bed_mng/
    ].every(pattern => !pattern.test(content)),
    message: 'room list must not keep dead bed management navigation wiring'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    custom: (content) => [
      /import \{ tableOption \} from '@\/const\/crud\/platform\/dormitory\/room'/,
      /tableOption:\s*tableOption/
    ].every(pattern => !pattern.test(content)),
    message: 'room list must not keep unused Avue tableOption wiring'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    custom: (content) => [
      /from '@\/util\/excel'/,
      /from 'vuex'/,
      /from 'echarts'/,
      /import echarts from "\.\/";/
    ].every(pattern => !pattern.test(content)),
    message: 'room list must not keep unused legacy imports'
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
    required: /<room-edit-dialog[\s\S]*ref="editForm"[\s\S]*:visible="editFormVisible"[\s\S]*:form="editForm"[\s\S]*:rules="editRules"[\s\S]*:is-dormitory-arr="isDormitoryArr"[\s\S]*:is-count-arr="isCountArr"[\s\S]*:park-dorm-type-list="parkDormTypeList"[\s\S]*:sd-temp-list="sdTempList"[\s\S]*:loading="editLoading"[\s\S]*@update-form-field="updateEditFormField"[\s\S]*@show-bed-num="showBedNum"[\s\S]*@get-bed-num="getBedNum"[\s\S]*@close="resetEditForm\('editForm'\)"[\s\S]*@submit="editSubmit\('editForm'\)"/,
    message: 'room list must keep edit room dialog props, ref, and submit/reset/change wiring'
  },
  {
    file: 'src/views/platform/dormitory/room/components/RoomEditDialog.vue',
    custom: (content) => {
      return [
        /<el-dialog/,
        /title="编辑房间"/,
        /width="600px"/,
        /:visible="visible"/,
        /@close="\$emit\('close'\)"/,
        /<el-form[\s\S]*ref="editForm"[\s\S]*:rules="rules"[\s\S]*:model="form"/,
        /label-width="120px"/,
        /prop="roomName"/,
        /:value="form\.roomName"/,
        /prop="isDormitoryRoom"/,
        /@input="\$emit\('update-form-field', \{ field: 'isDormitoryRoom', value: \$event \}\)"/,
        /@change="\$emit\('show-bed-num'\)"/,
        /prop="isCount"/,
        /@input="\$emit\('update-form-field', \{ field: 'isCount', value: \$event \}\)"/,
        /prop="roomType"/,
        /@input="\$emit\('update-form-field', \{ field: 'roomType', value: \$event \}\)"/,
        /@change="\$emit\('get-bed-num'\)"/,
        /prop="bedTotal"/,
        /:value="form\.bedTotal"/,
        /prop="roomSex"/,
        /@input="\$emit\('update-form-field', \{ field: 'roomSex', value: \$event \}\)"/,
        /prop="sdTemplateId"/,
        /@input="\$emit\('update-form-field', \{ field: 'sdTemplateId', value: \$event \}\)"/,
        /prop="leaveTempName"/,
        /:value="form\.leaveTempName"/,
        /@click="\$emit\('close'\)"/,
        /@click="\$emit\('submit'\)"/,
        /validate\(callback\)/,
        /this\.\$refs\.editForm\.validate\(callback\)/,
        /resetFields\(\)/,
        /this\.\$refs\.editForm\.resetFields\(\)/
      ].every(pattern => pattern.test(content))
    },
    message: 'room edit dialog must keep form fields, update events, change hooks, and ref proxy methods'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    required: /<room-batch-edit-dialog[\s\S]*ref="batchEditForm"[\s\S]*:title="editTitle"[\s\S]*:visible="batchEditFormVisible"[\s\S]*:form="batchEditForm"[\s\S]*:rules="batchEditRules"[\s\S]*:is-handel-s-d="isHandelSD"[\s\S]*:is-dormitory-arr="isDormitoryArr"[\s\S]*:is-count-arr="isCountArr"[\s\S]*:park-dorm-type-list="parkDormTypeList"[\s\S]*:sd-temp-list="sdTempList"[\s\S]*:loading="editLoading"[\s\S]*@update-form-field="updateBatchEditFormField"[\s\S]*@close="resetBatchEditForm\('batchEditForm'\)"[\s\S]*@submit="batchEditSubmit\('batchEditForm'\)"/,
    message: 'room list must keep batch edit dialog props, ref, conditional mode, and submit/reset wiring'
  },
  {
    file: 'src/views/platform/dormitory/room/components/RoomBatchEditDialog.vue',
    custom: (content) => {
      return [
        /<el-dialog/,
        /:title="title"/,
        /width="600px"/,
        /:visible="visible"/,
        /@close="\$emit\('close'\)"/,
        /<el-form[\s\S]*ref="batchEditForm"[\s\S]*:rules="rules"[\s\S]*:model="form"/,
        /label-width="120px"/,
        /v-if="!isHandelSD"[\s\S]*prop="isDormitoryRoom"/,
        /@input="\$emit\('update-form-field', \{ field: 'isDormitoryRoom', value: \$event \}\)"/,
        /v-if="!isHandelSD"[\s\S]*prop="isCount"/,
        /@input="\$emit\('update-form-field', \{ field: 'isCount', value: \$event \}\)"/,
        /v-if="!isHandelSD"[\s\S]*prop="roomType"/,
        /@input="\$emit\('update-form-field', \{ field: 'roomType', value: \$event \}\)"/,
        /v-if="!isHandelSD"[\s\S]*prop="roomSex"/,
        /@input="\$emit\('update-form-field', \{ field: 'roomSex', value: \$event \}\)"/,
        /v-if="isHandelSD"[\s\S]*prop="sdTemplateId"/,
        /@input="\$emit\('update-form-field', \{ field: 'sdTemplateId', value: \$event \}\)"/,
        /@click="\$emit\('close'\)"/,
        /@click="\$emit\('submit'\)"/,
        /validate\(callback\)/,
        /this\.\$refs\.batchEditForm\.validate\(callback\)/,
        /resetFields\(\)/,
        /this\.\$refs\.batchEditForm\.resetFields\(\)/
      ].every(pattern => pattern.test(content))
    },
    message: 'room batch edit dialog must keep conditional fields, update events, and ref proxy methods'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    required: /<room-floor-dialog[\s\S]*ref="floorForm"[\s\S]*:title="floorTitle"[\s\S]*:visible="floorVisible"[\s\S]*:form="floorForm"[\s\S]*:rules="editFloor \? floorEditRules : floorAddRules"[\s\S]*:edit-floor="editFloor"[\s\S]*:has-start-num="hasStartNum"[\s\S]*:loading="floorLoading"[\s\S]*@update-form-field="updateFloorFormField"[\s\S]*@close="resetFloorForm\('floorForm'\)"[\s\S]*@submit="floorSubmit\('floorForm'\)"/,
    message: 'room list must keep floor dialog props, ref, rule switch, and submit/reset wiring'
  },
  {
    file: 'src/views/platform/dormitory/room/components/RoomFloorDialog.vue',
    custom: (content) => {
      return [
        /<el-dialog/,
        /:title="title"/,
        /width="550px"/,
        /:visible="visible"/,
        /@close="\$emit\('close'\)"/,
        /<el-form[\s\S]*ref="floorForm"[\s\S]*:rules="rules"[\s\S]*:model="form"/,
        /label-width="80px"/,
        /<template v-if="editFloor">/,
        /prop="floorName"/,
        /:value="form\.floorName"/,
        /disabled/,
        /prop="roomNum"/,
        /@input="\$emit\('update-form-field', \{ field: 'roomNum', value: \$event \}\)"/,
        /<template v-else>/,
        /prop="startNum"/,
        /:disabled="hasStartNum"/,
        /@input="\$emit\('update-form-field', \{ field: 'startNum', value: \$event \}\)"/,
        /prop="floorNum"/,
        /@input="\$emit\('update-form-field', \{ field: 'floorNum', value: \$event \}\)"/,
        /@click="\$emit\('close'\)"/,
        /@click="\$emit\('submit'\)"/,
        /validate\(callback\)/,
        /this\.\$refs\.floorForm\.validate\(callback\)/,
        /resetFields\(\)/,
        /this\.\$refs\.floorForm\.resetFields\(\)/
      ].every(pattern => pattern.test(content))
    },
    message: 'room floor dialog must keep add/edit fields, update events, and ref proxy methods'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    required: /<room-dormitory-dialog[\s\S]*ref="dormForm"[\s\S]*:title="dormTitle"[\s\S]*:visible="dormVisible"[\s\S]*:form="dormForm"[\s\S]*:rules="dormRules"[\s\S]*:loading="floorLoading"[\s\S]*@update-form-field="updateDormFormField"[\s\S]*@close="resetDormForm\('dormForm'\)"[\s\S]*@submit="dormSubmit\('dormForm'\)"/,
    message: 'room list must keep dormitory dialog props, ref, and submit/reset wiring'
  },
  {
    file: 'src/views/platform/dormitory/room/components/RoomDormitoryDialog.vue',
    custom: (content) => {
      return [
        /<el-dialog/,
        /:title="title"/,
        /width="550px"/,
        /:visible="visible"/,
        /@close="\$emit\('close'\)"/,
        /<el-form[\s\S]*ref="dormForm"[\s\S]*:rules="rules"[\s\S]*:model="form"/,
        /label-position="left"/,
        /prop="dormitoryName"/,
        /宿舍楼名称/,
        /@input="\$emit\('update-form-field', \{ field: 'dormitoryName', value: \$event \}\)"/,
        /@click="\$emit\('close'\)"/,
        /@click="\$emit\('submit'\)"/,
        /validate\(callback\)/,
        /this\.\$refs\.dormForm\.validate\(callback\)/,
        /resetFields\(\)/,
        /this\.\$refs\.dormForm\.resetFields\(\)/
      ].every(pattern => pattern.test(content))
    },
    message: 'room dormitory dialog must keep form binding, field, events, and ref proxy methods'
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
