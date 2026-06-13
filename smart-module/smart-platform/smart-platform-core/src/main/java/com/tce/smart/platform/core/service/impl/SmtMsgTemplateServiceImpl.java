package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.dto.MsgTemplateDTO;
import com.tce.smart.platform.core.entity.SmtEmailReceive;
import com.tce.smart.platform.core.entity.SmtMsgTemplate;
import com.tce.smart.platform.core.entity.ext.MsgTemplateExt;
import com.tce.smart.platform.core.mapper.SmtMsgTemplateMapper;
import com.tce.smart.platform.core.mapper.SmtParkMapper;
import com.tce.smart.platform.core.service.SmtEmailReceiveService;
import com.tce.smart.platform.core.service.SmtMsgTemplateService;
import com.tce.smart.tool.enums.MsgTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息模板服务实现类
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:49:29
 */
@Service
public class SmtMsgTemplateServiceImpl extends ServiceImpl<SmtMsgTemplateMapper, SmtMsgTemplate> implements SmtMsgTemplateService {

	@Autowired
	private SmtEmailReceiveService reService;

	@Autowired
	private SmtParkMapper smtParkMapper;

	/**
	 * 查询所有的短信模板
	 */
	@Override
	public Result getSmtMessageTemplate() {
		// TODO Auto-generated method stub
		List<SmtMsgTemplate> smtMessageTemplate = this.list(Wrappers.<SmtMsgTemplate>query().lambda().eq(SmtMsgTemplate::getMsgType, MsgTypeEnum.MSG_1.getCode()));
		return new Result<>(smtMessageTemplate);
	}

	/**
	 * 查询所有的邮箱模板
	 */
	@Override
	public Result getSmtEmainTemplateService() {
		// TODO Auto-generated method stub
		List<SmtMsgTemplate> smtMessageTemplate = this.list(Wrappers.<SmtMsgTemplate>query().lambda().eq(SmtMsgTemplate::getMsgType, MsgTypeEnum.MSG_2.getCode()));
		return new Result<>(smtMessageTemplate);
	}

	@Override
	public Result getByCode(String code) {
		// TODO Auto-generated method stub
		SmtMsgTemplate smtMessageTemplate = this.baseMapper.selectOne(Wrappers.<SmtMsgTemplate>query().lambda().eq(SmtMsgTemplate::getTempCode, code));

		if (smtMessageTemplate == null) {
			throw new TCEException("模板编号不存在");
		}
		MsgTemplateExt msgTemplateExt = new MsgTemplateExt();
		List<SmtEmailReceive> selectList = reService.list(Wrappers.<SmtEmailReceive>query().lambda()
				.eq(SmtEmailReceive::getTemplateId, smtMessageTemplate.getId()));
		for(SmtEmailReceive receive: selectList){
			receive.setParkName(smtParkMapper.selectById(receive.getParkId()).getParkName());
		}
		msgTemplateExt.setReceiveList(selectList);
		msgTemplateExt.setMsgTemplate(this.getById(smtMessageTemplate.getId()));
		return new Result<>(msgTemplateExt);
	}

	@Override
	public Result update(MsgTemplateDTO msgTemplateDTO) {
		// TODO Auto-generated method stub
		SmtMsgTemplate msgTemplate = msgTemplateDTO.getMsgTemplate();
		if (ObjectUtil.isNull(msgTemplate.getId())) {
			return new Result<>(this.save(msgTemplate));
		}
		return new Result<>(msgTemplate.updateById());
	}

	@Override
	public Result updateReceive(MsgTemplateDTO msgTemplateDTO) {
		// TODO Auto-generated method stub
		SmtMsgTemplate msgTemplate = msgTemplateDTO.getMsgTemplate();
		List<SmtEmailReceive> receiveList = msgTemplateDTO.getReceiveList();
		//先删除再添加
		reService.remove(Wrappers.<SmtEmailReceive> query().lambda().eq(SmtEmailReceive::getTemplateId, msgTemplate.getId()));
		if(ObjectUtil.isNotNull(receiveList)&& receiveList.size()>0)
		{
			for (SmtEmailReceive smtEmailReceive : receiveList) {
				smtEmailReceive.setParkId(smtEmailReceive.getParkId());
				smtEmailReceive.setTemplateId(String.valueOf(msgTemplate.getId()));
				smtEmailReceive.insert();
			}
		}
		return new Result<>(msgTemplate.updateById());
	}

	@Override
	public Result getEmailById(Integer id) {
		// TODO Auto-generated method stub
		if (ObjectUtil.isNull(id)) {
			throw new TCEException("唯一标识不能为空");
		}
		MsgTemplateExt msgTemplateExt = new MsgTemplateExt();

		List<SmtEmailReceive> selectList = reService.list(Wrappers.<SmtEmailReceive>query().lambda().eq(SmtEmailReceive::getTemplateId, id));
		msgTemplateExt.setReceiveList(selectList);
		msgTemplateExt.setMsgTemplate(this.getById(id));
		return new Result<>(msgTemplateExt);
	}

	@Override
	public SmtMsgTemplate selectByTempCode(String tempCode) {
		QueryWrapper<SmtMsgTemplate> queryWrapper = new QueryWrapper<SmtMsgTemplate>();
		queryWrapper.lambda().eq(SmtMsgTemplate::getTempCode, tempCode);
		return this.getOne(queryWrapper);
	}

	/**
	 * 查询所有的短信模板
	 */
	@Override
	public List<SmtMsgTemplate> getMsgTemplate() {
		List<SmtMsgTemplate> smtMessageTemplate = this.list(Wrappers.<SmtMsgTemplate>query().lambda().eq(SmtMsgTemplate::getMsgType, MsgTypeEnum.MSG_1.getCode()));
		return smtMessageTemplate;
	}

}