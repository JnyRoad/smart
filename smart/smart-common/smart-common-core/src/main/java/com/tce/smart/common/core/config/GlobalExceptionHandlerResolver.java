package com.tce.smart.common.core.config;

import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Objects;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerResolver {

    /**
     * 全局异常.
     *
     * @param e the e
     * @return Result
     */
    @ExceptionHandler(Exception.class)
	@ResponseBody
   // @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleGlobalException(Exception e) {
        log.error("全局异常信息 ex={}", e.getMessage(), e);
        return Result.builder()
                .msg(e.getLocalizedMessage())
			//	.msg("请求异常")
                .code(CommonConstants.FAIL)
                .build();
    }

    /**
     * TCEException异常.
     *
     * @param e the e
     * @return Result
     */
    @ExceptionHandler(TCEException.class)
	@ResponseBody
    public Result handleTCEException(TCEException e) {
        log.error("TCEException异常信息 ex={}", e.getMessage(), e);
        return Result.builder()
				.msg(e.getLocalizedMessage())
				.code(e.getCode())
				.build();
    }

    /**
     * AccessDeniedException
     *
     * @param e the e
     * @return Result
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result handleAccessDeniedException(AccessDeniedException e) {
        String msg = SpringSecurityMessageSource.getAccessor()
                .getMessage("AbstractAccessDecisionManager.accessDenied"
                        , e.getMessage());
        log.error("拒绝授权异常信息 ex={}", msg, e);
        return Result.builder()
                .msg(msg)
                .code(CommonConstants.FAIL)
                .build();
    }

	/**
	 * BadCredentialsException
	 *
	 * @param e the e
	 * @return Result
	 */
	@ExceptionHandler(BadCredentialsException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public Result handleSecurityException(BadCredentialsException e) {
		log.error("账号密码错误", e);
		return Result.builder()
				.msg("账号密码错误")
				.code(CommonConstants.FAIL)
				.build();
	}

    /**
     * validation Exception
     *
     * @param exception
     * @return Result
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleBodyValidException(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        log.error("参数绑定异常,ex = {}", fieldErrors.get(0).getDefaultMessage());
        return Result.builder()
                .msg(fieldErrors.get(0).getDefaultMessage())
                .code(CommonConstants.FAIL)
                .build();
    }

	/**
	 * BindException
	 * @param exception
	 * @return
	 */
	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result handleBindException(BindException exception) {
		List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
		log.error("参数绑定异常,ex = {}", fieldErrors.get(0).getDefaultMessage());
		return Result.builder()
				.msg(fieldErrors.get(0).getDefaultMessage())
				.code(CommonConstants.FAIL)
				.build();
	}

	/**
	 * validation Exception
	 *
	 * @param exception
	 * @return Result
	 */
	@ExceptionHandler(DataAccessException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result handleSqlException(DataAccessException exception) {
		log.error("sql执行异常，ex = {}", exception.getMessage(), exception);
		return Result.fail("数据操作异常");
	}
}
