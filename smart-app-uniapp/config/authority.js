/*
	各类权限问题 更多请访问 http://www.html5plus.org/doc/zh_cn/downloader.html
*/ 

// 获取定位权限  若未开 提示去开启定位 未使用 依赖不是很大
const locationAuthortyPermissions = (callback) => {
	const that = this
	let system = uni.getSystemInfoSync()
	if (system.platform === 'android') {
		plus.android.requestPermissions(['android.permission.ACCESS_FINE_LOCATION'], function(e) {
			if (e.deniedAlways.length > 0) { //权限被永久拒绝
				// 弹出提示框解释为何需要定位权限，引导用户打开设置页面开启
				console.log('Always Denied!!! ' + e.deniedAlways.toString());
				toLocationSettings('定位权限未开启，去开启？', 1)
			}
			if (e.deniedPresent.length > 0) { //权限被临时拒绝
				// 弹出提示框解释为何需要定位权限，可再次调用plus.android.requestPermissions申请权限
				console.log('Present Denied!!! ' + e.deniedPresent.toString());
				toLocationSettings('定位权限未开启，去开启？', 2)
			}
			if (e.granted.length > 0) { //权限被允许
				// 调用依赖获取定位权限的代码
				console.log('Granted!!! ' + e.granted.toString());
				callback()
			}
		}, function (e) {
			console.log('Request Permissions error:' + JSON.stringify(e));
		});
	}
	function toLocationSettings (title,type) {
		uni.showModal({
			title: '温馨提示',
			content: title,
			showCancel: false,
			success: () => {
				const main = plus.android.runtimeMainActivity()
				const Intent = plus.android.importClass('android.content.Intent')
				const Settings = plus.android.importClass('android.provider.Settings')
				const intent = new Intent(Settings.ACTION_SECURITY_SETTINGS)
				main.startActivity(intent)
			}
		})
	}
}

export {
	locationAuthortyPermissions
}