package com.tce.smart.platform.service.leavecount.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementCountReqDTO;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementInfoDhrRespDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementLog;
import com.tce.smart.platform.core.mapper.leavecount.SmtSettlementLogMapper;
import com.tce.smart.platform.service.leavecount.SmtSettlementLogService;
import net.sf.json.JSONArray;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:02:04
 */
@Service
public class SmtSettlementLogServiceImpl extends ServiceImpl<SmtSettlementLogMapper, SmtSettlementLog> implements SmtSettlementLogService {

	@Override
	public Boolean saveLog(Result<SettlementInfoDhrRespDTO> result, SettlementCountReqDTO smtSettlementInfo) {
		HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String requestIp = getIpAddr(httpServletRequest);
		SmtSettlementLog log = SmtSettlementLog.builder()
				.requestLog(JSONArray.fromObject(smtSettlementInfo).toString())
				.requestIp(requestIp)
				.requestName(requestIp)
				.responseDesc(result.getMessage())
				.responseStatus(result.getCode().toString())
				.responseTime(LocalDateTime.now()).build();
		if(Objects.nonNull(result.getData()) && result.isSuccess()) {
			SettlementInfoDhrRespDTO infoDhrRespDTO = result.getData();
			log.setRequestTime(DateUtils.parseLocalDateTime(infoDhrRespDTO.getCreateTime()));
			log.setResponseLog(JSONArray.fromObject(infoDhrRespDTO).toString());
			log.setInfoId(Long.parseLong(infoDhrRespDTO.getNum()));
		}
		return this.save(log);
	}

	private String getIpAddr(HttpServletRequest request) {
		String ipAddress = request.getHeader("x-forwarded-for");
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
			String localIp = "127.0.0.1";
			String localIpv6 = "0:0:0:0:0:0:0:1";
			if (ipAddress.equals(localIp) || ipAddress.equals(localIpv6)) {
				// 根据网卡取本机配置的IP
				InetAddress inet = null;
				try {
					inet = InetAddress.getLocalHost();
					ipAddress = inet.getHostAddress();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		}
		// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
		String ipSeparate = ",";
		int ipLength = 15;
		if (ipAddress != null && ipAddress.length() > ipLength) {
			if (ipAddress.indexOf(ipSeparate) > 0) {
				ipAddress = ipAddress.substring(0, ipAddress.indexOf(ipSeparate));
			}
		}
		return ipAddress;
	}
}
