<!--业务监控：退宿记录 -->
<template>
  <div class="my-basic-container staff">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom('searchForm')" plain>清空</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId" @defaultHandle="defaultHandle"></parkSelect>
          </el-form-item>
          <!-- <el-form-item label="楼栋" prop="dormitoryId">
            <dormMultiSelect :parkId="searchForm.parkId" v-model="searchForm.dormitoryId"></dormMultiSelect>
          </el-form-item> -->
          <el-form-item label="申请人工号" prop="badge">
            <el-input v-model="searchForm.badge" placeholder="工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="申请人姓名" prop="name">
            <el-input v-model="searchForm.name" placeholder="姓名" clearable></el-input>
          </el-form-item>
          <!-- <el-form-item label="是否处理" prop="isHandle">
            <el-select v-model="searchForm.isHandle" clearable placeholder="是否处理">
              <el-option label="已处理" value="已处理"></el-option>
              <el-option label="未处理" value="未处理"></el-option>
            </el-select>
          </el-form-item> -->
          <el-form-item label="申请时间" prop="timeRange">
            <el-date-picker
              v-model="searchForm.timeRange"
              type="datetimerange"
              range-separator="-"
              value-format="yyyy-MM-dd HH:mm:ss"
              :default-time="['00:00:00', '23:59:59']"
              start-placeholder="起始日期"
              end-placeholder="截止日期"
              clearable
            ></el-date-picker>
          </el-form-item>
        </el-form>
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" :option="tableOption" @size-change="sizeChange" @current-change="currentChange">
          <template slot-scope="scope" slot="status">
            <span>{{ scope.row.status }}</span>
          </template>

          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-view" @click="handleDetail(scope.row, scope.$index)">查看详情</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList } from '@/api/platform/work/checkedOut'
import { tableOption } from '@/const/crud/platform/work/checkedOut'
import { mapGetters } from 'vuex'
import { isArrayFn } from '@/util/util'
import dormMultiSelect from "@/views/platform/components/dorm-multi-select/index";
export default {
  name: 'checkedOut',
  components: {
    dormMultiSelect
  },
  data() {
    return {
      searchForm: {
        //搜索菜单表单
        name: undefined,
        badge: undefined,
        timeRange: undefined,
        parkId: undefined
        // dormitoryId: [],
      },

      hasSelect: false,
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      }
    }
  },
  created() {
    this.$nextTick(() => {
      // 详情带参数返回
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
    startTime: function () {
      if (this.searchForm.timeRange) {
        return this.searchForm.timeRange[0]
      } else {
        return undefined
      }
    },
    endTime: function () {
      if (this.searchForm.timeRange) {
        return this.searchForm.timeRange[1]
      } else {
        return undefined
      }
    }
  },
  watch: {},
  methods: {
    defaultHandle(e){
      this.searchform.parkId = e.value
    //   this.getList(this.searchform)
    },
    getList(page, params) {
      this.tableLoading = true

      params = Object.assign(
        {
          current: page.currentPage,
          size: page.pageSize,
          startTime: this.startTime,
          endTime: this.endTime
        },
        params
      )

      if (isArrayFn(params.timeRange)) {
        //虽然后台没有用到这个值，但是传参时会报错，所以修改一下格式
        params.timeRange = params.timeRange.join()
      }

      fetchList(params).then((response) => {
        this.tableData = response.data.data.records
        this.tableData.forEach((element) => {
          const p = element.dorDetailStr[0].split('-')
          element['parkName'] = p[0]
          element['dormitoryName'] = p[1] + p[2]
        })
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
      const src = `/platform/work/checkedOut/detail/${row.id}`
        this.$router.push({
          path: src,
          query: {
            queryPage: this.page,
            queryForm: this.searchForm
          }
        });
    },
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      this.page.currentPage = 1
      this.getList(this.page, form)
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields()
        this.page.currentPage = 1
        this.getList(this.page)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
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
</style>
