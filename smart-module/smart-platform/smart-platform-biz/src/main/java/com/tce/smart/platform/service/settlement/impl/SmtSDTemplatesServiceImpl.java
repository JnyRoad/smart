package com.tce.smart.platform.service.settlement.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsjobRespDTO;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYsjobService;
import com.tce.smart.platform.core.entity.SmtSdTemplates;
import com.tce.smart.platform.core.entity.SmtTemplatesRule;
import com.tce.smart.platform.core.mapper.SmtSDTemplatesMapper;
import com.tce.smart.platform.service.settlement.SmtSDTemplatesService;
import com.tce.smart.platform.service.settlement.SmtTemplatesRuleService;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryPersonService;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @description: SmtSDTemplatesServiceImpl
 * @date: 2020-07-07 11:34
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class SmtSDTemplatesServiceImpl extends ServiceImpl<SmtSDTemplatesMapper, SmtSdTemplates> implements SmtSDTemplatesService {

	private final SmtSDTemplatesMapper smtSDTemplatesMapper;

	private final SmtTemplatesRuleService smtTemplatesRuleService;

	private final RemoteOvwYsjobService remoteOvwYsjobService;

	private final SmtDormitoryPersonService smtDormitoryPersonService;

	@Override
	public IPage getSmtDormitorySDTemplatePage(Page page, SmtSdTemplates smtSdTemplates) {
		//当前登录用户所属的园区ID列表
		List<Integer> parkIdList = smtDormitoryPersonService.getParkId(SecurityUtils.getUser().getUsername());
		if(CollUtil.isEmpty(parkIdList)) {
			parkIdList = SecurityUtils.getUser().getParkIdList();
		}
		return  smtSDTemplatesMapper.getSmtDormitorySDTemplatePage(page,smtSdTemplates,parkIdList);
	}

	@Override
	public List<SmtSdTemplates> getSDTempByParkId(Integer parkid) {
		QueryWrapper<SmtSdTemplates> queryWrapper = new QueryWrapper<SmtSdTemplates>();
		queryWrapper.eq("PARK_ID",parkid);
		return this.list(queryWrapper);
	}

	@Transactional
    @Override
    public boolean deleteSDTemplateData(Long tempId) {
		//判断指定的模板是否存在
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		SmtSdTemplates smtSdTemplates = smtSDTemplatesMapper.selectById(tempId);
		if(smtSdTemplates == null || !parkIdList.contains(smtSdTemplates.getParkId())){
			throw new TCEException("水电模板不存在");
		}
		//删除模板数据
		this.removeById(tempId);
		//删除收费规则数据
		smtTemplatesRuleService.remove(new QueryWrapper<SmtTemplatesRule>().lambda().eq(SmtTemplatesRule::getTempId,tempId));
        return true;
    }

	@Override
	public List<OvwYsjobRespDTO> getJChenList() {
		Result<List<OvwYsjobRespDTO>> jChenList = remoteOvwYsjobService.getJChenList(SecurityConstants.FROM_IN);
		if(!jChenList.isSuccess()){
			log.error("远程获取职位级层异常");
			throw new TCEException("获取级层异常");
		}
		return jChenList.getData();
	}
}
