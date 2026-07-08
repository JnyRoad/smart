/**
 * 环境配置：全工程唯一的服务器地址来源，业务代码禁止再写死域名。
 *
 * 环境选择分两层：
 * 1. 自动层：process.env.NODE_ENV —— HBuilderX「运行」编译为 development，「发行」编译为 production。
 * 2. 覆盖层：FORCE_ENV —— 云打包（发行）产物的 NODE_ENV 恒为 production，
 *    需要打「连测试环境的发行包」给测试安装时，把 FORCE_ENV 改为 'development' 再打包。
 *
 * 【发版检查单】发生产包前必须确认 FORCE_ENV = null；
 * 若忘记还原，App 启动会弹出阻断式「测试环境包」提示（防呆逻辑见 App.vue onLaunch）。
 */

// 发行测试包时手动改为 'development'；发生产包必须还原为 null
const FORCE_ENV = null

const ENV = {
	// 测试环境：直连内网网关（10.0.20.113 的 smart-gateway，端口 9990）。
	// 注意别用 113 的 80 端口——那是 admin 后台静态站，只代理 /auth 等、不代理 /app
	development: {
		// 后端网关地址（认证 /auth/** 与业务 /app/** 接口统一入口）
		API_BASE_URL: 'http://10.0.20.113:9990/',
		// 薪资查询外链（gz 为独立系统，协议与地址由对方系统决定，本工程只引用）
		SALARY_QUERY_URL: 'http://gz.szyuto.com/XinZiChaXun/salary-query.aspx',
	},
	// 生产环境
	production: {
		API_BASE_URL: 'http://smartapp.szyuto.com:8090/',
		SALARY_QUERY_URL: 'http://gz.szyuto.com/XinZiChaXun/salary-query.aspx',
	},
}

// FORCE_ENV 优先于 NODE_ENV；两者都取不到时兜底生产配置，保证任何打包形态下都有可用地址
const active = FORCE_ENV || process.env.NODE_ENV

// 是否处于 FORCE_ENV 强制覆盖状态（App.vue 启动防呆提示用）
export const IS_FORCED = !!FORCE_ENV

export default ENV[active] || ENV.production
