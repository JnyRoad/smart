package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.DeviceAuthRelationDelReqDTO;
import com.tce.smart.platform.dto.authoperation.AuthOperationReceipt;
import com.tce.smart.platform.dto.authoperation.AuthOperationIntakeCapability;
import com.tce.smart.platform.service.impl.EmployeeAuthIntakeService;
import com.tce.smart.platform.service.SmtDeviceAuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import javax.validation.Valid;
import java.util.List;

/** 人员回执使用独立路由和异常响应，保留旧 Boolean 接口契约。 */
@RestController
@lombok.extern.slf4j.Slf4j
@RequiredArgsConstructor
@RequestMapping("/device/authority/relation/person")
public class EmployeeAuthOperationReceiptController {
 private final SmtDeviceAuthorityService service;


 public Result<AuthOperationReceipt> delete(@RequestBody @Valid DeviceAuthRelationDelReqDTO request) {
  return new Result<>(service.personRelationDeleteReceipt(request, allowedParks()));
 }

 public Result<AuthOperationReceipt> clear(@PathVariable("id") Integer id) {
  return new Result<>(service.personRelationClearReceipt(id, allowedParks()));
 }
 @PostMapping("/del/receipt")
 public Result<?> delete(@RequestBody @Valid DeviceAuthRelationDelReqDTO request,
   @RequestHeader(value="Idempotency-Key",required=false) String requestKey) {
  if(requestKey==null)return delete(request);
  List<Integer> parks=allowedParks();
  return new Result<>(service.personRelationDeleteIntake(request,requestKey,SecurityUtils.getUser().getId(),parks));
 }
 @PostMapping("/clear/{id}/receipt")
 public Result<?> clear(@PathVariable("id") Integer id,
   @RequestHeader(value="Idempotency-Key",required=false) String requestKey) {
  if(requestKey==null)return clear(id);
  List<Integer> parks=allowedParks();
  return new Result<>(service.personRelationClearIntake(id,requestKey,SecurityUtils.getUser().getId(),parks));
 }
 @GetMapping("/{id}/intake-capability")
 public Result<AuthOperationIntakeCapability> capability(@PathVariable("id") Integer id) {
  return new Result<>(service.personIntakeCapability(id,allowedParks()));
 }
 @ExceptionHandler(EmployeeAuthIntakeService.IntakeException.class)
 @ResponseStatus(HttpStatus.CONFLICT)
 public Result intakeConflict(EmployeeAuthIntakeService.IntakeException error) {
  String code=error.getCode();
  String message="KEYED_UNSUPPORTED".equals(code)?"当前园区未启用可靠受理，本次未受理；请保留请求键核对":
    "KEY_PAYLOAD_CONFLICT".equals(code)?"同一请求键的内容不同，请使用原请求核对":
    "受理记录不完整，请保留请求键并联系管理员核验";
  return Result.fail(409,code+"："+message);
 }
 private List<Integer> allowedParks() {
  if (SecurityUtils.getAuthentication() == null || SecurityUtils.getUser() == null
    || SecurityUtils.getUser().getParkIdList() == null || SecurityUtils.getUser().getParkIdList().isEmpty())
   throw new SecurityException("缺少明确的允许园区范围");
  return SecurityUtils.getUser().getParkIdList();
 }

 /** 本地处理器优先于全局 Advice，校验失败不能返回 HTTP 200。 */
 @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class,
   HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
 @ResponseStatus(HttpStatus.BAD_REQUEST)
 public Result invalid(Exception error) {
  return Result.fail(400, "人员权限请求参数无效，请刷新并核对所选来源");
 }
 @ExceptionHandler(SecurityException.class)
 @ResponseStatus(HttpStatus.FORBIDDEN)
 public Result forbidden(SecurityException error) {
  return Result.fail(403, "无人员权限操作范围");
 }
 @ExceptionHandler(IllegalStateException.class)
 @ResponseStatus(HttpStatus.CONFLICT)
 public Result conflict(IllegalStateException error) {
  return Result.fail(409, "操作状态冲突，请先核对权限任务再操作");
 }
 @ExceptionHandler(Exception.class)
 @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
 public Result unknown(Exception error) {
  // 只保留异常诊断栈，不记录请求正文、人员资料或凭据。
  log.error("人员权限受理发生非预期异常", error);
  return Result.fail(500, "提交结果未确认，请先核对权限任务再操作");
 }
}
