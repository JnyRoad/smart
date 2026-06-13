import Vue from 'vue'
import toast from '@/components/toast/main.js'
import '@/components/loading/index.js'
import '@/components/message/main.js'

import tceImage from '@/components/tce-image/index.vue'
import tceForm from '@/components/tce-form/index.vue'
import tceFormGroup from '@/components/tce-form/form-group.vue'
import tceFormItem from '@/components/tce-form/form-item.vue'
import tceLabelJustify from '@/components/tce-label-justify/index'
import tceEmpty from '@/components/empty/empty.vue'
Vue.component('tce-image', tceImage)
Vue.component('tce-form', tceForm)
Vue.component('tce-form-group', tceFormGroup)
Vue.component('tce-form-item', tceFormItem)
Vue.component('tce-label-justify', tceLabelJustify)
Vue.component('tce-empty', tceEmpty)

Vue.prototype.$tceMobile = {
  toast: toast
}
