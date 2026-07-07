import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const store = new Vuex.Store({
    state: {
		restart: false,
		address: '',
		dormStatus: '0',
		dormDesc: '未分配',
		vehicleStatus: '0', // 车辆 
		vehicleDesc: '未添加',
		modelStatus: false,
		showLoading: false,
		planSelectNum: 0, // 备选
		pushMessage: {} // 推送对象
    },
    mutations: {
		restart (state,status) {
			state.restart = status
		},
        login(state) {
            state.hasLogin = true;
        },
        logout(state) {
            state.hasLogin = false;
        },
		// 宿舍的申请状态
		WatchDorm(state, status) {
			state.dormStatus = status.dormStatus
			state.dormDesc = status.dormDesc
		},
		// 车辆的申请状态
		WatchVehicle(state, status) {
			state.vehicleStatus = status.vehicleStatus
			state.vehicleDesc = status.vehicleDesc
		},
		// 显示输入模态框
		ytModelStatus(state,status) {
			state.modelStatus = status
		},
		// 显示Loading 动画
		ytLoading(state, xshow) {
			state.showLoading = xshow
		},
		// 加入备选
		addPlanSelect(state, num) {
			state.planSelectNum = num
		},
		// 设置地址
		setAddress (state, address) {
			state.address = address
		},
		// 序列化推送
		updatePushMessage(state, message) {
			/**
			 * 注意：这里为了方便预览查看效果，始终对 payload 做了序列化的处理。
			 * 实际开发期中，请自行调整代码并注意发送的 payload 消息格式。
			 */ 
			let payload = message.payload;
			if (typeof payload !== 'string') {
				message.payload = JSON.stringify(payload);
			}
			state.pushMessage = message || {};
		}
    }
})

export default store
