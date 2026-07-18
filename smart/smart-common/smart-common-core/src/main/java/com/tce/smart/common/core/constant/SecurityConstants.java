package com.tce.smart.common.core.constant;

/**
 * 常量
 */
public interface SecurityConstants {
	/**
	 * 管理员帐号
	 */
	String ADMIN = "admin";
	/**
	 * 系统默认帐号
	 */
	String SYS_DEFAULT_USER = "sys_default_user";
	/**
	 * 密码参数
	 */
	String PASSWORD = "password";
	/**
	 * 管理员角色
	 */
	String ROLE_ADMIN = "ROLE_ADMIN";
	/**
	 * 普通员工角色
	 */
	String ROLE_ORDINARY = "ROLE_ORDINARY";
	/**
	 * 普通员工角色ID
	 */
	Integer ROLE_ORDINARY_ID = 3;
	/**
	 * 刷新
	 */
	String REFRESH_TOKEN = "refresh_token";
	/**
	 * 验证码有效期
	 */
	int CODE_TIME = 60;
	/**
	 * 验证码长度
	 */
	String CODE_SIZE = "4";
	/**
	 * 角色前缀
	 */
	String ROLE = "ROLE_";
	/**
	 * 前缀
	 */
	String SMART_PREFIX = "smart_";

	/**
	 * oauth 相关前缀
	 */
	String OAUTH_PREFIX = "oauth:";
	/**
	 * 项目的license
	 */
	String SMART_LICENSE = "Made By TCE";

	/**
	 * 内部
	 */
	String FROM_IN = "Y";

	/**
	 * 标志
	 */
	String FROM = "from";

	/**
	 * OAUTH URL
	 */
	String OAUTH_TOKEN_URL = "/oauth/token";

	/**
	 * 手机号登录URL
	 */
	String SMS_TOKEN_URL = "/mobile/token/sms";

	/**
	 *  orc人脸登录URL
	 */
	String OCR_TOKEN_URL = "/ocr/token/*";

	/**
	 * 微信公众号登录URL
	 */
	String WX_PUBLIC_TOKEN_URL = "/wx/public/token";
	/**
	 * 友互通登录URL
	 */
	String YHT_TOKEN_URL = "/yht/token";

	/**
	 * 修改密码URL
	 */
	String UPDATE_PWD_URL = "/password/update";

	/**
	 * 社交登录URL
	 */
	String SOCIAL_TOKEN_URL = "/mobile/token/social";
	/**
	 * 自定义登录URL
	 */
	String MOBILE_TOKEN_URL = "/mobile/token/*";
	/**
	 * oauth 客户端信息
	 */
	String CLIENT_DETAILS_KEY = "smart_oauth:client:details";

	/**
	 *  app 密码修改授权码子key
	 */
	String PWD_UPDATE_AUTHCODE_SUB_KEY = "pwdUpdateAuthCode";

	/**
	 * app 密码修改授权码
	 */
	String APP_PWD_UPDATE_AUTHCODE = "smart_app:auth:password:update:code:";

	/**
	 * 微信获取OPENID
	 */
	String WX_AUTHORIZATION_CODE_URL = "https://api.weixin.qq.com/sns/oauth2/access_token" +
			"?appid=%s&secret=%s&code=%s&grant_type=authorization_code";

	/**
	 * 微信获取ACCESS_TOKEN
	 */
	String WX_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";

	/**
	 * 微信获取JSAPI_TICKET
	 * jsapi_ticket是公众号用于调用微信JS接口的临时票据
	 */
	String WX_JSAPI_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi";

	/**
	 * {bcrypt} 加密的特征码
	 */
	String BCRYPT = "{bcrypt}";
	/**
	 * sys_oauth_client_details 表的字段，不包括client_id、client_secret
	 * 注意：client_secret 直接读取原始值，不再在 SQL 里强制拼接 '{noop}' 前缀；
	 *      编码前缀（{noop}/{bcrypt}）随数据入库，交给 DelegatingPasswordEncoder 按前缀选择匹配算法。
	 */
	String CLIENT_FIELDS = "client_id, client_secret, resource_ids, scope, "
			+ "authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, "
			+ "refresh_token_validity, additional_information, autoapprove";

	/**
	 * JdbcClientDetailsService 查询语句
	 */
	String BASE_FIND_STATEMENT = "select " + CLIENT_FIELDS
			+ " from sys_oauth_client_details";

	/**
	 * 默认的查询语句
	 */
	String DEFAULT_FIND_STATEMENT = BASE_FIND_STATEMENT + " order by client_id";

	/**
	 * 按条件client_id 查询
	 */
	String DEFAULT_SELECT_STATEMENT = BASE_FIND_STATEMENT + " where client_id = ?";

	/**
	 * 资源服务器默认bean名称
	 */
	String RESOURCE_SERVER_CONFIGURER = "resourceServerConfigurerAdapter";

	/**
	 * 客户端模式
	 */
	String CLIENT_CREDENTIALS = "client_credentials";

}
