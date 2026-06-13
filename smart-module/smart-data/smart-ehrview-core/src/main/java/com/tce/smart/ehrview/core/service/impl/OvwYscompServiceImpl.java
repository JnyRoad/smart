package com.tce.smart.ehrview.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.OvwYscomp;
import com.tce.smart.ehrview.core.mapper.OvwYscompMapper;
import com.tce.smart.ehrview.core.service.IOvwYscompService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Service
public class OvwYscompServiceImpl extends ServiceImpl<OvwYscompMapper, OvwYscomp> implements IOvwYscompService {

    @Override
    public OvwYscomp getByCompId(String compId) {
	//id为long为外部Bu
	if(compId.length() > 12) {
		return null;
		}
        return this.baseMapper.selectOne(Wrappers.<OvwYscomp> query().lambda().eq(OvwYscomp::getCompid, Integer.parseInt(compId)));
    }

    @Override
    public List<OvwYscomp> getList() {
        return this.baseMapper.selectList(Wrappers.<OvwYscomp> query().lambda().orderByAsc(OvwYscomp::getCompid));
    }
}
