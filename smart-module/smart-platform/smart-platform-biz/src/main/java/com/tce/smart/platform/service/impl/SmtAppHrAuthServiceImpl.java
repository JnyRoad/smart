package com.tce.smart.platform.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtAppHrAuth;
import com.tce.smart.platform.core.mapper.SmtAppHrAuthMapper;
import com.tce.smart.platform.service.SmtAppHrAuthService;
import com.tce.smart.tool.enums.DeleteStatusEnum;

/**
 * app招聘数据权限服务实现类
 *
 * @author mckaywu
 * @date 2019-06-12 11:24:03
 */
@Service
public class SmtAppHrAuthServiceImpl extends ServiceImpl<SmtAppHrAuthMapper, SmtAppHrAuth>
		implements SmtAppHrAuthService {

	@Override
	public List<SmtAppHrAuth> getHrAuthList() {
		QueryWrapper<SmtAppHrAuth>  queryWrapper = new QueryWrapper<SmtAppHrAuth> ();
		queryWrapper
			.lambda()
			.eq(SmtAppHrAuth::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode());

		return this.baseMapper.selectList(queryWrapper);
	}

}
