package com.tce.smart.platform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.api.dto.req.AddDormitoryAdministratorReqDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryAdministrator;
import com.tce.smart.platform.core.mapper.SmtDormitoryAdministratorMapper;
import com.tce.smart.platform.service.SmtDormitoryAdministratorService;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @Title: SmtDormitoryAdministratorServiceImpl
 * @Descripition: 宿舍管理员服务实现类
 * @Auther: guohongtai
 * @Date: 2020-10-14 16:02
 */
@Slf4j
@Service
@AllArgsConstructor
public class SmtDormitoryAdministratorServiceImpl extends ServiceImpl<SmtDormitoryAdministratorMapper, SmtDormitoryAdministrator> implements SmtDormitoryAdministratorService {

	@Override
	public SmtDormitoryAdministrator getByParkId(Integer parkId) {
		return this.getBaseMapper().selectOne(Wrappers.<SmtDormitoryAdministrator> query().lambda().eq(SmtDormitoryAdministrator::getParkId, parkId));
	}

	@Override
	public Boolean saveDormitoryAdministrator(AddDormitoryAdministratorReqDTO reqDTO) {
		if(Objects.isNull(reqDTO)){
			throw new TCEException("宿舍管理员列表为空");
		}

		this.getBaseMapper().delete(Wrappers.<SmtDormitoryAdministrator> query().lambda().eq(SmtDormitoryAdministrator::getParkId, reqDTO.getParkId()));

		SmtDormitoryAdministrator administrator = new SmtDormitoryAdministrator();
		if(StrUtil.isNotEmpty(reqDTO.getBadgeOne()))
			administrator.setBadgeOne(reqDTO.getBadgeOne());
		if(StrUtil.isNotEmpty(reqDTO.getBadgeTwo()))
			administrator.setBadgeTwo(reqDTO.getBadgeTwo());
		if(StrUtil.isNotEmpty(reqDTO.getBadgeThree()))
			administrator.setBadgeThree(reqDTO.getBadgeThree());
		if(StrUtil.isNotEmpty(reqDTO.getBadgeFour()))
			administrator.setBadgeFour(reqDTO.getBadgeFour());
		administrator.setParkId(reqDTO.getParkId());
		return administrator.insert();
	}
}
