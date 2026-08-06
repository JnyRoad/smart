package com.tce.smart.data.security;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.openapi.OpenApiInterceptor;
import com.tce.smart.data.api.dto.businesstrip.req.CcdFormtableMainReqDTO;
import com.tce.smart.data.api.dto.temporary.req.SaveEPhotoReqDTO;
import com.tce.smart.data.api.dto.temporary.req.EleaveJjitemReqDTO;
import com.tce.smart.data.controller.businesstrip.FormtableMainController;
import com.tce.smart.data.controller.dhrview.YutoDhrPsndoController;
import com.tce.smart.data.controller.ehrview.EvwEmphrYsController;
import com.tce.smart.data.controller.temporary.EPhotoController;
import com.tce.smart.data.controller.temporary.EleaveJjitemController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.After;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * EHR 内部路由必须在统一入口按精确服务 client_id 收口，不能只依赖任意 server scope。
 */
public class EhrInternalRouteClientAccessInterceptorTest {

	private static final String ALLOWED_CLIENT = "smart-app";
	private static final Pattern INNER_SERVER_ROUTE = Pattern.compile(
			"(?m)^\\s*@Inner\\s*\\R\\s*@OpenApi\\(\\\"server\\\"\\)\\s*\\R\\s*@(Get|Post|Put|Delete|Patch)Mapping\\(");

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void everyRealDataInnerServerRouteUsesTheUnifiedExactClientGuard() throws Exception {
		EhrInternalRouteClientAccessInterceptor interceptor = interceptor();
		Method representativeRoute = EvwEmphrYsController.class.getMethod("info", String.class);
		assertTrue("统一守卫必须按 @Inner + @OpenApi(server) 识别路由",
				interceptor.requiresExactClientGuard(representativeRoute));
		assertTrue("测试必须从真实 Controller 源码枚举到内部服务路由", countRealInnerServerRoutes() > 0);
	}

	@Test
	public void everyGuardedHandlerMethodUsesAUniqueRouteClientMappingKey() throws Exception {
		EhrInternalRouteClientAccessInterceptor interceptor = interceptor();
		List<String> routeKeys = guardedHandlerRouteKeys(interceptor);

		assertTrue("测试必须枚举到真实受守卫 HandlerMethod", !routeKeys.isEmpty());
		Set<String> distinctRouteKeys = new HashSet<>(routeKeys);
		assertEquals("所有 @Inner + @OpenApi(server) HandlerMethod 必须拥有不冲突的 route-client-ids 键",
				routeKeys.size(), distinctRouteKeys.size());
	}

	@Test
	public void everyProductionSmartDataFeignInvocationHasAnExactClientMapping() throws Exception {
		EhrInternalRouteClientAccessProperties properties = nacosTemplateProperties();
		EhrInternalRouteClientAccessInterceptor interceptor = new EhrInternalRouteClientAccessInterceptor(
				new OpenApiAuthenticationAdapter(), properties);
		Map<String, Method> guardedRoutes = guardedHandlerMethodsByEndpoint(interceptor);
		List<String> missingControllerRoutes = new ArrayList<>();
		List<String> missingClientMappings = new ArrayList<>();

		List<FeignInvocation> invocations = productionSmartDataFeignInvocations();
		assertTrue("测试必须从生产源码枚举到 SmartData Feign 调用", !invocations.isEmpty());
		for (FeignInvocation invocation : invocations) {
			String endpoint = endpoint(invocation.feignType, invocation.feignMethod);
			Method controllerMethod = guardedRoutes.get(endpoint);
			if (controllerMethod == null) {
				missingControllerRoutes.add(invocation.describe() + " -> " + endpoint);
				continue;
			}
			String configuredClientIds = properties.getRouteClientIds().get(interceptor.routeKey(controllerMethod));
			if (configuredClientIds == null || configuredClientIds.trim().isEmpty()) {
				missingClientMappings.add(controllerMethod.getDeclaringClass().getName() + "#"
						+ controllerMethod.getName() + " <- " + invocation.describe());
			} else if (!configuredClientIds.contains(expectedClientIdPlaceholder(invocation.sourcePath))) {
				missingClientMappings.add(controllerMethod.getDeclaringClass().getName() + "#"
						+ controllerMethod.getName() + " 缺少调用方 client_id "
						+ expectedClientIdPlaceholder(invocation.sourcePath) + " <- " + invocation.describe());
			}
		}

		assertTrue("生产 SmartData Feign 调用必须命中受守卫 Controller 路由: "
				+ missingControllerRoutes, missingControllerRoutes.isEmpty());
		assertTrue("每个真实生产 SmartData Feign 调用路由都必须有非空 route-client-ids 映射: " + missingClientMappings,
				missingClientMappings.isEmpty());
	}

	@Test
	public void routesWithoutProductionFeignInvocationRemainFailClosed() throws Exception {
		EhrInternalRouteClientAccessProperties properties = nacosTemplateProperties();
		EhrInternalRouteClientAccessInterceptor interceptor = new EhrInternalRouteClientAccessInterceptor(
				new OpenApiAuthenticationAdapter(), properties);
		String[][] routesWithoutCaller = {
				{ "com.tce.smart.data.controller.ehrview.CInterFaceBenSupplyController", "save" },
				{ "com.tce.smart.data.controller.ehrview.EvwAshiftRunNoController", "info" },
				{ "com.tce.smart.data.controller.ehrview.EvwBizLregleaveController", "info" },
				{ "com.tce.smart.data.controller.ehrview.EvwEappraisController", "info" },
				{ "com.tce.smart.data.controller.ehrview.EvwEmphrYsController", "getByCompId" },
				{ "com.tce.smart.data.controller.ehrview.LvwAcardlostController", "getByBadge" },
				{ "com.tce.smart.data.controller.ehrview.LvwAdjustbasicController", "info" },
				{ "com.tce.smart.data.controller.ehrview.OvwYsCallOwanceDetailsController", "getInfoByTime" },
				{ "com.tce.smart.data.controller.guard.VcallCarController", "getVcallCarPage" }
		};

		for (String[] route : routesWithoutCaller) {
			Class<?> controllerType = Class.forName(route[0]);
			Method method = Arrays.stream(controllerType.getDeclaredMethods()).filter(candidate -> route[1].equals(candidate.getName()))
					.findFirst().orElseThrow(() -> new AssertionError(route[0] + " 缺少方法 " + route[1]));
			assertTrue(route[0] + "#" + route[1] + " 必须仍受精确 client_id 守卫保护",
					interceptor.requiresExactClientGuard(method));
			assertFalse("没有生产 Feign 调用证据的路由必须保持 fail-closed: " + route[0] + "#" + route[1],
					properties.getRouteClientIds().containsKey(interceptor.routeKey(method)));
		}
	}

	@Test
	public void accessMatrixRejectsEveryBypassAndAllowsConfiguredServiceClient() throws Exception {
		EhrInternalRouteClientAccessInterceptor interceptor = interceptor();
		OpenApiInterceptor openApiInterceptor = new OpenApiInterceptor(new OpenApiAuthenticationAdapter());
		HandlerMethod handler = new HandlerMethod(new EvwEmphrYsController(),
				EvwEmphrYsController.class.getMethod("info", String.class));

		assertDeniedByExactGuard(interceptor, handler, null, SecurityConstants.FROM_IN);
		assertDeniedByExactGuard(interceptor, handler, new TestingAuthenticationToken("user", "credentials"),
				SecurityConstants.FROM_IN);
		SecurityContextHolder.getContext().setAuthentication(serverAuthentication("other-scope", ALLOWED_CLIENT));
		assertFalse("错误 scope 必须由统一 OpenApi 守卫拒绝",
				openApiInterceptor.preHandle(request(SecurityConstants.FROM_IN), new MockHttpServletResponse(), handler));
		assertDeniedByExactGuard(interceptor, handler, serverAuthentication("server", "unexpected-client"),
				SecurityConstants.FROM_IN);
		assertDeniedByExactGuard(interceptor, handler, serverAuthentication("server", ALLOWED_CLIENT), null);

		SecurityContextHolder.getContext().setAuthentication(serverAuthentication("server", ALLOWED_CLIENT));
		MockHttpServletRequest request = request(SecurityConstants.FROM_IN);
		assertTrue("配置白名单中的 server client 且带内部来源头必须通过 OpenApi 守卫",
				openApiInterceptor.preHandle(request, new MockHttpServletResponse(), handler));
		assertTrue("配置白名单中的 server client 且带内部来源头必须通过精确 client_id 守卫",
				interceptor.preHandle(request, new MockHttpServletResponse(), handler));
	}

	@Test
	public void emptyRouteMappingFailsClosed() throws Exception {
		EhrInternalRouteClientAccessInterceptor interceptor = new EhrInternalRouteClientAccessInterceptor(
				new OpenApiAuthenticationAdapter(), properties(Collections.emptyMap()));
		HandlerMethod handler = new HandlerMethod(new EvwEmphrYsController(),
				EvwEmphrYsController.class.getMethod("info", String.class));
		assertDeniedByExactGuard(interceptor, handler, serverAuthentication("server", ALLOWED_CLIENT),
				SecurityConstants.FROM_IN);
	}

	@Test
	public void routeClientMappingOnlyAllowsTheConfiguredClientForThatRoute() throws Exception {
		EhrInternalRouteClientAccessInterceptor interceptor = interceptor();
		HandlerMethod employeeInfo = new HandlerMethod(new EvwEmphrYsController(),
				EvwEmphrYsController.class.getMethod("info", String.class));
		HandlerMethod travelInfo = new HandlerMethod(new FormtableMainController(),
				FormtableMainController.class.getMethod("info", Page.class, CcdFormtableMainReqDTO.class));
		Method photoSaveMethod = EPhotoController.class.getDeclaredMethod("saveOrUpdatePhoto", SaveEPhotoReqDTO.class);
		HandlerMethod photoSave = new HandlerMethod(new EPhotoController(), photoSaveMethod);

		SecurityContextHolder.getContext().setAuthentication(serverAuthentication("server", ALLOWED_CLIENT));
		assertTrue("员工 EHR 查询的合法 smart-app 调用必须保留",
				interceptor.preHandle(request(SecurityConstants.FROM_IN), new MockHttpServletResponse(), employeeInfo));
		assertDeniedByExactGuard(interceptor, travelInfo, serverAuthentication("server", ALLOWED_CLIENT),
				SecurityConstants.FROM_IN);
		assertDeniedByExactGuard(interceptor, photoSave, serverAuthentication("server", ALLOWED_CLIENT),
				SecurityConstants.FROM_IN);
	}

	@Test
	public void routeClientMappingPreservesTheConfirmedPlatformPhotoWriteFeignCall() throws Exception {
		EhrInternalRouteClientAccessInterceptor interceptor = interceptor();
		Method photoSaveMethod = EPhotoController.class.getDeclaredMethod("saveOrUpdatePhoto", SaveEPhotoReqDTO.class);
		HandlerMethod photoSave = new HandlerMethod(new EPhotoController(), photoSaveMethod);

		SecurityContextHolder.getContext().setAuthentication(serverAuthentication("server", "smart-platform"));
		assertTrue("Platform 的 EHR 照片写入 Feign 调用必须命中其专属路由映射",
				interceptor.preHandle(request(SecurityConstants.FROM_IN), new MockHttpServletResponse(), photoSave));
	}

	@Test
	public void routeClientMappingPreservesConfirmedUpmsAndScheduleFeignCalls() throws Exception {
		EhrInternalRouteClientAccessInterceptor interceptor = interceptor();
		HandlerMethod employeeInfo = new HandlerMethod(new EvwEmphrYsController(),
				EvwEmphrYsController.class.getMethod("info", String.class));
		HandlerMethod dhrPage = new HandlerMethod(new YutoDhrPsndoController(),
				YutoDhrPsndoController.class.getMethod("page", Integer.class, Integer.class, List.class));

		SecurityContextHolder.getContext().setAuthentication(serverAuthentication("server", "smart-upms"));
		assertTrue("UPMS 的员工信息 Feign 调用必须保留", interceptor.preHandle(request(SecurityConstants.FROM_IN),
				new MockHttpServletResponse(), employeeInfo));
		SecurityContextHolder.getContext().setAuthentication(serverAuthentication("server", "smart-schedule"));
		assertTrue("Schedule 的 DHR 同步分页 Feign 调用必须保留", interceptor.preHandle(request(SecurityConstants.FROM_IN),
				new MockHttpServletResponse(), dhrPage));
	}

	@Test
	public void guardedOverloadsUseDifferentRouteClientMappings() throws Exception {
		EhrInternalRouteClientAccessInterceptor keyInterceptor = new EhrInternalRouteClientAccessInterceptor(
				new OpenApiAuthenticationAdapter(), properties(new HashMap<>()));
		Method singleSave = EleaveJjitemController.class.getDeclaredMethod("save", EleaveJjitemReqDTO.class);
		Method batchSave = EleaveJjitemController.class.getDeclaredMethod("save", List.class);

		assertTrue("两个 save 重载都必须属于精确 client_id 守卫范围",
				keyInterceptor.requiresExactClientGuard(singleSave) && keyInterceptor.requiresExactClientGuard(batchSave));
		assertNotEquals("同一 Controller 的受守卫重载不能共享 route-client-ids 配置键",
				keyInterceptor.routeKey(singleSave), keyInterceptor.routeKey(batchSave));

		Map<String, String> routeClientIds = new HashMap<>();
		routeClientIds.put(keyInterceptor.routeKey(singleSave), "single-client");
		routeClientIds.put(keyInterceptor.routeKey(batchSave), "batch-client");
		EhrInternalRouteClientAccessInterceptor interceptor = new EhrInternalRouteClientAccessInterceptor(
				new OpenApiAuthenticationAdapter(), properties(routeClientIds));
		HandlerMethod singleHandler = new HandlerMethod(new EleaveJjitemController(), singleSave);
		HandlerMethod batchHandler = new HandlerMethod(new EleaveJjitemController(), batchSave);

		SecurityContextHolder.getContext().setAuthentication(serverAuthentication("server", "single-client"));
		assertTrue("单条保存只能使用单条保存路由的专属 client_id",
				interceptor.preHandle(request(SecurityConstants.FROM_IN), new MockHttpServletResponse(), singleHandler));
		assertDeniedByExactGuard(interceptor, batchHandler, serverAuthentication("server", "single-client"),
				SecurityConstants.FROM_IN);
	}

	@Test
	public void refreshedAllowlistTakesEffectWithoutRestartingTheGuard() throws Exception {
		EhrInternalRouteClientAccessProperties properties = properties(Collections.emptyMap());
		EhrInternalRouteClientAccessInterceptor interceptor = new EhrInternalRouteClientAccessInterceptor(
				new OpenApiAuthenticationAdapter(), properties);
		HandlerMethod handler = new HandlerMethod(new EvwEmphrYsController(),
				EvwEmphrYsController.class.getMethod("info", String.class));

		assertDeniedByExactGuard(interceptor, handler, serverAuthentication("server", ALLOWED_CLIENT),
				SecurityConstants.FROM_IN);
		properties.setRouteClientIds(routeClientIds(ALLOWED_CLIENT));
		SecurityContextHolder.getContext().setAuthentication(serverAuthentication("server", ALLOWED_CLIENT));
		assertTrue("刷新后的白名单必须立即被守卫使用",
				interceptor.preHandle(request(SecurityConstants.FROM_IN), new MockHttpServletResponse(), handler));
		properties.setRouteClientIds(Collections.emptyMap());
		assertDeniedByExactGuard(interceptor, handler, serverAuthentication("server", ALLOWED_CLIENT),
				SecurityConstants.FROM_IN);
		assertTrue("白名单属性必须是 Nacos 可刷新配置",
				EhrInternalRouteClientAccessProperties.class.isAnnotationPresent(RefreshScope.class));
	}

	@Test
	public void nacosTemplateBindsBracketedHandlerMethodKeysToExactRoutes() throws Exception {
		EhrInternalRouteClientAccessProperties properties = nacosTemplateProperties();
		EhrInternalRouteClientAccessInterceptor interceptor = new EhrInternalRouteClientAccessInterceptor(
				new OpenApiAuthenticationAdapter(), properties);
		Method employeeInfo = EvwEmphrYsController.class.getMethod("info", String.class);
		Method photoSave = EPhotoController.class.getDeclaredMethod("saveOrUpdatePhoto", SaveEPhotoReqDTO.class);
		Method singleSave = EleaveJjitemController.class.getDeclaredMethod("save", EleaveJjitemReqDTO.class);
		Method batchSave = EleaveJjitemController.class.getDeclaredMethod("save", List.class);

		assertEquals("带 [] 的员工查询键必须绑定到真实 HandlerMethod routeKey",
				"${SMART_APP_OAUTH_CLIENT_ID:},${SMART_PLATFORM_OAUTH_CLIENT_ID:},${SMART_UPMS_OAUTH_CLIENT_ID:}",
				properties.getRouteClientIds().get(interceptor.routeKey(employeeInfo)));
		assertEquals("带 [] 的照片写入键必须绑定到真实 HandlerMethod routeKey",
				"${SMART_PLATFORM_OAUTH_CLIENT_ID:}", properties.getRouteClientIds().get(interceptor.routeKey(photoSave)));
		assertEquals("重载的单条保存键必须绑定到带参数类型的 routeKey", "${SMART_PLATFORM_OAUTH_CLIENT_ID:}",
				properties.getRouteClientIds().get(interceptor.routeKey(singleSave)));
		assertEquals("重载的批量保存键必须绑定到带参数类型的 routeKey", "${SMART_PLATFORM_OAUTH_CLIENT_ID:}",
				properties.getRouteClientIds().get(interceptor.routeKey(batchSave)));
	}

	private EhrInternalRouteClientAccessInterceptor interceptor() {
		return new EhrInternalRouteClientAccessInterceptor(new OpenApiAuthenticationAdapter(),
				properties(routeClientIds(ALLOWED_CLIENT + ",smart-platform,smart-upms")));
	}

	private EhrInternalRouteClientAccessProperties properties(Map<String, String> routeClientIds) {
		EhrInternalRouteClientAccessProperties properties = new EhrInternalRouteClientAccessProperties();
		properties.setRouteClientIds(routeClientIds);
		return properties;
	}

	private Map<String, String> routeClientIds(String employeeInfoClientIds) {
		Map<String, String> routeClientIds = new HashMap<>();
		routeClientIds.put(routeKey(EvwEmphrYsController.class, "info"), employeeInfoClientIds);
		routeClientIds.put(routeKey(FormtableMainController.class, "info"), "smart-platform");
		routeClientIds.put(routeKey(EPhotoController.class, "saveOrUpdatePhoto"), "smart-platform");
		routeClientIds.put(routeKey(YutoDhrPsndoController.class, "page"), "smart-schedule");
		return routeClientIds;
	}

	private String routeKey(Class<?> controllerType, String methodName) {
		return controllerType.getName() + "#" + methodName;
	}

	private void assertDeniedByExactGuard(EhrInternalRouteClientAccessInterceptor interceptor, HandlerMethod handler,
			org.springframework.security.core.Authentication authentication, String from) throws Exception {
		SecurityContextHolder.getContext().setAuthentication(authentication);
		MockHttpServletResponse response = new MockHttpServletResponse();
		assertFalse(interceptor.preHandle(request(from), response, handler));
		assertTrue("精确 client_id 守卫拒绝时必须返回 403", response.getStatus() == 403);
	}

	private MockHttpServletRequest request(String from) {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/emphr/ys/info");
		if (from != null) {
			request.addHeader(SecurityConstants.FROM, from);
		}
		return request;
	}

	private OAuth2Authentication serverAuthentication(String scope, String clientId) {
		OAuth2Request request = new OAuth2Request(Collections.emptyMap(), clientId, Collections.emptyList(), true,
				Collections.singleton(scope), Collections.emptySet(), null, Collections.emptySet(), Collections.emptyMap());
		return new OAuth2Authentication(request, null);
	}

	private EhrInternalRouteClientAccessProperties nacosTemplateProperties() throws IOException {
		Path template = locateRepositoryRoot().resolve("docker/nacos/config/dev/smart-data.yml");
		List<PropertySource<?>> propertySources = new YamlPropertySourceLoader().load("smart-data",
				new FileSystemResource(template.toFile()));
		return new Binder(ConfigurationPropertySources.from(propertySources)).bind("security.inner.ehr",
					Bindable.of(EhrInternalRouteClientAccessProperties.class)).orElseThrow(
							() -> new AssertionError("Nacos 模板必须绑定 security.inner.ehr.route-client-ids"));
	}

	private int countRealInnerServerRoutes() throws IOException {
		Path controllerRoot = locateRepositoryRoot().resolve(
				"smart-module/smart-data/smart-data-biz/src/main/java/com/tce/smart/data/controller");
		try (Stream<Path> paths = Files.walk(controllerRoot)) {
			int routeCount = 0;
			for (Path controller : paths.filter(path -> path.toString().endsWith("Controller.java"))
					.collect(Collectors.toList())) {
				String source = new String(Files.readAllBytes(controller), java.nio.charset.StandardCharsets.UTF_8);
				Matcher matcher = INNER_SERVER_ROUTE.matcher(source.replace("\r\n", "\n"));
				while (matcher.find()) {
					routeCount++;
				}
			}
			return routeCount;
		}
	}

	/** 从编译后的真实 Controller 枚举受守卫方法，避免仅靠源码字符串漏掉 Java 重载。 */
	private List<String> guardedHandlerRouteKeys(EhrInternalRouteClientAccessInterceptor interceptor) throws Exception {
		List<String> routeKeys = new ArrayList<>();
		for (Method method : guardedHandlerMethodsByEndpoint(interceptor).values()) {
			routeKeys.add(interceptor.routeKey(method));
		}
		return routeKeys;
	}

	/** 将真实内部 Controller 路由按 HTTP 方法和路径索引，供 Feign 合同精确比对。 */
	private Map<String, Method> guardedHandlerMethodsByEndpoint(EhrInternalRouteClientAccessInterceptor interceptor)
			throws Exception {
		Path controllerRoot = locateRepositoryRoot().resolve(
				"smart-module/smart-data/smart-data-biz/src/main/java/com/tce/smart/data/controller");
		Pattern packagePattern = Pattern.compile("(?m)^package\\s+([\\w.]+);");
		Pattern controllerPattern = Pattern.compile("(?m)^public\\s+class\\s+(\\w+)");
		Map<String, Method> guardedRoutes = new HashMap<>();
		try (Stream<Path> paths = Files.walk(controllerRoot)) {
			for (Path controller : paths.filter(path -> path.toString().endsWith("Controller.java"))
					.collect(Collectors.toList())) {
				String source = withoutComments(new String(Files.readAllBytes(controller),
						java.nio.charset.StandardCharsets.UTF_8));
				Matcher packageMatcher = packagePattern.matcher(source);
				Matcher controllerMatcher = controllerPattern.matcher(source);
				if (!packageMatcher.find() || !controllerMatcher.find()) {
					if (!source.contains("@OpenApi")) {
						continue;
					}
					throw new AssertionError("无法从 Controller 源码解析类名: " + controller);
				}
				Class<?> controllerType = Class.forName(packageMatcher.group(1) + "." + controllerMatcher.group(1), false,
						getClass().getClassLoader());
				for (Method method : controllerType.getDeclaredMethods()) {
					if (interceptor.requiresExactClientGuard(method)) {
						String endpoint = endpoint(controllerType, method);
						if (endpoint == null) {
							throw new AssertionError("受守卫 Controller 缺少 HTTP 映射: " + controllerType.getName() + "#"
									+ method.getName());
						}
						Method duplicate = guardedRoutes.put(endpoint, method);
						if (duplicate != null) {
							throw new AssertionError("多个受守卫 Controller 共享 HTTP 路由: " + endpoint);
						}
					}
				}
			}
		}
		return guardedRoutes;
	}

	/** 枚举 SmartData API 中已声明服务认证头的 Feign 合同类型。 */
	private List<Class<?>> smartDataFeignContractTypes() throws Exception {
		Path feignRoot = locateRepositoryRoot().resolve(
				"smart-module/smart-data/smart-data-api/src/main/java/com/tce/smart/data/api/feign");
		Pattern packagePattern = Pattern.compile("(?m)^package\\s+([\\w.]+);");
		Pattern interfacePattern = Pattern.compile("(?m)^public\\s+interface\\s+(\\w+)");
		List<Class<?>> feignTypes = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(feignRoot)) {
			for (Path feignSource : paths.filter(path -> path.toString().endsWith(".java"))
					.collect(Collectors.toList())) {
				String source = new String(Files.readAllBytes(feignSource), java.nio.charset.StandardCharsets.UTF_8);
				if (!source.contains("@FeignClient") || !source.contains("SecurityConstants.INTERNAL_SERVICE_AUTH")) {
					continue;
				}
				Matcher packageMatcher = packagePattern.matcher(source);
				Matcher interfaceMatcher = interfacePattern.matcher(source);
				if (!packageMatcher.find() || !interfaceMatcher.find()) {
					throw new AssertionError("无法从 Feign 合同源码解析接口名: " + feignSource);
				}
				feignTypes.add(Class.forName(packageMatcher.group(1) + "." + interfaceMatcher.group(1), false,
						getClass().getClassLoader()));
			}
		}
		return feignTypes;
	}

	/**
	 * 从生产源码中找出对 SmartData Feign 字段的实际调用；仅有接口声明或注释不会被视为调用证据。
	 */
	private List<FeignInvocation> productionSmartDataFeignInvocations() throws Exception {
		Map<String, Class<?>> feignTypesBySimpleName = smartDataFeignContractTypes().stream()
				.collect(Collectors.toMap(Class::getSimpleName, type -> type));
		Pattern remoteFieldPattern = Pattern.compile("\\b(Remote\\w+)\\s+(\\w+)\\b");
		Set<String> invocationKeys = new HashSet<>();
		List<FeignInvocation> invocations = new ArrayList<>();
		for (Path sourceRoot : Arrays.asList(locateRepositoryRoot().resolve("smart"),
				locateRepositoryRoot().resolve("smart-module"))) {
			try (Stream<Path> paths = Files.walk(sourceRoot)) {
				for (Path sourcePath : paths.filter(path -> path.toString().endsWith(".java"))
						.filter(path -> !path.toString().contains("/src/test/"))
						.filter(path -> !path.toString().contains("/smart-module/smart-data/"))
						.collect(Collectors.toList())) {
					String source = withoutComments(new String(Files.readAllBytes(sourcePath),
							java.nio.charset.StandardCharsets.UTF_8));
					Matcher fieldMatcher = remoteFieldPattern.matcher(source);
					while (fieldMatcher.find()) {
						Class<?> feignType = feignTypesBySimpleName.get(fieldMatcher.group(1));
						if (feignType == null) {
							continue;
						}
						Pattern invocationPattern = Pattern.compile("\\b(?:this\\s*\\.\\s*)?"
								+ Pattern.quote(fieldMatcher.group(2)) + "\\s*\\.\\s*(\\w+)\\s*\\(");
						Matcher invocationMatcher = invocationPattern.matcher(source);
						while (invocationMatcher.find()) {
							String methodName = invocationMatcher.group(1);
							for (Method feignMethod : feignType.getDeclaredMethods()) {
								if (!methodName.equals(feignMethod.getName()) || !hasInternalServiceAuthHeader(feignMethod)) {
									continue;
								}
								String key = sourcePath + "|" + feignType.getName() + "|" + feignMethod.toGenericString();
								if (invocationKeys.add(key)) {
									invocations.add(new FeignInvocation(sourcePath, feignType, feignMethod));
								}
							}
						}
					}
				}
			}
		}
		return invocations;
	}

	private String withoutComments(String source) {
		return source.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("(?m)//.*$", "");
	}

	/** 生产调用方模块决定必须出现的专属 OAuth client_id，避免仅验证“任意非空映射”。 */
	private String expectedClientIdPlaceholder(Path sourcePath) {
		String normalizedPath = sourcePath.toString().replace('\\', '/');
		if (normalizedPath.contains("/smart-module/smart-app/")) {
			return "${SMART_APP_OAUTH_CLIENT_ID:}";
		}
		if (normalizedPath.contains("/smart-module/smart-platform/")) {
			return "${SMART_PLATFORM_OAUTH_CLIENT_ID:}";
		}
		if (normalizedPath.contains("/smart/smart-upms/")) {
			return "${SMART_UPMS_OAUTH_CLIENT_ID:}";
		}
		if (normalizedPath.contains("/smart-module/smart-schedule/")) {
			return "${SMART_SCHEDULE_OAUTH_CLIENT_ID:}";
		}
		throw new AssertionError("未声明调用方 OAuth client_id 约定的生产模块: " + sourcePath);
	}

	private boolean hasInternalServiceAuthHeader(Method method) {
		return Arrays.stream(method.getParameterAnnotations()).flatMap(Arrays::stream)
				.filter(RequestHeader.class::isInstance).map(RequestHeader.class::cast)
				.anyMatch(header -> SecurityConstants.INTERNAL_SERVICE_AUTH.equals(header.value()));
	}

	/** 统一提取 Controller 或 Feign 方法的 HTTP 方法和完整路径，避免按方法名猜测对应关系。 */
	private String endpoint(Class<?> type, Method method) {
		String methodPath = mappingPath(method);
		if (methodPath == null) {
			return null;
		}
		String classPath = mappingPath(type);
		return mappingHttpMethod(method) + " " + normalizePath(classPath, methodPath);
	}

	private String mappingHttpMethod(Method method) {
		if (method.isAnnotationPresent(GetMapping.class)) {
			return "GET";
		}
		if (method.isAnnotationPresent(PostMapping.class)) {
			return "POST";
		}
		if (method.isAnnotationPresent(PutMapping.class)) {
			return "PUT";
		}
		if (method.isAnnotationPresent(DeleteMapping.class)) {
			return "DELETE";
		}
		if (method.isAnnotationPresent(PatchMapping.class)) {
			return "PATCH";
		}
		return "REQUEST";
	}

	private String mappingPath(java.lang.reflect.AnnotatedElement element) {
		RequestMapping requestMapping = element.getAnnotation(RequestMapping.class);
		if (requestMapping != null) {
			return firstMappingPath(requestMapping.value(), requestMapping.path());
		}
		GetMapping getMapping = element.getAnnotation(GetMapping.class);
		if (getMapping != null) {
			return firstMappingPath(getMapping.value(), getMapping.path());
		}
		PostMapping postMapping = element.getAnnotation(PostMapping.class);
		if (postMapping != null) {
			return firstMappingPath(postMapping.value(), postMapping.path());
		}
		PutMapping putMapping = element.getAnnotation(PutMapping.class);
		if (putMapping != null) {
			return firstMappingPath(putMapping.value(), putMapping.path());
		}
		DeleteMapping deleteMapping = element.getAnnotation(DeleteMapping.class);
		if (deleteMapping != null) {
			return firstMappingPath(deleteMapping.value(), deleteMapping.path());
		}
		PatchMapping patchMapping = element.getAnnotation(PatchMapping.class);
		if (patchMapping != null) {
			return firstMappingPath(patchMapping.value(), patchMapping.path());
		}
		return null;
	}

	private String firstMappingPath(String[] values, String[] paths) {
		String[] candidates = values.length > 0 ? values : paths;
		if (candidates.length != 1) {
			throw new AssertionError("精确 client_id 守卫不支持空或多路径映射");
		}
		return candidates[0];
	}

	private String normalizePath(String classPath, String methodPath) {
		String prefix = classPath == null ? "" : classPath;
		return ("/" + prefix + "/" + methodPath).replaceAll("/{2,}", "/");
	}

	private static class FeignInvocation {
		private final Path sourcePath;
		private final Class<?> feignType;
		private final Method feignMethod;

		private FeignInvocation(Path sourcePath, Class<?> feignType, Method feignMethod) {
			this.sourcePath = sourcePath;
			this.feignType = feignType;
			this.feignMethod = feignMethod;
		}

		private String describe() {
			return sourcePath + ": " + feignType.getName() + "#" + feignMethod.getName();
		}
	}

	private Path locateRepositoryRoot() {
		Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
		while (current != null) {
			if (Files.isRegularFile(current.resolve("docker/nacos/config/dev/smart-data.yml"))) {
				return current;
			}
			current = current.getParent();
		}
		throw new IllegalStateException("无法定位仓库根目录");
	}
}
