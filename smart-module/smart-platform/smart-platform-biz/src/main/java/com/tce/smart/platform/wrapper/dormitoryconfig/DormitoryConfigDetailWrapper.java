package com.tce.smart.platform.wrapper.dormitoryconfig;


import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.dormitoryconfig.DormitoryConfigRespDTO;
import com.tce.smart.platform.api.dto.resp.dormitoryconfig.DormitoryPersonRespDTO;
import com.tce.smart.platform.core.entity.dormitoryconfig.SmtDormitoryConfig;
import com.tce.smart.platform.core.entity.dormitoryconfig.SmtDormitoryPerson;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryPersonService;
import com.tce.smart.tool.util.ToolUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description:
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author
 * @Date
 */
@Component
@AllArgsConstructor
public class DormitoryConfigDetailWrapper extends BaseWrapper<SmtDormitoryConfig, DormitoryConfigRespDTO> {

	@Autowired
	private SmtDormitoryPersonService smtDormitoryPersonService;

	@Override
	protected DormitoryConfigRespDTO warp(SmtDormitoryConfig bean) throws IOException {
		DormitoryConfigRespDTO resp = BeanUtils.transform(DormitoryConfigRespDTO.class, bean);
		if (StringUtils.isNotEmpty(bean.getRelationBus())) {
			resp.setRelationBus(ToolUtils.splitStr(bean.getRelationBus()));
		}
		List<SmtDormitoryPerson> personList = smtDormitoryPersonService.getByConfigId(bean.getId());
		if(Objects.isNull(personList)) {
			return resp;
		}
		List<DormitoryPersonRespDTO> personResp = personList.stream().map(p -> {
			DormitoryPersonRespDTO person = BeanUtils.transform(DormitoryPersonRespDTO.class, p);
			person.setDormitoryIds(ToolUtils.splitInt(p.getDormitoryIds()));
			return person;
		}).collect(Collectors.toList());
		resp.setPersonList(personResp);
		return resp;
	}
}
