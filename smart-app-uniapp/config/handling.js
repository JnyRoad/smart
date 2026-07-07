/* 
	弹窗  toast Modal  调用方法 this.$ytHint.toast(obj) this.$ytHint.modal(obj)
 */
export default {
	toast (obj) {
		uni.showToast({
			title: obj.title || '请求后台服务失败',
			icon: obj.icon || 'none',
			mask: obj.mask || false,
			duration: obj.duration || 1500,
			success: () => {
				if (typeof obj.success === 'function' && obj.success) {
					setTimeout(function() {
						obj.success()
					},obj.duration||1500)
				}
			}
		})
	},
	modal (obj) {
		uni.showModal({
			title: obj.title || '温馨提示',
			content: obj.content,
			showCancel:obj.showCancel || true,
			cancelText: obj.cancelText || '取消',
			confirmText: obj.confirmText || '确定',
			confirmColor: obj.confirmColor || '#508BFF',
			success: function (res) {
				if (res.confirm) {
					if (typeof obj.confirm === 'function' && obj.confirm) {
						obj.confirm()
					}
				} else if (res.cancel) {
					if (typeof obj.cancel === 'function' && obj.cancel) {
						obj.cancel()
					}
				}
			}
		})
	}
}

