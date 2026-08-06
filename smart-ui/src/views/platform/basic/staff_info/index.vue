<!--基础信息：员工信息 -->
<template>
  <div class="my-basic-container staff">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu clear" style="min-height: 48px">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom('searchForm')" plain>清空</el-button>
            <el-button type="primary" :loading="exportLoading" @click="export2Excel" icon>导出员工信息</el-button>
            <el-button type="primary" class="importBtn" :loading="selectLoading" @click="importImg">导入员工照片</el-button>
            <input class="fileBtn" ref="uploadBtn" type="file" multiple @change="myChange($event)" />
            <el-button type="primary" @click="getDeviceAuthList" :disabled="!hasSelect">通关权限</el-button>
            <el-button type="primary" @click="getAppAuthList" :disabled="!hasSelect">APP权限</el-button>
            <el-button type="primary" @click="issueAuth" :disabled="!hasSelect">重新下发</el-button>
          </div>
        </div>
        <tce-Search-bar>
          <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
            <el-form-item label="工号" prop="badge">
              <!-- <el-input v-model="searchForm.badge" placeholder="工号" clearable></el-input> -->
              <div class="badgeTemplate" @click="pasteBadge">
                <span v-if="badgeArry.length === 0" style="color: rgb(192, 196, 204)">点击批量粘贴工号</span>
                <div style="padding: 2px 0" v-else>
                  <el-tag type="info">{{ badgeArry[0] }}</el-tag>
                  <el-tag type="info" v-if="badgeArry.length > 1" style="margin-left: 4px">+ {{ badgeArry.length - 1 }}</el-tag>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="姓名" prop="name">
              <el-input v-model="searchForm.name" placeholder="姓名" clearable></el-input>
            </el-form-item>
            <el-form-item label="所属园区/BU/部门" prop="depIds">
              <el-cascader expand-trigger="hover" :options="compOptions" :show-all-levels="false" :change-on-select="true" v-model="depIds" clearable></el-cascader>
            </el-form-item>
            <el-form-item label="中心" prop="depAbbr">
              <el-input v-model="searchForm.depAbbr" placeholder="中心" clearable></el-input>
            </el-form-item>
            <el-form-item label="岗位" prop="jobName">
              <el-input v-model="searchForm.jobName" placeholder="岗位" clearable></el-input>
            </el-form-item>
            <el-form-item label="职层" prop="jcheId">
              <jcheSelect v-model="searchForm.jcheId"></jcheSelect>
            </el-form-item>
            <el-form-item label="员工状态" prop="status">
              <el-select v-model="searchForm.status" placeholder="员工状态" clearable>
                <el-option v-for="item in staffStatusData" :key="item.value" :label="item.label" :value="item.value"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="是否有照片" prop="hasFace">
              <el-select v-model="searchForm.hasFace" placeholder="是否有照片" clearable>
                <el-option label="是" :value="true"></el-option>
                <el-option label="否" :value="false"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="入职时间" prop="timeSlot">
              <el-date-picker
                v-model="searchForm.timeSlot"
                type="datetimerange"
                range-separator="-"
                value-format="yyyy-MM-dd HH:mm:ss"
                :default-time="['00:00:00', '23:59:59']"
                start-placeholder="起始时间"
                end-placeholder="截止时间"
                clearable
              ></el-date-picker>
            </el-form-item>
          </el-form>
        </tce-Search-bar>
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          :option="tableOption"
          @size-change="sizeChange"
          @current-change="currentChange"
          @selection-change="selectChange"
        >
          <template slot-scope="scope" slot="status">
            <span>{{ scope.row.status | staffStatusInit }}</span>
          </template>
          <template slot-scope="scope" slot="hasFace">
            <span>{{ scope.row.hasFace ? '是' : '否' }}</span>
          </template>
          <template slot-scope="scope" slot="deviceAuth">
            <span>{{ scope.row.deviceAuth || '未分配' }}</span>
          </template>
          <template slot-scope="scope" slot="appAuth">
            <span>{{ scope.row.appAuth || '未分配' }}</span>
          </template>
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-view" @click="handleDetail(scope.row, scope.$index)">员工详情</el-button>
            <el-button type="text" icon="el-icon-view" :disabled="!scope.row.deviceAuth" @click="staffAuth(scope.row, scope.$index)">查看通关权限</el-button>
          </template>
        </avue-crud>
        <el-dialog title="批量导入图片" @close="importCancel" class="dialog_form" width="600px" :visible.sync="importVisible">
          <div class="importCont">
            <p>操作提示：</p>
            <p>1.照片名称以工号为命名。</p>
            <p>2.请核对一下要导入的照片，避免重复覆盖。</p>
            <p>3.一次性导入数量，不超过2000张，避免造成系统压力。</p>
            <div>
              本次共选择
              <span class="num">{{ totalImgNum }}</span
              >个员工照片， 其中 <span class="num red">{{ noneStaffs.length }}</span
              >个照片的工号不存在
              <template v-if="noneStaffs.length > 0">
                ，请检查以下工号是否正确！
                <span class="num red">
                  （
                  <template v-for="(item, index) in noneStaffs">
                    {{ item.staffBadge }}
                    <template v-if="index + 1 != noneStaffs.length">，</template>
                  </template>
                  ）
                </span>
              </template>
            </div>
            <el-scrollbar style="height: 400px">
              <table class="fileTb">
                <tr>
                  <td>工号</td>
                  <td>姓名</td>
                  <td>是否存在</td>
                  <td>是否有照片</td>
                  <td>操作</td>
                </tr>
                <tr v-for="(item, index) in listInfo" :key="index" :class="{ haveNo: item.status == 2 }">
                    <td>{{ item.staffBadge }}</td>
                    <td>{{ item.staffName || '-' }}</td>
                    <td>{{ item.status == 0 ? '是' : '否' }}</td>
                    <td>{{ item.status == 0 ? '是' : '否' }}</td>
                    <td>
                      <el-button type="text" @click="delImg(item.staffBadge)">移除</el-button>
                    </td>
                  </tr>
                <template v-if="listInfo.length == 0">
                  <tr>
                    <td colspan="5">还未选择图片文件</td>
                  </tr>
                </template>
              </table>
            </el-scrollbar>
          </div>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="importCancel" plain>取 消</el-button>
            <el-button type="primary" @click="myUpload()" :loading="importLoading" :disabled="listInfo.length == 0">确 定</el-button>
          </div>
        </el-dialog>
        <el-dialog
          title="通关权限分配"
          @close="resetEntry('entryForm')"
          class="dialog_form auth-dialog mac-style"
          width="700px"
          :visible.sync="entryFormVisible">

          <!-- 已选员工信息 -->
          <div class="selected-staff-info">
            <div class="info-content">
              <i class="el-icon-user"></i>
              <span>已选择 {{ selectStaffs.length }} 名员工</span>
              <div class="staff-tags">
                <el-tag
                  v-for="staff in selectStaffs.slice(0, 3)"
                  :key="staff.id"
                  size="small"
                  effect="plain">
                  {{ staff.badge }} - {{ staff.name }}
                </el-tag>
                <el-tag
                  v-if="selectStaffs.length > 3"
                  size="small"
                  type="info"
                  effect="plain">
                  +{{ selectStaffs.length - 3 }}
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 权限选择区域 -->
          <div class="auth-container">
            <!-- 左侧：待选权限 -->
            <div class="auth-panel">
              <div class="panel-header">
                <span class="panel-title">待选权限</span>
                <el-input
                  v-model="authSearchKey"
                  placeholder="搜索权限"
                  prefix-icon="el-icon-search"
                  size="small"
                  clearable>
                </el-input>
              </div>

              <div class="panel-content">
                <el-checkbox-group v-model="tempSelectedAuth">
                  <el-checkbox
                    v-for="item in filteredAuthList"
                    :key="item.id"
                    :label="item.id"
                    :disabled="selectedAuth.includes(item.id)"
                    class="auth-checkbox"
                    role="checkbox"
                    :aria-label="item.authorityName">
                    <div class="auth-item-content">
                      <span class="auth-name">{{ item.authorityName }}</span>
                      <el-tooltip
                        v-if="item.description"
                        :content="item.description"
                        placement="top">
                        <i class="el-icon-info"></i>
                      </el-tooltip>
                    </div>
                  </el-checkbox>
                </el-checkbox-group>
              </div>
            </div>

            <!-- 中间操作按钮 -->
            <div class="auth-operations">
              <div class="operations-center">
                <el-button
                  type="primary"
                  size="small"
                  :disabled="!tempSelectedAuth.length"
                  @click="addAuth">
                  <i class="el-icon-arrow-right"></i>
                </el-button>
                <el-button
                  type="primary"
                  size="small"
                  :disabled="!selectedAuth.length"
                  @click="removeAuth">
                  <i class="el-icon-arrow-left"></i>
                </el-button>
              </div>
            </div>

            <!-- 右侧：已选权限 -->
            <div class="auth-panel">
              <div class="panel-header">
                <span class="panel-title">已选权限 ({{ selectedAuth.length }})</span>
                <el-button
                  type="text"
                  @click="clearSelected"
                  :disabled="!selectedAuth.length">
                  清空
                </el-button>
              </div>

              <div class="panel-content">
                <el-checkbox-group v-model="tempRemovedAuth">
                  <el-checkbox
                    v-for="id in selectedAuth"
                    :key="id"
                    :label="id"
                    class="auth-checkbox"
                    role="checkbox"
                    :aria-label="getAuthName(id)">
                    <span>{{ getAuthName(id) }}</span>
                  </el-checkbox>
                </el-checkbox-group>
              </div>
            </div>
          </div>

          <!-- 分配方式 -->
          <div class="assign-type">
            <el-radio-group v-model="addType" size="small">
              <el-radio :label="1">追加权限</el-radio>
              <el-radio :label="2">覆盖权限</el-radio>
            </el-radio-group>
          </div>

          <div slot="footer" class="dialog-footer">
            <el-button @click="entryFormClose()" size="small" plain>取 消</el-button>
            <el-button
              type="primary"
              @click="entrySubmit()"
              size="small"
              :loading="entryLoading"
              :disabled="!selectedAuth.length">
              确 定
            </el-button>
          </div>
        </el-dialog>
        <el-dialog title="APP权限分配" @close="resetApp('appForm')" class="dialog_form" width="400px" :visible.sync="appFormVisible">
          <el-form ref="appForm" :model="appForm" :rules="appRules">
            <el-form-item label="请选择APP权限策略" prop="authId">
              <el-select v-model="appForm.authId" placeholder="请选择" filterable multiple>
                <el-option v-for="item in appList" :key="item.id" :label="item.authName" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="appFormVisible = false" plain>取 消</el-button>
            <el-button type="primary" @click="appSubmit('appForm')" :loading="appLoading">确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
    <DoPasteDialogs ref="DoPasteDialogs" :dataItem="badges" @refresh="badgeChange" />
    <IssueAuthDialogs ref="IssueAuthDialogs" :dataItem="taskRecordStr" @refresh="entryFormClose" />
  </div>
</template>

<script>
import { fetchList, getStaffImgInfo, importImgs, fetchAppList, editAppList, deviceAuthList, upDeviceAuthList } from '@/api/platform/basic/staff_info'
import { getCompTree } from '@/api/platform/_publicService'
import { tableOption } from '@/const/crud/platform/basic/staff_info'
import { enumStaffStatus } from '@/const/crud/platform/enum'
import { mapGetters } from 'vuex'
import { isArrayFn } from '@/util/util'
import { staffStatusInit } from '@/filters/index'
import DoPasteDialogs from './doPasteBadge'
import IssueAuthDialogs from './issueAuth'
// issueAuth

export default {
  name: 'staff',
  components: {
    DoPasteDialogs,
    IssueAuthDialogs
  },
  data() {
    return {
      exportLoading: false,
      myFiles: null,
      totalImgNum: 0, //本次共选择了几项
      noneStaffs: [], //不存在的工号集合
      fileNames: [], //名称集合
      listInfo: [], //根据名称集合查询的图片信息
      uploadInfo: [], //要上传的文件列表
      importVisible: false,
      selectLoading: false,
      importLoading: false,
      searchForm: {
        //搜索菜单表单
        compId: undefined,
        depId: undefined,
        jcheId: undefined,
        jobName: undefined,
        name: undefined,
        badges: undefined,
        status: undefined,
        hasFace: undefined,
        timeSlot: undefined,
        startTime: undefined,
        endTime: undefined
      },
      staffStatusData: enumStaffStatus,
      entryLoading: false,
      appLoading: false,
      entryFormVisible: false,
      appFormVisible: false,
      entryList: [],
      appList: [],
      entryForm: {
        ids: [],
        deviceAuthIds: undefined //通关权限策略
      },
      addType: 1,
      appForm: {
        staffId: [], //多个员工id
        authId: undefined //APP权限策略
      },
      entryRules: {
        deviceAuthIds: [{ required: true, message: '请选择通关权限策略', trigger: 'change' }]
      },
      appRules: {
        authId: [{ required: true, message: '请选择APP权限策略', trigger: 'change' }]
      },
      selectStaffs: [], //当前选中员工的集合
      hasSelect: false,
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      depIds: [],
      compOptions: [],
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      badges: '',
      badgeArry: [],
      taskRecordStr: '',
      authSearchKey: '', // 搜索关键字
      selectedAuth: [], // 已选择的权限ID数组
      tempSelectedAuth: [], // 临时选中的待选权限
      tempRemovedAuth: [], // 临时选中的已选权限
    }
  },
  created() {
    getCompTree().then((response) => {
      this.compOptions = response.data.data
    })
    this.$nextTick(() => {
      if (this.$route.query.queryForm != undefined) {
        let queryPage = this.$route.query.queryPage
        let queryForm = this.$route.query.queryForm
        if (queryPage && queryPage.constructor === Object) {
          this.page = Object.assign(queryPage, {})
        }
        if (queryForm && queryForm.constructor === Object) {
          this.searchForm = Object.assign(queryForm, {})
        }
        this.getList(this.page, this.searchForm)
      } else {
        this.getList(this.page)
      }
    })
  },
  mounted: function () {},
  computed: {
    ...mapGetters(['permissions']),
    filteredAuthList() {
      if (!this.authSearchKey) return this.entryList;
      const key = this.authSearchKey.toLowerCase();
      return this.entryList.filter(item =>
        item.authorityName.toLowerCase().includes(key) ||
        (item.description && item.description.toLowerCase().includes(key))
      );
    }
  },
  watch: {
    selectStaffs: function (val) {
      val.length > 0 ? (this.hasSelect = true) : (this.hasSelect = false)
    },
    depIds(newVal, oldVal) {
      if (isArrayFn(newVal) && newVal.length > 0) {
        const depLength = newVal.length
        if (depLength == 3) {
          this.searchForm.depId = this.depIds[2]
          this.searchForm.compId = this.depIds[1]
          this.searchForm.parkId = this.depIds[0]
        } else if (depLength >= 2) {
          this.searchForm.depId = undefined
          this.searchForm.compId = this.depIds[1]
          this.searchForm.parkId = this.depIds[0]
        } else if (depLength >= 1) {
          this.searchForm.depId = undefined
          this.searchForm.compId = undefined
          this.searchForm.parkId = this.depIds[0]
        }
      } else {
        this.searchForm.depId = undefined
        this.searchForm.compId = undefined
        this.searchForm.parkId = undefined
      }
    }
  },
  methods: {
    pasteBadge() {
      this.$refs.DoPasteDialogs && this.$refs.DoPasteDialogs.open()
    },
    badgeChange(val) {
      this.badges = val
      let arr = val.split(/[\s\n,]/)
      this.badgeArry = arr.filter((el) => {
        return el != ''
      })
    },
    parkChange() {
      this.searchForm.compId = undefined
      this.searchForm.depId = undefined
    },
    buChange() {
      this.searchForm.depId = undefined
    },
    readAndPreview(file) {
      //将对象转为base64和文件名称的组合
      let _this = this
      let noneStaffs = this.noneStaffs
      var namePosition = file.name.lastIndexOf('.')
      var badge = namePosition > 0 ? file.name.substring(0, namePosition) : file.name

      if (noneStaffs.length > 0) {
        for (var i = 0; i < noneStaffs.length; i++) {
          if (noneStaffs[i].staffBadge == badge) {
            return
          }
        }
      }

      var reader = new FileReader()
      reader.addEventListener(
        'load',
        function () {
          let originalImgCode = this.result //含 base64 头的
          let baseIndex = originalImgCode.indexOf(',') + 1
          let baseImg = originalImgCode.slice(baseIndex)

          _this.uploadInfo.push({ staffBadge: badge, facePic: baseImg })
        },
        false
      )
      reader.readAsDataURL(file)
    },
    myChange(e) {
      this.$message.closeAll()
      let files = e.target.files
      this.myFiles = files
      let errorSuffix = [] //所选文件非指定规范格式(jpg)的图片 的集合 ,用来提示
      let errorSize = [] //所选文件非指定规范大小(60~200k)的图片 的集合 ,用来提示
      let arrName = [] //图片名称集合，用来查询是否存在图片信息
      let arrFile = [] //所选文件信息集合，上传使用
      this.totalImgNum = files.length

      if (files.length > 0) {
        if (files.length < 2000) {
          //单次导入，数量不能超过200张
          this.selectLoading = true
          for (var i = 0; i < files.length; i++) {
            var suffixPosition = files[i].name.lastIndexOf('.') + 1
            var nums = files[i].name.length - suffixPosition
            var suffix = files[i].name.substr(suffixPosition, nums) //文件后缀

            var namePosition = files[i].name.lastIndexOf('.')
            var badge = files[i].name.substr(0, namePosition) //文件名字

            //找出格式不规范的文件集合
            const isImg = suffix == 'jpg' || suffix == 'JPG' || suffix == 'jpeg' || suffix == 'JPEG'
            if (!isImg) {
              //判断所选文件格式
              errorSuffix.push(badge)
            }

            //找出大小不规范的文件集合
            const fileSize = files[i].size / 1024
            if (fileSize < 20 || fileSize > 200) {
              errorSize.push(badge)
            }

            arrName.push({ staffBadge: badge })
          }
          if (errorSuffix.length > 0) {
            this.selectLoading = false
            let str =
              '<div class="tips" style="max-width: 500px; word-break:break-all;line-height:20px;""><p>共选择了 ' +
              this.totalImgNum +
              ' 个文件，有 ' +
              errorSuffix.length +
              ' 个文件格式不符合规范</p>'
            str += '<p>照片仅支持 jpg 一种格式，请检查以下文件格式再重新导入！</p><p style="color:red">'

            for (var i = 0; i < errorSuffix.length; i++) {
              str += errorSuffix[i] + '，'
            }

            var dotPosition = str.lastIndexOf('，')
            str = str.substr(0, dotPosition)
            str += '</p></div>'

            this.$message({
              dangerouslyUseHTMLString: true,
              showClose: true,
              message: str,
              type: 'warning',
              duration: 0
            })
          } else if (errorSize.length > 0) {
            this.selectLoading = false
            let str =
              '<div class="tips" style="max-width: 500px; word-break:break-all;line-height:20px;""><p>共选择了 ' +
              this.totalImgNum +
              ' 个文件，有 ' +
              errorSize.length +
              ' 个文件小不符合规范</p>'
            str += '<p>照片大小应大于20kb小于200KB，请检查以下文件大小再重新导入！</p><p style="color:red">'

            for (var i = 0; i < errorSize.length; i++) {
              str += errorSize[i] + '，'
            }

            var dotPosition = str.lastIndexOf('，')
            str = str.substr(0, dotPosition)
            str += '</p></div>'

            this.$message({
              dangerouslyUseHTMLString: true,
              showClose: true,
              message: str,
              type: 'warning',
              duration: 0
            })
          } else {
            getStaffImgInfo({ facePicUpLoad: arrName })
              .then((response) => {
                if (!this.validatenull(response.data.data)) {
                  var result = response.data.data
                  for (var i = 0; i < result.length; i++) {
                    if (result[i].status == 2) {
                      this.noneStaffs.push(result[i])
                    } else {
                      this.listInfo.push(result[i])
                    }
                  }

                  ;[].forEach.call(files, this.readAndPreview)

                  this.importVisible = true
                  this.selectLoading = false
                }
              })
              .catch((err) => {
                this.selectLoading = false
              })
          }
        } else {
          this.$message.warning('为避免造成系统压力,一次性导入数量，不能超过2000张!')
          return
        }
      }
    },
    delImg(badge) {
      //根据工号，移除上传列表中的照片文件

      let listInfo = this.listInfo //列表展示的集合
      let uploadInfo = this.uploadInfo //上传的集合

      //移除的同时，列表展示的集合和上传的集合都要移除
      for (var i = 0; i < listInfo.length; i++) {
        if (listInfo[i].staffBadge == badge) {
          this.listInfo.splice(i, 1)
        }
      }

      //移除的同时，列表展示的集合和上传的集合都要移除
      for (var i = 0; i < uploadInfo.length; i++) {
        if (uploadInfo[i].staffBadge == badge) {
          this.uploadInfo.splice(i, 1)
        }
      }
    },
    importCancel() {
      this.importVisible = false
      this.noneStaffs = [] //不存在的工号集合
      this.fileNames = [] //名称集合
      this.listInfo = [] //根据名称集合查询的图片信息
      this.uploadInfo = [] //要上传的文件列表
      this.$refs.uploadBtn.value = ''
    },
    myUpload() {
      if (this.uploadInfo.length > 0) {
        this.importLoading = true

        importImgs({ facePicUpLoad: this.uploadInfo })
          .then((response) => {
            this.importLoading = false
            if (response.data.data) {
              this.importVisible = false
              this.$notify({
                title: '完成',
                message: '导入成功！',
                type: 'success'
              })
              this.taskRecordStr = response.data.data
              this.$refs.IssueAuthDialogs && this.$refs.IssueAuthDialogs.open()
            } else {
              // console.log(response.data)
              this.$notify.error({
                title: '失败',
                message: '员工照片导入失败！'
              })
            }
          })
          .catch((err) => {
            //不要在此处提示失败
            // this.$notify.error({
            //   title: '失败',
            //   message: '员工照片导入失败！'
            // });
            // console.log('catch-----------')
            // console.log(err)
            this.importLoading = false
          })
      }
    },
    importImg() {
      this.$refs.uploadBtn.click()
    },
    getList(page, params) {
      this.tableLoading = true
      fetchList(
        Object.assign(
          {
            descs: 'create_time',
            current: page.currentPage,
            size: page.pageSize
          },
          params
        )
      ).then((response) => {
        this.tableData = response.data.data.records
        this.page.total = response.data.data.total
        this.tableLoading = false
      })

      this.tableLoading = false
    },
    sizeChange(val) {
      this.page.currentPage = 1
      this.page.pageSize = val
      this.getList(this.page, this.searchForm)
    },
    currentChange(val) {
      this.page.currentPage = val
      this.getList(this.page, this.searchForm)
    },
    handleDetail(row, index) {
      const src = `/platform/basic/staff_info/detail/${row.id}`
      this.$router.push({
        path: src,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm
        }
      })
    },
    staffAuth(row, index) {
      const src = `/platform/basic/staff_info/auth/${row.id}`
      this.$router.push({
        path: src,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm
        }
      })
    },
    selectChange(val) {
      //序号那边选择事件
      this.selectStaffs = val
      var idArr = []
      if (val.length > 0) {
        val.forEach(function (element) {
          idArr.push(element.id)
        }, this)
      }
      this.entryForm.ids = idArr
      this.appForm.staffId = idArr
    },
    entrySubmit(formName) {
      // 数据验证
      if (!this.selectStaffs.length || !this.selectedAuth.length) {
        this.$message.warning('请选择员工和权限');
        return;
      }

      // 设置提交数据
      this.entryForm = {
        ids: this.selectStaffs.map(staff => staff.id),
        deviceAuthIds: this.selectedAuth
      };

      // console.log('提交的数据:', this.entryForm); // 打印提交数据

      this.entryLoading = true;
      upDeviceAuthList(this.entryForm, this.addType)
        .then((response) => {
          // console.log('接口返回:', response);
          if (response.data.code === 0) {
            this.taskRecordStr = response.data.data;
            this.$notify({
              title: '成功',
              message: '配置通关权限成功',
              type: 'success'
            });
            this.$refs.IssueAuthDialogs && this.$refs.IssueAuthDialogs.open();
          } else {
            this.$notify({
              title: '失败',
              message: response.data.msg || '配置通关权限失败',
              type: 'error'
            });
          }
          this.entryLoading = false;
        })
        .catch((err) => {
          // console.error('接口错误:', err);
          this.entryLoading = false;
          this.$notify({
            title: '失败',
            message: '配置通关权限失败',
            type: 'error'
          });
        });
    },
    entryFormClose() {
      this.entryFormVisible = false;
      this.resetEntry();
    },
    resetEntry() {
      this.tempSelectedAuth = [];
      this.tempRemovedAuth = [];
      this.selectedAuth = [];
      this.authSearchKey = '';
      this.addType = 1;
      this.entryForm = {
        staffIds: [],
        deviceAuthIds: []
      };
    },
    appSubmit(formName) {
      //配置APP权限，确认
      var _this = this
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.appLoading = true
          editAppList(this.appForm)
            .then((response) => {
              var msg = response.data.msg
              var dataResult = response.data.data
              if (dataResult === true) {
                this.appFormVisible = false
                _this.getList(_this.page, _this.searchForm)

                this.$notify({
                  title: '成功',
                  message: '修改成功',
                  type: 'success',
                  duration: 2000
                })
              } else if (dataResult === false) {
                this.$notify({
                  title: '失败',
                  message: msg,
                  type: 'error',
                  duration: 2000
                })
              }
              this.appLoading = false
            })
            .catch((err) => {
              this.appLoading = false
            })
        } else {
          // console.log('error submit!!')
          return false
        }
      })
    },
    resetApp(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields()
      }
    },
    getDeviceAuthList() {
      let ids = this.selectStaffs
      let _this = this

      let nullFaceArr = [] //没有人脸照片的员工集合
      ids.forEach(function (element) {
        if (!element.hasFace) {
          nullFaceArr.push(element.badge)
        }
      }, this)

      if (nullFaceArr.length > 0) {
        let str = '<div class="tips" style="max-width: 500px; word-break:break-all; line-height:20px;">'
        str += '<p>所选下列员工没有人脸照片，请导入人脸照片后再进行通关权限分配！</p><p style="color:red">'

        for (var i = 0; i < nullFaceArr.length; i++) {
          str += nullFaceArr[i] + '，'
        }

        var dotPosition = str.lastIndexOf('，')
        str = str.substr(0, dotPosition)
        str += '</p></div>'

        this.$message({
          dangerouslyUseHTMLString: true,
          showClose: true,
          message: str,
          type: 'warning',
          duration: 0
        })
      } else {
        this.entryFormVisible = true
        this.deviceAuthList()
      }
    },
    async issueAuth() {
      let ids = this.selectStaffs
      let _this = this

      let nullFaceArr = [] //没有人脸照片的员工集合
      ids.forEach(function (element) {
        if (!element.hasFace) {
          nullFaceArr.push(element.badge)
        }
      }, this)

      if (nullFaceArr.length > 0) {
        let str = '<div class="tips" style="max-width: 500px; word-break:break-all; line-height:20px;">'
        str += '<p>所选下列员工没有人脸照片，请导入人脸照片后再进行重新下发操作！</p><p style="color:red">'

        for (var i = 0; i < nullFaceArr.length; i++) {
          str += nullFaceArr[i] + '，'
        }

        var dotPosition = str.lastIndexOf('，')
        str = str.substr(0, dotPosition)
        str += '</p></div>'

        this.$message({
          dangerouslyUseHTMLString: true,
          showClose: true,
          message: str,
          type: 'warning',
          duration: 0
        })
      } else {
        //console.log(this.entryForm)
        await this.$confirm('是否重新下发所选员工权限？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        upDeviceAuthList({ ids: _this.entryForm.ids }, 3)
          .then((response) => {
            if (response.data.code === 0) {
              _this.taskRecordStr = response.data.data
              _this.$refs.IssueAuthDialogs && _this.$refs.IssueAuthDialogs.open()
            } else {
              _this.$notify({
                title: '失败',
                message: '重新下发权限失败',
                type: 'error'
              })
            }
            _this.entryLoading = false
          })
          .catch((err) => {
            _this.entryLoading = false
            _this.$notify({
              title: '失败',
              message: '重新下发权限失败',
              type: 'error'
            })
          })
      }
      // this.$refs.IssueAuthDialogs && this.$refs.IssueAuthDialogs.open()
    },
    getAppAuthList() {
      this.appFormVisible = true
      this.getAppList()
    },
    getAppList() {
      //查询app权限列表
      fetchAppList().then((response) => {
        if (response.data.data) {
          this.appList = response.data.data
        }
      })
    },
    deviceAuthList() {
      deviceAuthList({
        type: 1, //1代表门禁，要和c++那边对应
        current: 1,
        size: 1000
      }).then((response) => {
        if (response.data.data) {
          this.entryList = response.data.data.records
        }
      })
    },
    //导出
    export2Excel() {
      require.ensure([], () => {
        this.exportLoading = true
        const { export_json_to_excel } = require('@/vendor/Export2Excel')
        const tHeader = ['工号', '姓名', 'BU', '部门', '职层', '岗位', '入职日期', '员工状态', '所属园区', '是否有照片', '通关权限', 'APP权限']
        const filterVal = ['badge', 'name', 'compName', 'depName', 'jcheName', 'jobName', 'createTime', 'status', 'parkName', 'hasFace', 'deviceAuth', 'appAuth']

        let params = Object.assign(
          {
            current: 1,
            size: 10000
          },
          this.searchForm
        )
        var _this = this
        fetchList(params)
          .then((response) => {
            const list = response.data.data.records
            list.forEach(function (item) {
              item.status = staffStatusInit(item.status)
              item.hasFace = item.hasFace ? '有' : '无'
              item.deviceAuth = item.deviceAuth ? item.deviceAuth : '未分配'
              item.appAuth = item.appAuth ? item.appAuth : '未分配'
            })
            const data = this.formatJson(filterVal, list)
            export_json_to_excel(tHeader, data, `员工信息&(${this.searchForm.timeSlot})`);
            this.exportLoading = false
          })
          .catch((err) => {
            this.exportLoading = false
          })
      })
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map((v) => filterVal.map((j) => v[j]))
    },
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      this.page.currentPage = 1
      if (form.timeSlot && form.timeSlot != null) {
        form.startTime = form.timeSlot[0]
        form.endTime = form.timeSlot[1]
      } else {
        form.startTime = null
        form.endTime = null
      }
      form['badges'] = this.badges
      this.getList(this.page, form)
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields()
        this.page.currentPage = 1
        this.depIds = []
        this.badges = ''
        this.badgeArry = []
        this.searchForm.badges = null
        this.getList(this.page, this.searchForm)
      }
    },
    handleAuthSearch(val) {
      // 搜索框输入时触发过滤
      this.authSearchKey = val;
    },

    handleSelectAuth(auth) {
      if (!this.selectedAuth.includes(auth.id)) {
        this.selectedAuth.push(auth.id);
        this.entryForm.deviceAuthIds = this.selectedAuth; // 同步到表单数据
      }
    },


    // 清空已选权限
    clearSelected() {
      this.selectedAuth = [];
      this.tempRemovedAuth = [];
      this.entryForm.deviceAuthIds = []; // 同步到表单数据
    },

    getAuthName(id) {
      const auth = this.entryList.find(item => item.id === id);
      return auth ? auth.authorityName : '';
    },
    // 添加选中的权限
    addAuth() {
      this.selectedAuth = [...new Set([...this.selectedAuth, ...this.tempSelectedAuth])];
      this.tempSelectedAuth = [];
    },

    // 移除选中的权限
    removeAuth() {
      this.selectedAuth = this.selectedAuth.filter(id => !this.tempRemovedAuth.includes(id));
      this.tempRemovedAuth = [];
    },
    // 检查文本是否溢出
    isTextOverflow(event) {
      const element = event.target;
      return element.scrollWidth > element.clientWidth;
    },
  }
}
</script>

<style lang="scss" scoped>
.topForm ::v-deep {
  .el-form-item__label {
    width: 130px;
  }
}
.upload-demo {
  display: inline-block;
  margin-right: 20px;
}
.tips {
  max-width: 500px;
  border: 1px solid red;
}
.num {
  padding: 0 5px;
  font-weight: bold;
}
.red {
  color: red;
}
.importBtn {
  margin-right: 20px;
}
.fileBtn {
  display: none;
}
.importCont {
  line-height: 25px;
  padding-bottom: 30px;
}
.fileTb {
  margin: 10px 0 0 0;
  width: 100%;
  .haveNo {
    color: red;
  }
  td {
    border: 1px solid #e0e0e0;
    text-align: center;
    padding: 0 10px;
    height: 35px;
  }
}
::v-deep .el-scrollbar__wrap {
  overflow-x: auto;
}
.badgeTemplate {
  width: 180px;
  height: 32px;
  border: 1px solid rgb(220, 223, 230);
  border-radius: 4px;
  padding: 0 30px 0 15px;
  cursor: pointer;
  .el-tag {
    height: 24px;
    line-height: 24px;
  }
}
.mac-style {
  ::v-deep .el-dialog {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);

    .el-dialog__header {
      padding: 16px 20px;
      margin: 0;
      border-bottom: 1px solid #f0f0f0;

      .el-dialog__title {
        font-size: 16px;
        font-weight: 500;
      }
    }

    .el-dialog__body {
      padding: 20px;
      max-height: 600px;
      overflow-y: auto;
    }


  }

  .selected-staff-info {
    background: #f9f9f9;
    border-radius: 8px;
    padding: 12px 16px;
    margin-bottom: 16px;

    .info-content {
      display: flex;
      align-items: center;
      gap: 8px;

      i {
        color: #ed6d00;
      }

      .staff-tags {
        display: flex;
        gap: 6px;
        margin-left: 8px;

        .el-tag {
          background: #fff;
          border-color: #e4e4e4;
        }
      }
    }
  }

  .auth-container {
    display: flex;
    gap: 8px;
    margin: 16px 0;
    height: 320px;

    .auth-panel {
      flex: 1;
      height: 100%;
      border: 1px solid #DCDFE6;
      border-radius: 8px;
      display: flex;
      flex-direction: column;
      background: #fff;

      .panel-header {
        padding: 12px;
        border-bottom: 1px solid #EBEEF5;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .panel-title {
          font-weight: 500;
          color: #303133;
        }

        .el-input {
          width: 180px;
          margin-left: 12px;
        }
      }

      .panel-content {
        flex: 1;
        overflow-y: auto;
        padding: 8px;

        &::-webkit-scrollbar {
          width: 6px;
        }

        &::-webkit-scrollbar-thumb {
          background: #E4E7ED;
          border-radius: 3px;
        }
      }
    }

    .auth-checkbox {
      display: block;
      padding: 8px 12px;
      margin: 4px 0;
      border-radius: 4px;

      &:hover {
        background-color: #F5F7FA;
      }

      ::v-deep .el-checkbox__label {
        padding-left: 8px;
      }

      .auth-item-content {
        display: flex;
        align-items: center;
        gap: 8px;

        .auth-name {
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        i {
          color: #909399;
          font-size: 14px;
          flex-shrink: 0;
        }
      }
    }

    .auth-operations {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;

      .operations-center {
        display: flex;
        flex-direction: column;
        gap: 12px;

        .el-button {
          width: 36px;
          height: 36px;
          padding: 0;
          display: flex;
          align-items: center;
          justify-content: center;
          margin: 0;

          i {
            font-size: 18px;
            margin: 0;
            position: relative;

            &.el-icon-arrow-right {
              left: 1px;
            }

            &.el-icon-arrow-left {
              right: 1px;
            }
          }

          &[disabled] {
            color: #C0C4CC;
            background-color: #F5F7FA;
            border-color: #E4E7ED;
          }
        }
      }
    }
  }

  .assign-type {
    padding: 16px;
    background: #f9f9f9;
    border-radius: 8px;
    margin-top: 16px;
  }
}

// 文本溢出省略号
.text-ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
