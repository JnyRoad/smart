<template>
  <main>
    <div class="print-page print-toolbar">
      <label>管理园区 <select
        v-model="parkId"
        :disabled="loading || editing || busy"
        aria-label="管理园区"><option value="">请选择园区</option><option
          v-for="park in parks"
          :key="park.id"
          :value="String(park.id)">{{ park.name || park.parkName }}</option></select></label>
      <router-link :to="{ path: '/platform/print/templates', query: { parkId } }">单面模板</router-link>
      <router-link :to="{ path: '/platform/print/pairs', query: { parkId } }">正反面组合</router-link>
      <router-link :to="{ path: '/platform/print/bindings', query: { parkId } }">适用规则</router-link>
      <router-link :to="{ path: '/platform/print/jobs/manual', query: { parkId } }">厂牌打印</router-link>
      <router-link :to="{ path: '/platform/print/jobs/visitor', query: { parkId } }">访客打印</router-link>
      <router-link :to="{ path: '/platform/print/printers', query: { parkId } }">打印机</router-link>
      <p
        v-if="error"
        role="alert"
        class="error">{{ error }}</p>
    </div>
    <router-view
      :park-id="parkId"
      @editing-state="editing = $event"
      @busy-state="busy = $event" />
  </main>
</template>
<script>
import { fetchList } from '@/api/platform/area/park'
import { recordsOf } from '@/api/platform/print/client'
export default {
  name: 'PrintCenter',
  data() { return { parkId: '', parks: [], loading: true, editing: false, busy: false, error: '' } },
  watch: { '$route.path'() { this.editing = false; this.busy = false } },
  /** 园区来自现有权限过滤接口；查询参数不能自行增加可访问园区。 */
  async mounted() { try { let current = 1; let total; do { const response = await fetchList({ current, size: 100 }); if (response.status !== 200 || !response.data || response.data.code !== 0) throw new Error('无法读取获准园区'); const records = recordsOf(response.data.data); this.parks.push(...records); total = Number(response.data.data.total || this.parks.length); if (!records.length) break; current++ } while (this.parks.length < total); const requested = String(this.$route.query.parkId || ''); if (this.parks.some(park => String(park.id) === requested)) this.parkId = requested; else if (this.parks.length === 1) this.parkId = String(this.parks[0].id) } catch (error) { this.error = error.message } finally { this.loading = false } }
}
</script>
<style src="./print.css" />
