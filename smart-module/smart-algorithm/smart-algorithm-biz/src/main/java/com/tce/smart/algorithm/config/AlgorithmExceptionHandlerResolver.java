package com.tce.smart.algorithm.config;

/**
 * @ClassName: VerifyExceptionHandlerResolver
 * @Package com.tce.smart.business.config
 * @Description:
 * @Author wuxinjian
 * @Date 2020/6/9 11:08
 * @Version V1.0
 */

import cn.hutool.json.JSONUtil;
import com.tce.smart.algorithm.exception.AlgorithmException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.WebUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 全局异常处理器
 * @author wxjason
 */
@Slf4j
@AllArgsConstructor
@RestControllerAdvice
public class AlgorithmExceptionHandlerResolver extends com.tce.smart.common.core.config.GlobalExceptionHandlerResolver {

	/**
	 * AlgorithmException
	 *
	 * @param e the e
	 * @return Result
	 */
	@ExceptionHandler(AlgorithmException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public void handleAccessDeniedException(AlgorithmException e) {
		log.error("算法异常:code={}, message={}", e.getCode(), e.getMessage());
		Result result = new Result();
		result.setCode(e.getCode());
		result.setMsg(e.getMessage());
		process(JSONUtil.toJsonStr(result));
	}

	private void process(String result) {
		try {
			HttpServletResponse response = WebUtils.getResponse();
			response.setContentType("application/json;charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(result);
		} catch (IOException e) {
			log.error("网络通信异常:", e);
		}
	}
}
