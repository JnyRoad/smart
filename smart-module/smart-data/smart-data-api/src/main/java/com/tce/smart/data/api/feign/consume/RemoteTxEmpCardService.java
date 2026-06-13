package com.tce.smart.data.api.feign.consume;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.consume.resp.TxEmpCardRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

/**
 * 厂牌信息
 *
 * @author fushiping
 * @date 2020-7-9
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteTxEmpCardService {

	/**
	 * 根据卡号获得厂牌信息
	 *
	 * @param cardDispNo 工号
	 * @return ture-成功,false-失败
	 */
	@GetMapping("/txemp/inner/info")
	Result<TxEmpCardRespDTO> getCard(@RequestParam("cardDispNo") String cardDispNo,
									 @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 厂牌挂失
	 *
	 * @param cardId
	 * @param from
	 * @return
	 */
	@GetMapping("/txemp/inner/loss")
	Result<Boolean> cardLoss(@RequestParam("cardId") Long cardId,
									 @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 查询实发金额
	 * @param empNo
	 * @return
	 */
	@GetMapping("/txemp/inner/getActPutMoney")
	Result<Double> getActPutMoney(@RequestParam("empNo")String empNo, @RequestParam("startDate")Date startDate,
								  @RequestParam("endDate")Date endDate,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 查询公司充值剩余金额
	 * @param empNo
	 * @return
	 */
	@GetMapping("/txemp/inner/getCompBalance")
	Result<Double> getCompBalance(@RequestParam("empNo")String empNo,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 查询个人充值剩余金额
	 * @param empNo
	 * @return
	 */
	@GetMapping("/txemp/inner/getPersonalBalance")
	Result<Double> getPersonalBalance(@RequestParam("empNo")String empNo,@RequestHeader(SecurityConstants.FROM) String from);
}
