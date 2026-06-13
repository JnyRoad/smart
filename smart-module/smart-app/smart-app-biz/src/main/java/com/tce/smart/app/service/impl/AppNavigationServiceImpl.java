package com.tce.smart.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.dto.AppModuleInfoDto;
import com.tce.smart.app.entity.AppModuleInfo;
import com.tce.smart.app.mapper.AppModuleInfoMapper;
import com.tce.smart.app.service.AppNavigationService;
import com.tce.smart.app.service.AppServeService;
import com.tce.smart.app.vo.AppNavigationVo;
import com.tce.smart.app.vo.AppServeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fushiping
 * @date 2019/6/18  16:59
 * APP导航菜单获取
 **/
@Service
public class AppNavigationServiceImpl extends ServiceImpl<AppModuleInfoMapper, AppModuleInfo> implements AppNavigationService {

	@Autowired
	private AppServeService appServeService;
	@Override
	public List<AppNavigationVo> getNavigationMenu() {
		List<AppModuleInfoDto> list = appServeService.getFix().getModule().subList(0, 7);
		List<AppNavigationVo> listVo = new ArrayList<>();
		list.forEach(AppModuleInfoDto-> {
			AppNavigationVo vo = new AppNavigationVo();
			BeanUtils.copyProperties(AppModuleInfoDto, vo);
			listVo.add(vo);
		});
		return listVo;
	}
}
