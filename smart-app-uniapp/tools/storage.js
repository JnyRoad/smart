// 关于存储本地的数据放在这里
export const ISFIRSTENTER = 'isFirstEnter' // 是否第一次进入app
export const ACCOMPANYPEOPLE = 'accompanyPeople' // 随行人员的本地存储字段
export const MEMBERPHONE = 'memberPhoto' // 随行人员的照片
export const MEMBERNAME = 'memberName' // 随行人员姓名
export const TEMPORARYDATA = 'temporarydata' // 临时数据存储KEY
export const RECURIT_APPLY = 'recuritApply' // 招聘简历筛选 选中的简历对象
export const RECURIT_APPLICATIONID = 'recuritApplicationId' // 招聘简历筛选 选中的简历id
export const HASGUIDED = 'hasGuided' // 是否已经引导过了
// 定义并导出一系列常量  根据业务情况定义
export const USER_TOKEN = 'USER_TOKEN' // 用户登录后的token
export const REFRESH_TOKEN = 'REFRESH_TOKEN' // 刷新登录令牌
export const USER_ID = 'USER_ID' // 用户登录后用户ID 
export const USER_NAME = 'USER_NAME' // 用户名 登录账号
export const EMPLOYEE_NAME = 'EMPLOYEE_NAME' // 员工名
export const USER_LAT = 'USER_LAT' // 当前用户的纬度
export const USER_LONG = 'USER_LONG' // 当前用户的经度
export const USER_TEL = 'USER_TEL' // 用户的手机号码
export const USER_POSITION = 'USER_POSITION' // 用户的定位
export const PARK_NAME = 'PARK_NAME' // 当前园区的名
export const PARK_ID = 'PARK_ID' // 当前园区的ID
export const USER_BASE_INFO = 'USER_BASE_INFO' // 用户的基本信息
export const EXPIRES_IN = 'EXPIRES_IN' // 登录的有效期
export const ISCOMPLETEINFORMATION = 'IS_COMPLETE_INFORMATION' // 是否完成信息填写
export const SALARYTYPENAME = "salaryTypeName" //查询员工薪资计算类型
export const USER_TYPE = "USER_TYPE" //员工类型
export const USER_STATUS = "USER_STATUS" //员工状态

export const storage = {
	USER_TYPE,
	USER_STATUS,
	USER_TOKEN,
	REFRESH_TOKEN,
	USER_ID,
	USER_TEL,
	USER_NAME,
	EMPLOYEE_NAME,
	USER_LAT,
	USER_LONG,
	PARK_NAME,
	USER_POSITION,
	PARK_ID,
	USER_BASE_INFO,
	EXPIRES_IN,
	ISCOMPLETEINFORMATION,
	SALARYTYPENAME,
	setSync(key, data) {
		try {
			uni.setStorageSync(key, data)
		} catch (err) {
			console.log(err);
		}
	},
	set(key, data) {
		uni.setStorage({
			key: key,
			data: data
		})
	},
	getSync(key) {
		try {
			return uni.getStorageSync(key)
		} catch (err) {
			console.log(err);
		}
	},
	get(key) {
		uni.setStorage({
			key: key,
			success: function(res) {
				return res
			}
		})
	},
	removeSync(key) {
		try {
			uni.removeStorageSync(key)
		} catch (err) {
			console.log(err);
		}
	},
	remove(key) {
		uni.removeStorage({
			key: key,
			success: function(res) {
				return res
			}
		})
	}
}
