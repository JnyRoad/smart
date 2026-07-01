# 设备卡片"所属权限组"查询扩展到三个设备页面 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把 `xc_guard/index.vue`（设备管理 → 门禁管理）已有的"所属权限组"只读查询按钮，原样复刻到 `gate.vue`（闸机管理）、`attendance.vue`（考勤机管理）、`entrance_guard.vue`（门禁机管理）三个设备页面。

**Architecture:** 纯前端改动，不涉及后端。三个文件各自在设备卡片按钮区加一个 `el-button`，在 `methods` 里加一个 `viewAuthorities(item)` 方法，调用已存在的 `xcGuardApi.getDecommissionPlan(deviceId)`（后端 `GET /platform/device/{deviceId}/decommission/plan`，按 `deviceId` 通用查询，不区分设备类型，已经构建部署过，本次不改）。

**Tech Stack:** Vue 2、Element UI（`$msgbox` + `$createElement`）、现有 `xcGuardApi`（`src/views/platform/device/xc_guard/_service.js`，三个目标文件已 import，无需新增依赖）。

## Global Constraints

- 三个页面的按钮文案、弹窗标题、列表文案、空数据文案、报错文案，逐字与 `xc_guard/index.vue` 保持一致，不做任何设备类型相关的措辞调整（spec 第 2 节）。
- 不改动 `smart-platform-biz` 后端代码或 `xc_guard/_service.js`（接口已通用且已部署）。
- 不新增前端单元测试（`.test.js`），与 `xc_guard/index.vue` 当初加这个按钮时的处理方式保持一致（spec 第 2 节）。
- 不修改 `gate.vue` 里已注释掉的重复按钮（第 101-103 行，历史遗留，与本次改动无关）。
- 每个文件的改动要匹配该文件已有的缩进风格（`gate.vue` 用 4 空格缩进 methods，`attendance.vue`/`entrance_guard.vue` 用 6 空格缩进 methods）。

---

## Task 1: gate.vue 加"所属权限组"

**Files:**
- Modify: `smart-ui/src/views/platform/device/gate.vue:97`（模板按钮区）
- Modify: `smart-ui/src/views/platform/device/gate.vue:777`（methods，`editLimit` 方法后）

**Interfaces:**
- Consumes：文件已有的 `import { xcGuardApi } from './xc_guard/_service'`（第 324 行，无需改动）；`this.$msgbox`、`this.$createElement`（Vue2/Element UI 全局可用，无需 import）。
- Produces：`viewAuthorities(item)` 方法，供模板按钮 `@click` 调用，`item` 为设备卡片对应的设备对象（含 `item.id`）。

- [ ] **Step 1: 在设备卡片按钮区加"所属权限组"按钮**

打开 `smart-ui/src/views/platform/device/gate.vue`，找到第 94-98 行：

```html
                          <div>
                            <el-button type="primary" @click="handleClear(item)" class="perm-btn" plain round>清空</el-button>
                            <el-button type="primary" @click="handleReissue(item)" class="perm-btn" plain round >重新下发</el-button>
                            <el-button type="primary" @click="permittedList(item)" class="perm-btn" plain round >通关人员</el-button>
                          </div>
```

改成：

```html
                          <div>
                            <el-button type="primary" @click="handleClear(item)" class="perm-btn" plain round>清空</el-button>
                            <el-button type="primary" @click="handleReissue(item)" class="perm-btn" plain round >重新下发</el-button>
                            <el-button type="primary" @click="permittedList(item)" class="perm-btn" plain round >通关人员</el-button>
                            <el-button type="primary" @click="viewAuthorities(item)" class="perm-btn" plain round >所属权限组</el-button>
                          </div>
```

- [ ] **Step 2: 在 methods 里加 `viewAuthorities` 方法**

找到第 766-778 行（`editLimit` 方法及其后的注释）：

```javascript
    /**
     * 更新设备权限
     */
    editLimit() {
      const src = `/platform/device/gate_limit`;
      this.$router.push({
        path: src,
        query: {
          deviceType: 1
        }
      });
    },
    /**
     * 搜索回调
     */
```

改成：

```javascript
    /**
     * 更新设备权限
     */
    editLimit() {
      const src = `/platform/device/gate_limit`;
      this.$router.push({
        path: src,
        query: {
          deviceType: 1
        }
      });
    },
    /**
     * 查看设备当前绑定的权限组（只读查询，不涉及任何删除/跳转副作用）
     */
    viewAuthorities(item) {
      var _this = this;
      xcGuardApi.getDecommissionPlan(item.id).then(response => {
        const plan = response.data.data || { affectedAuthorities: [] };
        const affected = plan.affectedAuthorities || [];
        const elm = _this.$createElement;
        let content;
        if (affected.length === 0) {
          content = elm("p", null, "该设备当前未绑定任何权限组。");
        } else {
          const listItems = affected.map(auth => elm("li", null,
            `${auth.authorityName}（${auth.staffCount} 名员工 / ${auth.vehicleCount} 辆车）`));
          // 注意：此处不能用 .smallInfo class（那是 76x76px 的图标占位框，会把文字列表裁成一个小图标框），
          // 沿用 xc_guard/index.vue 中已验证过的写法，传 null。
          content = elm("ul", null, listItems);
        }
        _this.$msgbox({
          title: "所属权限组",
          message: content,
          showCancelButton: false,
          confirmButtonText: "关闭",
          customClass: "small_dialog",
          center: true
        }).catch(reason => {
          // 纯只读提示弹窗，用户点击关闭/遮罩层/ESC 都会走到这里，不代表业务错误，仅记录原因用于排查
          console.error(reason);
        });
      }).catch(error => {
        console.error(error);
        _this.$message.error("查询失败，请稍后重试");
      });
    },
    /**
     * 搜索回调
     */
```

- [ ] **Step 3: Lint 检查**

Run: `pnpm run lint -- src/views/platform/device/gate.vue`
Expected: 无新增报错（原有历史 warning 不属于本次改动范围，忽略即可，只关注是否引入新的 error）。

- [ ] **Step 4: Commit**

```bash
git add smart-ui/src/views/platform/device/gate.vue
git commit -m "feat(smart-ui): add authority-group lookup button to gate device page"
```

---

## Task 2: attendance.vue 加"所属权限组"

**Files:**
- Modify: `smart-ui/src/views/platform/device/attendance.vue:79`（模板按钮区）
- Modify: `smart-ui/src/views/platform/device/attendance.vue:745`（methods，`editLimit` 方法后）

**Interfaces:**
- Consumes：文件已有的 `import { xcGuardApi } from './xc_guard/_service'`（第 291 行，无需改动）。
- Produces：`viewAuthorities(item)` 方法，与 Task 1 完全一致（供本文件模板按钮调用）。

- [ ] **Step 1: 在设备卡片按钮区加"所属权限组"按钮**

找到第 76-80 行：

```html
                          <div>
                            <el-button type="primary" @click="handleClear(item)" class="perm-btn" plain round>清空</el-button>
                            <el-button type="primary" @click="handleReissue(item)" class="perm-btn" plain round >重新下发</el-button>
                            <el-button type="primary" @click="permittedList(item)" class="perm-btn" plain round >通关人员</el-button>
                          </div>
```

改成：

```html
                          <div>
                            <el-button type="primary" @click="handleClear(item)" class="perm-btn" plain round>清空</el-button>
                            <el-button type="primary" @click="handleReissue(item)" class="perm-btn" plain round >重新下发</el-button>
                            <el-button type="primary" @click="permittedList(item)" class="perm-btn" plain round >通关人员</el-button>
                            <el-button type="primary" @click="viewAuthorities(item)" class="perm-btn" plain round >所属权限组</el-button>
                          </div>
```

- [ ] **Step 2: 在 methods 里加 `viewAuthorities` 方法**

找到第 734-746 行（注意本文件 methods 是 6 空格缩进，跟 gate.vue 的 4 空格不同）：

```javascript
      /**
       * 更新设备权限
       */
      editLimit() {
        const src = `/platform/device/gate_limit`;
        this.$router.push({
          path: src,
          query: {
            deviceType: 3
          }
        });
      },
      /**
       * 搜索回调
       */
```

改成：

```javascript
      /**
       * 更新设备权限
       */
      editLimit() {
        const src = `/platform/device/gate_limit`;
        this.$router.push({
          path: src,
          query: {
            deviceType: 3
          }
        });
      },
      /**
       * 查看设备当前绑定的权限组（只读查询，不涉及任何删除/跳转副作用）
       */
      viewAuthorities(item) {
        var _this = this;
        xcGuardApi.getDecommissionPlan(item.id).then(response => {
          const plan = response.data.data || { affectedAuthorities: [] };
          const affected = plan.affectedAuthorities || [];
          const elm = _this.$createElement;
          let content;
          if (affected.length === 0) {
            content = elm("p", null, "该设备当前未绑定任何权限组。");
          } else {
            const listItems = affected.map(auth => elm("li", null,
              `${auth.authorityName}（${auth.staffCount} 名员工 / ${auth.vehicleCount} 辆车）`));
            // 注意：此处不能用 .smallInfo class（那是 76x76px 的图标占位框，会把文字列表裁成一个小图标框），
            // 沿用 xc_guard/index.vue 中已验证过的写法，传 null。
            content = elm("ul", null, listItems);
          }
          _this.$msgbox({
            title: "所属权限组",
            message: content,
            showCancelButton: false,
            confirmButtonText: "关闭",
            customClass: "small_dialog",
            center: true
          }).catch(reason => {
            // 纯只读提示弹窗，用户点击关闭/遮罩层/ESC 都会走到这里，不代表业务错误，仅记录原因用于排查
            console.error(reason);
          });
        }).catch(error => {
          console.error(error);
          _this.$message.error("查询失败，请稍后重试");
        });
      },
      /**
       * 搜索回调
       */
```

- [ ] **Step 3: Lint 检查**

Run: `pnpm run lint -- src/views/platform/device/attendance.vue`
Expected: 无新增报错。

- [ ] **Step 4: Commit**

```bash
git add smart-ui/src/views/platform/device/attendance.vue
git commit -m "feat(smart-ui): add authority-group lookup button to attendance device page"
```

---

## Task 3: entrance_guard.vue 加"所属权限组"

**Files:**
- Modify: `smart-ui/src/views/platform/device/entrance_guard.vue:79`（模板按钮区）
- Modify: `smart-ui/src/views/platform/device/entrance_guard.vue:745`（methods，`editLimit` 方法后）

**Interfaces:**
- Consumes：文件已有的 `import { xcGuardApi } from './xc_guard/_service'`（第 291 行，无需改动）。
- Produces：`viewAuthorities(item)` 方法，与 Task 1/2 完全一致。

- [ ] **Step 1: 在设备卡片按钮区加"所属权限组"按钮**

找到第 76-80 行（与 attendance.vue 结构一致）：

```html
                          <div>
                            <el-button type="primary" @click="handleClear(item)" class="perm-btn" plain round>清空</el-button>
                            <el-button type="primary" @click="handleReissue(item)" class="perm-btn" plain round >重新下发</el-button>
                            <el-button type="primary" @click="permittedList(item)" class="perm-btn" plain round >通关人员</el-button>
                          </div>
```

改成：

```html
                          <div>
                            <el-button type="primary" @click="handleClear(item)" class="perm-btn" plain round>清空</el-button>
                            <el-button type="primary" @click="handleReissue(item)" class="perm-btn" plain round >重新下发</el-button>
                            <el-button type="primary" @click="permittedList(item)" class="perm-btn" plain round >通关人员</el-button>
                            <el-button type="primary" @click="viewAuthorities(item)" class="perm-btn" plain round >所属权限组</el-button>
                          </div>
```

- [ ] **Step 2: 在 methods 里加 `viewAuthorities` 方法**

找到第 734-746 行（6 空格缩进）：

```javascript
      /**
       * 更新设备权限
       */
      editLimit() {
        const src = `/platform/device/gate_limit`;
        this.$router.push({
          path: src,
          query: {
            deviceType: 4
          }
        });
      },
      /**
       * 搜索回调
       */
```

改成：

```javascript
      /**
       * 更新设备权限
       */
      editLimit() {
        const src = `/platform/device/gate_limit`;
        this.$router.push({
          path: src,
          query: {
            deviceType: 4
          }
        });
      },
      /**
       * 查看设备当前绑定的权限组（只读查询，不涉及任何删除/跳转副作用）
       */
      viewAuthorities(item) {
        var _this = this;
        xcGuardApi.getDecommissionPlan(item.id).then(response => {
          const plan = response.data.data || { affectedAuthorities: [] };
          const affected = plan.affectedAuthorities || [];
          const elm = _this.$createElement;
          let content;
          if (affected.length === 0) {
            content = elm("p", null, "该设备当前未绑定任何权限组。");
          } else {
            const listItems = affected.map(auth => elm("li", null,
              `${auth.authorityName}（${auth.staffCount} 名员工 / ${auth.vehicleCount} 辆车）`));
            // 注意：此处不能用 .smallInfo class（那是 76x76px 的图标占位框，会把文字列表裁成一个小图标框），
            // 沿用 xc_guard/index.vue 中已验证过的写法，传 null。
            content = elm("ul", null, listItems);
          }
          _this.$msgbox({
            title: "所属权限组",
            message: content,
            showCancelButton: false,
            confirmButtonText: "关闭",
            customClass: "small_dialog",
            center: true
          }).catch(reason => {
            // 纯只读提示弹窗，用户点击关闭/遮罩层/ESC 都会走到这里，不代表业务错误，仅记录原因用于排查
            console.error(reason);
          });
        }).catch(error => {
          console.error(error);
          _this.$message.error("查询失败，请稍后重试");
        });
      },
      /**
       * 搜索回调
       */
```

- [ ] **Step 3: Lint 检查**

Run: `pnpm run lint -- src/views/platform/device/entrance_guard.vue`
Expected: 无新增报错。

- [ ] **Step 4: Commit**

```bash
git add smart-ui/src/views/platform/device/entrance_guard.vue
git commit -m "feat(smart-ui): add authority-group lookup button to entrance guard device page"
```

---

## Task 4: 整体验证

**Files:** 无新增/修改文件，仅验证。

**Interfaces:** 无。

- [ ] **Step 1: 全量 lint**

Run: `pnpm lint`
Expected: 退出码 0（或只有改动前就存在的历史 warning，无新增 error）。

- [ ] **Step 2: 全量单测回归**

Run: `pnpm test`
Expected: 全部通过（本次没有新增测试文件，只是确认没有破坏现有测试）。

- [ ] **Step 3: 启动本地 dev server，浏览器实测三个页面**

Run: `pnpm dev`（或项目里配置的本地启动命令），依次打开：
- `/#/platform/device/gate`（闸机管理）
- `/#/platform/device/attendance`（考勤机管理）
- `/#/platform/device/entrance_guard`（门禁机管理）

对每个页面：
1. 找一台已绑定权限组的设备，点击"所属权限组"，确认弹窗标题为"所属权限组"，列表项格式为"权限组名（N 名员工 / N 辆车）"。
2. 找一台未绑定权限组的设备（或临时选一台），确认弹窗显示"该设备当前未绑定任何权限组。"
3. 确认按钮位置和视觉样式与"通关人员"等按钮一致（`plain round` 风格），弹窗不出现文字被裁切的问题（对应 spec 里提到的 `.smallInfo` 坑）。

- [ ] **Step 4: 生产构建**

Run: `pnpm build`
Expected: `BUILD  Build complete.`，`dist/` 产物更新（供后续走 PR 合并后重新发布到生产）。

- [ ] **Step 5: 最终 Commit（如验证过程中有微调）**

```bash
git add -A
git commit -m "test(smart-ui): verify authority-group lookup rollout across device pages"
```

（如验证阶段没有任何代码改动，跳过本步，不产生空提交。）
