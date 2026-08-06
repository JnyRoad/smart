const assert = require('assert');
const fs = require('fs');
const path = require('path');

const page = fs.readFileSync(path.join(__dirname, '../pages/page/service/leave/leave-apply.vue'), 'utf8');

assert(page.includes('storage.getSync(storage.PARK_ID)'), '离职提交必须读取当前受控园区');
assert(page.includes('this.applyInfo.parkId = Number.isInteger(currentParkId)'), '离职请求必须携带当前园区');
assert(page.includes("title: '请先选择园区'"), '未选择园区时必须给出可操作提示');
assert(page.includes("url: '/pages/page/home/address-list/address-list'"), '离职页必须能跳转到现有园区选择页');

console.log('leave apply current-park contract passed');
