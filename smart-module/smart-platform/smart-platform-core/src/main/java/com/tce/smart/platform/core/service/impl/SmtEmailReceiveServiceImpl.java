package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtEmailReceive;
import com.tce.smart.platform.core.entity.SmtMsgTemplate;
import com.tce.smart.platform.core.mapper.SmtEmailReceiveMapper;
import com.tce.smart.platform.core.mapper.SmtMsgTemplateMapper;
import com.tce.smart.platform.core.service.SmtEmailReceiveService;
import com.tce.smart.tool.util.RegexUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SmtEmailReceiveServiceImpl extends ServiceImpl<SmtEmailReceiveMapper, SmtEmailReceive> implements SmtEmailReceiveService {

	private final SmtMsgTemplateMapper msgMapper;

	@Override
	public Result updateId(SmtEmailReceive email) {
		// TODO Auto-generated method stub
		if (!RegexUtils.matchEmail(email.getEmail())) {
			return new Result<>(Boolean.FALSE, "邮箱格式不正确");
		}
		if (!RegexUtils.matchPhone(email.getPhone())) {
			return new Result<>(Boolean.FALSE, "手机格式不正确");
		}
		SmtEmailReceive selectById = this.getById(email.getId());
		selectById.setName(email.getName());
		selectById.setEmail(email.getEmail());
		selectById.setPhone(email.getPhone());
		return new Result<>(this.updateById(selectById));
	}

	@Override
	public Result add(SmtEmailReceive email) {
		// TODO Auto-generated method stub
		if (!RegexUtils.matchEmail(email.getEmail())) {
			return new Result<>(Boolean.FALSE, "邮箱格式不正确");
		}
		if (!RegexUtils.matchPhone(email.getPhone())) {
			return new Result<>(Boolean.FALSE, "手机格式不正确");
		}

		SmtEmailReceive list = this.getOne(Wrappers.<SmtEmailReceive>query().lambda().eq(SmtEmailReceive::getEmail, email.getEmail()));
		if (ObjectUtil.isNull(list)) {
			return new Result<>(Boolean.FALSE, "该邮箱已经添加");
		}

		return new Result<>(email.insert());
	}

	@Override
	public Result getEmailById(Integer templateId) {
		// TODO Auto-generated method stub

		List<SmtEmailReceive> list = this.list(Wrappers.<SmtEmailReceive>query().lambda().eq(SmtEmailReceive::getTemplateId, templateId));

		return new Result<>(list);
	}

	@Override
	public Result<List<SmtEmailReceive>> getByCode(String templateCode, Integer parkId) {
		// TODO Auto-generated method stub
		SmtMsgTemplate one = msgMapper.selectOne(Wrappers.<SmtMsgTemplate>query().lambda()
				.eq(SmtMsgTemplate::getTempCode, templateCode));
		List<SmtEmailReceive> list = this.list(Wrappers.<SmtEmailReceive>query().lambda()
				.eq(SmtEmailReceive::getTemplateId, one.getId())
				.eq(SmtEmailReceive::getParkId, parkId));

		return new Result<>(list);
	}

}
