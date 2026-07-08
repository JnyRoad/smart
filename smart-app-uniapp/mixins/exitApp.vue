<script>
export default {
	onBackPress(options) {
		if (options.from === 'backbutton') {
			uni.showModal({
				title: '提示',
				content: '确定退出APP？',
				success: function(res) {
					if (res.confirm) {
						// 退出当前应用，改方法只在App中生效
						if (typeof plus !== 'undefined' && plus.runtime) {
							plus.runtime.quit();
						}
					}
				}
			});
			return true;
		}
	}
};
</script>
