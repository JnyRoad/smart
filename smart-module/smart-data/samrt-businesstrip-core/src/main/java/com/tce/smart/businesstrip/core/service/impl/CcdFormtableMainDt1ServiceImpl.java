package com.tce.smart.businesstrip.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.businesstrip.core.entity.CcdFormtableMainDt1;
import com.tce.smart.businesstrip.core.mapper.CcdFormtableMainDt1Mapper;
import com.tce.smart.businesstrip.core.service.CcdFormtableMainDt1Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 出差日程数据
 * @author liangyuan
 *
 */
@Service
@Slf4j
public class CcdFormtableMainDt1ServiceImpl extends ServiceImpl<CcdFormtableMainDt1Mapper, CcdFormtableMainDt1>
		implements CcdFormtableMainDt1Service {

	@Autowired
	private CcdFormtableMainDt1Service ccdFormtableMainDt1Service;

}
