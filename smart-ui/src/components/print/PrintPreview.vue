<template>
  <section
    :class="{ 'print-dialog': !embedded }"
    :role="embedded ? 'region' : 'dialog'"
    class="print-preview"
    aria-label="模板打印预览">
    <h2>{{ frozen ? '本任务冻结卡面' : business ? '实际人员打印预览' : '模板预览' }}</h2><p>{{ frozen ? '以下为本任务已冻结的卡面，请与当前实物逐面核对。' : business ? '请逐面核对实际人员资料和本次选择的模板。' : '使用合成示例数据核对版面。' }}</p>
    <p
      v-if="error"
      role="alert"
      class="error">{{ error }}</p>
    <ul
      v-if="diagnostics.length"
      aria-label="预览诊断"><li
        v-for="(diagnostic, index) in diagnostics"
        :key="index">{{ diagnostic }}</li></ul><p
          v-else-if="loading"
          role="status">正在生成并校验预览…</p>
    <div
      v-for="artifact in displayed"
      :key="artifact.id"><h3>{{ artifact.face === 'BACK' ? '背面' : '正面' }}</h3><iframe
        :src="artifact.url"
        :title="artifact.face === 'BACK' ? '背面预览' : '正面预览'" /></div>
    <button
      v-if="!embedded"
      type="button"
      @click="$emit('close')">关闭预览</button>
  </section>
</template>
<script>
import { verifyPreviewArtifact } from './preview-artifact'
import { getPreview, downloadPreviewArtifact } from '@/api/platform/print/templates'
const violationLabels = { TEXT_OVERFLOW: '文字超出组件范围，请扩大组件或缩短内容', GLYPH_MISSING: '字体缺少所需字形，请检查该组件内容', ELEMENT_OUT_OF_BOUNDS: '组件超出版面，请调整位置或尺寸', ROTATION_UNSUPPORTED: '当前不支持旋转组件', TEXT_MODE_UNSUPPORTED: '当前不支持该文字样式', TEXT_METRICS_INVALID: '字号、行距或字间距无效', FIELD_REQUIRED: '缺少必填字段', IMAGE_URI_NOT_INLINE: '图片资源无效，请重新上传', RESOURCE_HASH_MISMATCH: '图片校验失败，请重新上传' }
export default {
  name: 'PrintPreview', props: { initial: { type: Object, required: true }, business: Boolean, embedded: Boolean, frozen: Boolean, expectedFaces: { type: Array, default: () => [] }, loadPreview: { type: Function, default: getPreview }, loadArtifact: { type: Function, default: downloadPreviewArtifact } },
  data() { return { loading: true, error: '', diagnostics: [], displayed: [], timer: null, attempts: 0, disposed: false } },
  mounted() { this.consume(this.initial) },
  beforeDestroy() { this.disposed = true; clearTimeout(this.timer); this.displayed.forEach(item => URL.revokeObjectURL(item.url.split('#')[0])) },
  methods: {
    /** 只读取当前已授权预览制品，逐张校验PDF头、大小和hash后才建立临时展示地址。 */
    async consume(preview) {
      try {
        if (this.disposed) return
        if (preview.status === 'RENDER_FAILED') {
          const violations = preview.violations || (preview.error && preview.error.details && preview.error.details.violations) || []
          this.diagnostics = (Array.isArray(violations) ? violations : []).map(item => {
            const location = [item.face === 'FRONT' ? '正面' : item.face === 'BACK' ? '背面' : '', item.schemaName || ''].filter(Boolean).join(' · ')
            return `${location ? location + '：' : ''}${violationLabels[item.code] || '组件校验失败，请检查版面与字段'}（${item.code || preview.errorCode || 'RENDER_FAILED'}）`
          })
          throw new Error((preview.error && preview.error.message) || '预览生成失败，请检查下列组件诊断')
        }
        if (preview.status !== 'READY') {
          if (++this.attempts > 30) throw new Error('预览尚未完成，请稍后重新预览')
          this.timer = setTimeout(async () => { try { await this.consume(await this.loadPreview(preview.previewId)) } catch (error) { if (!this.disposed) { this.error = error.message; this.loading = false } } }, 1000)
          return
        }
        if (!Array.isArray(preview.artifacts) || !preview.artifacts.length || preview.artifacts.length > 2) throw new Error('预览制品不完整')
        if (this.expectedFaces.length && (preview.artifacts.length !== this.expectedFaces.length || preview.artifacts.some((a, index) => a.face !== this.expectedFaces[index]))) throw new Error('预览卡面不完整，请重新生成全部卡面')
        for (const artifact of preview.artifacts) {
          const blob = await this.loadArtifact(preview.previewId, artifact.artifactId)
          if (this.disposed) return
          const verified = await verifyPreviewArtifact(blob, artifact)
          if (this.disposed) return
          this.displayed.push({ id: artifact.artifactId, face: artifact.face, url: URL.createObjectURL(verified) + '#toolbar=0' })
        }
        if (!this.disposed) this.$emit('verified', preview.previewId)
      } catch (error) { if (!this.disposed) { this.error = error.message; this.displayed.forEach(item => URL.revokeObjectURL(item.url.split('#')[0])); this.displayed = [] } }
      finally { if (!this.disposed && (preview.status === 'READY' || this.error)) this.loading = false }
    }
  }
}
</script>
