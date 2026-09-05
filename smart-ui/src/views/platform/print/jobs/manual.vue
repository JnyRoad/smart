<template><print-workbench ref="workbench" :park-id="parkId" @busy-state="$emit('busy-state', $event)" @editing-state="$emit('editing-state', $event)" /></template>
<script>
import PrintWorkbench from './Workbench.vue'
export default { name: 'StaffPrintPage', components: { PrintWorkbench }, beforeRouteLeave(to, from, next) { next(this.$refs.workbench.canLeave()) }, props: { parkId: { type: String, default: '' } } }
</script>
