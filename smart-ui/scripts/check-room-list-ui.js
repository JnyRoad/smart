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
    required: /<el-tree[\s\S]*class="my-menu-tree"[\s\S]*:data="treeData"[\s\S]*:filter-node-method="filterNode"[\s\S]*@node-click="handleNodeClick"/,
    message: 'room list must keep the dormitory tree, filter hook, and node click handler'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    required: /@click="searchSubmit\(searchForm\)"[\s\S]*@click="resetFrom\('searchForm'\)"[\s\S]*v-if="hasData"[\s\S]*@click="export2Excel"[\s\S]*@click="handleBatchEdit\(\)"[\s\S]*@click="handleSDBatchEdit\(\)"/,
    message: 'room list toolbar must keep search, reset, export, and batch edit entries'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    required: /<el-checkbox\s+:indeterminate="isIndeterminate"\s+v-model="checkAll"\s+@change="checkAllChange">全选<\/el-checkbox>[\s\S]*<el-checkbox-group\s+v-model="checkedRoom"\s+@change="roomChange"\s+class="room-list">/,
    message: 'room list must keep select-all and room multi-select wiring'
  },
  {
    file: 'src/views/platform/dormitory/room/list.vue',
    required: /<el-dropdown-item @click\.native="handleEdit\(item\)">编辑房间<\/el-dropdown-item>[\s\S]*<el-dropdown-item @click\.native="rowDel\(item\)">删除房间<\/el-dropdown-item>/,
    message: 'room dropdown must keep edit-room and delete-room actions'
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
      return /fetchRoomList\(\s*Object\.assign\(\s*\{[\s\S]*asc:\s*'room_name'[\s\S]*parkId:\s*this\.parkId[\s\S]*dormitoryId:\s*this\.dormitoryId[\s\S]*floorId:\s*this\.floorId/.test(block)
    },
    message: 'room list query must keep room-name ascending order and tree scope filters'
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
