package com.tce.smart.businesstrip.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.businesstrip.core.entity.CcdFormtableMainDt2;
import com.tce.smart.businesstrip.core.mapper.CcdFormtableMainDt2Mapper;
import com.tce.smart.businesstrip.core.service.CcdFormtableMainDt2Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * 出差报告
 * @author liangyuan
 *
 */
@Service
@Slf4j
public class CcdFormtableMainDt2ServiceImpl extends ServiceImpl<CcdFormtableMainDt2Mapper, CcdFormtableMainDt2>
		implements CcdFormtableMainDt2Service {

	@Autowired
	private CcdFormtableMainDt2Service ccdFormtableMainDt2Service;

}
