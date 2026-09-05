package com.tce.smart.common.security.openapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 开放 API capability scope 的后端权威目录。
 *
 * <p>目录与 Controller 的 scope 同版本发布，避免前端静态常量、数据库录入值和资源服务鉴权值
 * 各自演进。内部开放接口统一使用 {@code server}；既有细分 scope 仅作为存量兼容项保留，
 * 不引入 {@code energy:*} 一类通配授权。</p>
 */
public final class OpenApiScopeCatalog {

	/** 内部开放接口统一使用的通用服务 scope。 */
	public static final String SERVER = "server";
	/**
	 * 兼容既有代码引用的通用服务 scope 别名。
	 *
	 * @deprecated 请使用 {@link #SERVER}；该值仍是正常可授予的 scope。
	 */
	@Deprecated
	public static final String LEGACY_SERVER = SERVER;
	/** 既有入厂申请照片读取细分 scope，仅用于存量兼容。 */
	public static final String ADMITTANCE_PHOTO_READ = "open:admittance:photo:read";
	/** 既有能耗投影任务执行细分 scope，仅用于存量兼容。 */
	public static final String ENERGY_PROJECTION_RUN = "internal:energy:projection:run";

	private static final Map<String, OpenApiScope> BY_VALUE;
	private static final List<OpenApiScope> ALL;

	static {
		Map<String, OpenApiScope> scopes = new LinkedHashMap<>();
		register(scopes, new OpenApiScope(SERVER, "通用服务", false));
		register(scopes, new OpenApiScope(ADMITTANCE_PHOTO_READ, "入厂申请照片-读取（历史兼容）", true));
		register(scopes, new OpenApiScope(ENERGY_PROJECTION_RUN, "能耗投影-运行（历史兼容）", true));
		BY_VALUE = Collections.unmodifiableMap(scopes);
		ALL = Collections.unmodifiableList(new ArrayList<>(scopes.values()));
	}

	private OpenApiScopeCatalog() {
	}

	/**
	 * 返回稳定顺序的只读目录，供管理端渲染多选项；不包含任何客户端密钥或运行态凭据。
	 */
	public static List<OpenApiScope> all() {
		return ALL;
	}

	public static boolean contains(String value) {
		return value != null && BY_VALUE.containsKey(value);
	}

	public static boolean isDeprecated(String value) {
		OpenApiScope scope = BY_VALUE.get(value);
		return scope != null && scope.isDeprecated();
	}

	private static void register(Map<String, OpenApiScope> scopes, OpenApiScope scope) {
		if (scopes.put(scope.getValue(), scope) != null) {
			throw new IllegalStateException("重复注册开放 API scope：" + scope.getValue());
		}
	}
}
