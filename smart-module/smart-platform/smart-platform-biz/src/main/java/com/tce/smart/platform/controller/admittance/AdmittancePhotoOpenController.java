package com.tce.smart.platform.controller.admittance;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.platform.service.admittance.AdmittancePhotoOpenService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 入厂申请照片开放接口（开放 API，应用凭证专用）。
 * 消费方：许昌 FileReceiver 拉取客户端（照片拉取 spec §3.1/§3.2）。
 * 安全约定：
 * - 两个接口均标注 @OpenApi，只接受携带 open:admittance:photo:read scope 的应用 token；
 * - 园区数据范围只从 token 的 app_park_ids claim 推导，不接受任何请求参数指定范围；
 * - 本控制器路由不得加入 PermitAllUrlProperties 白名单（必须带 token）——因此路由前缀
 *   使用 /open/admittance/photo 而非 /admittance/photo：Nacos ignore-urls 中存在历史条目
 *   "/admittance/**"（H5 访客自助流程用），若落在其下会在 Security 过滤器层被 permitAll，
 *   纵深防御只剩 MVC 拦截器一层，违反开放 API spec 硬约束。
 */
@RestController
@AllArgsConstructor
@RequestMapping("/open/admittance/photo")
public class AdmittancePhotoOpenController extends BaseController {

	/** photoId 只允许标准 UUID 分组格式（8-4-4-4-12），防路径穿越与遍历式枚举；下游为参数化查询，此处为纵深防御第一层 */
	private static final Pattern PHOTO_ID_PATTERN = Pattern
			.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

	private final AdmittancePhotoOpenService admittancePhotoOpenService;

	private final OpenApiAuthenticationAdapter openApiAuthenticationAdapter;

	/**
	 * 待拉取照片ID清单（园区范围=应用绑定范围）。
	 */
	@OpenApi("open:admittance:photo:read")
	@GetMapping("/pending")
	public Result<List<String>> pending() {
		List<Integer> allowedParkIds = openApiAuthenticationAdapter
				.appParkIds(SecurityContextHolder.getContext().getAuthentication());
		return success(admittancePhotoOpenService.listPendingPhotoIds(allowedParkIds));
	}

	/**
	 * 按 photoId 下载照片二进制（image/png）。
	 * 400=photoId 非法；404=无该图片（数据质量问题，客户端跳过不重试循环）。
	 */
	@OpenApi("open:admittance:photo:read")
	@GetMapping("/download/{photoId}")
	public ResponseEntity<byte[]> download(@PathVariable("photoId") String photoId) {
		if (photoId == null || !PHOTO_ID_PATTERN.matcher(photoId).matches()) {
			// 非法入参不回显，避免反射式注入日志/响应
			return ResponseEntity.badRequest().build();
		}
		byte[] bytes = admittancePhotoOpenService.loadPhoto(photoId);
		if (bytes == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(bytes);
	}
}
