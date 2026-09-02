package com.tce.smart.common.security.openapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 开放 API capability scope 的后端权威目录。
 *
 * <p>目录与 Controller 的精确 scope 同版本发布，避免前端静态常量、数据库录入值和资源服务鉴权值
 * 各自演进。新增能力时只新增明确的资源/动作 scope，不引入 {@code energy:*} 一类通配授权。</p>
 */
public final class OpenApiScopeCatalog {

	/** 历史通用服务 scope，仅用于迁移兼容，禁止新增授予。 */
	public static final String LEGACY_SERVER = "server";
	/** 入厂申请照片读取 capability。 */
	public static final String ADMITTANCE_PHOTO_READ = "open:admittance:photo:read";
	/** 能耗投影任务执行 capability。 */
	public static final String ENERGY_PROJECTION_RUN = "internal:energy:projection:run";

	private static final Map<String, OpenApiScope> BY_VALUE;
	private static final List<OpenApiScope> ALL;

	static {
		Map<String, OpenApiScope> scopes = new LinkedHashMap<>();
		register(scopes, new OpenApiScope(ADMITTANCE_PHOTO_READ, "入厂申请照片-读取", false));
		register(scopes, new OpenApiScope(ENERGY_PROJECTION_RUN, "能耗投影-运行", false));
		register(scopes, new OpenApiScope(LEGACY_SERVER, "通用服务（历史兼容，禁止新增）", true));
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
