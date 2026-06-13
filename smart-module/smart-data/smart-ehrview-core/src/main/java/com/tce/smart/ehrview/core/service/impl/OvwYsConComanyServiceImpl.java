package com.tce.smart.ehrview.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.OvwYsConComany;
import com.tce.smart.ehrview.core.mapper.OvwYsConComanyMapper;
import com.tce.smart.ehrview.core.service.IOvwYsConComanyService;
import org.springframework.stereotype.Service;

import java.util.List;

/***
 * description: 合同签约单位服务实现类 <br>
 * date: 2019/11/27 11:53 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Service
public class OvwYsConComanyServiceImpl extends ServiceImpl<OvwYsConComanyMapper, OvwYsConComany> implements IOvwYsConComanyService {

	@Override
	public OvwYsConComany getByCompId(Integer compId) {
		return baseMapper.selectOne(Wrappers.<OvwYsConComany>query().lambda().eq(OvwYsConComany::getCompId, compId));
	}

	@Override
	public List<OvwYsConComany> getByTitle(String title) {
		return baseMapper.selectList(Wrappers.<OvwYsConComany>query().lambda().like(OvwYsConComany::getTitle, title));
	}
}
