<script>
	import { storage } from '@/tools/storage.js';
	export default {
		// 页面隐藏无条件设置弹出框隐藏
		onHide() {
			this.$store.commit('ytModelStatus',false)
			this.$loading(false)
		},
		onBackPress() {
			this.$store.commit('ytModelStatus',false) 
			this.$loading(false)
		}
	}
</script>
