package com.tce.smart.platform.wrapper;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.BackFactoryConfirmListDTO;
import com.tce.smart.platform.core.entity.SmtArticlesRelease;
import com.tce.smart.platform.core.entity.SmtArticlesReleaseMain;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.core.vo.StaffInfoVO;
import com.tce.smart.platform.emun.ReleaseItemEnum;
import com.tce.smart.platform.emun.ReleaseTypeEnum;
import com.tce.smart.platform.service.SmtArticlesReleaseMainService;
import com.tce.smart.platform.service.SmtProcessRecordService;
import com.tce.smart.platform.service.SmtStaffService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 物品返厂确认列表wrapper
 * @author sunfujian
 * @date 2021/8/12 20:36
 */
@Component
@AllArgsConstructor
public class ArticlesReleaseBackConfirmListWrapper extends BaseWrapper<SmtArticlesRelease, BackFactoryConfirmListDTO> {

	private final SmtProcessRecordService processRecordService;
	private final SmtStaffService smtStaffService;
	private final SmtArticlesReleaseMainService articlesReleaseMainService;

	@Override
	protected BackFactoryConfirmListDTO warp(SmtArticlesRelease smtArticlesRelease) throws IOException {
		BackFactoryConfirmListDTO confirmListDTO = new BackFactoryConfirmListDTO();
		confirmListDTO.setId(smtArticlesRelease.getId());
		confirmListDTO.setProcessId(smtArticlesRelease.getProcessId());
		confirmListDTO.setName(smtArticlesRelease.getName());
		confirmListDTO.setReleaseItemDesc(ReleaseItemEnum.getByCode(smtArticlesRelease.getReleaseItem()));
		confirmListDTO.setCreateTime(smtArticlesRelease.getCreateTime());
		confirmListDTO.setBackStatus(Objects.nonNull(smtArticlesRelease.getBackTime()) ? "已确认" : "未确认");
		if(StringUtils.isEmpty(smtArticlesRelease.getPhone())){
			StaffInfoVO vo = smtStaffService.getBaseinfoById(smtArticlesRelease.getBadge());
			confirmListDTO.setDeptName(vo.getSmtStaff().getDepName());
		}else{
			StaffInfoVO vo = smtStaffService.getSmtStaffInfoByPhone(smtArticlesRelease.getPhone(),smtArticlesRelease.getCarrier());
			confirmListDTO.setDeptName(vo.getSmtStaff() != null ? vo.getSmtStaff().getDepName() : "-");
		}
		if (StrUtil.isNotBlank(smtArticlesRelease.getProcessId())) {
			List<SmtProcessRecord> selectList = processRecordService.list(Wrappers.<SmtProcessRecord> query().lambda().eq(SmtProcessRecord::getProcessId, smtArticlesRelease.getProcessId()).orderByDesc(SmtProcessRecord::getRecordDate));
			if(selectList.size()>0) {
				//查询流程的最新的状态数据
				String nodeName = selectList.get(0).getNodeName();
				if(StrUtil.isEmpty(nodeName)) {
					confirmListDTO.setOaNode("");
				}else {
					String[] nodeNames = nodeName.split(" ");
					if(nodeNames.length == 2) {
						confirmListDTO.setOaNode(nodeNames[1]);
					}
				}
			}
		}
		SmtArticlesReleaseMain releaseMain = articlesReleaseMainService.getByReleaseId(smtArticlesRelease.getId());
		confirmListDTO.setReleaseTypeDesc(releaseMain != null ? ReleaseTypeEnum.getByCode(releaseMain.getWpfxlb()) : null);
		return confirmListDTO;
	}
}
