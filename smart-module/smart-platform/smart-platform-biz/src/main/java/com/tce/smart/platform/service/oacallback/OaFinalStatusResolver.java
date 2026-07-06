package com.tce.smart.platform.service.oacallback;

import cn.hutool.core.util.StrUtil;
import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import com.tce.smart.tool.enums.ApproveListStateEnum;
import com.tce.smart.tool.enums.OaFinalStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * OA 流程终态解析：按 OPERATEDATE+OPERATETIME 排序取最新记录的 CURRENTNODETYPE 判定（spec §3.1.2）。
 * 3=归档→通过；0=退回→拒绝；其余（审批中/空/查询失败）→ null 表示尚无终态。
 */
@Component
public class OaFinalStatusResolver {

	public Integer resolve(WorkFlowLogDTO dto) {
		if (Objects.isNull(dto) || !dto.success()) {
			return null;
		}
		List<WorkFlowLogDataDTO> data = dto.getResultdata();
		if (data == null || data.isEmpty()) {
			return null;
		}
		// 按操作时间升序排序（时间字符串可比较），时间相同保持原顺序（稳定排序），取最新一条
		WorkFlowLogDataDTO latest = data.stream()
				.sorted(Comparator.comparing(this::operateDateTime))
				.reduce((a, b) -> b).orElse(null);
		if (latest == null) {
			return null;
		}
		if (OaFinalStatusEnum.CAUSE_3.getCode().toString().equals(latest.getCURRENTNODETYPE())) {
			return ApproveListStateEnum.AGREE.getCode();
		}
		if (OaFinalStatusEnum.CAUSE_0.getCode().toString().equals(latest.getCURRENTNODETYPE())) {
			return ApproveListStateEnum.REFUSE.getCode();
		}
		return null;
	}

	/**
	 * 拼接可字典序比较的时间串；缺失时间的记录排最前（视为最旧）
	 * 前提：OA 返回固定宽度 yyyy-MM-dd / HH:mm:ss（零填充），字典序比较即时间序；该前提与生产中入厂对账的既有用法一致
	 */
	private String operateDateTime(WorkFlowLogDataDTO d) {
		String date = StrUtil.nullToEmpty(d.getOPERATEDATE());
		String time = StrUtil.nullToEmpty(d.getOPERATETIME());
		return date + " " + time;
	}
}
