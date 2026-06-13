package com.tce.smart.businesstrip.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.businesstrip.core.entity.CcdFormtableMain;
import com.tce.smart.businesstrip.core.mapper.CcdFormtableMainMapper;
import com.tce.smart.businesstrip.core.service.CcdFormtableMainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 出差数据服务实现类
 *
 * @author mkwu
 * @date 2019-06-25
 */
@Service
@Slf4j
public class CcdFormtableMainServiceImpl extends ServiceImpl<CcdFormtableMainMapper, CcdFormtableMain>
		implements CcdFormtableMainService {

	@Autowired
	private CcdFormtableMainService ccdFormtableMainService;

	/*	@Override
	public List<CcdFormtableMain> selectList() {
		// 流程编号
		QueryWrapper<CcdFormtableMain> queryWrapper = new QueryWrapper<CcdFormtableMain>();
		queryWrapper.lambda().eq(CcdFormtableMain::getPedestrianBadge, "811938");

		List<CcdFormtableMain> list = ccdFormtableMainService.getBaseMapper().selectList(queryWrapper);

		return list;
	}*/

}
