package com.tce.smart.ehrview.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.OvwYscomp;
import com.tce.smart.ehrview.core.entity.YutoDhrOrgs;
import com.tce.smart.ehrview.core.mapper.YutoDhrOrgsMapper;
import com.tce.smart.ehrview.core.service.YutoDhrOrgsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class YotoDhrOrgsServiceImpl extends ServiceImpl<YutoDhrOrgsMapper, YutoDhrOrgs> implements YutoDhrOrgsService {

    @Override
    public OvwYscomp getByCompId(String compId) {
        //id为long为外部Bu
        if(compId.length() > 12) {
            return null;
        }
        YutoDhrOrgs yutoDhrOrgs = this.baseMapper.selectOne(Wrappers.<YutoDhrOrgs> query().lambda().eq(YutoDhrOrgs::getPkOrg, Integer.parseInt(compId)));
        return format(yutoDhrOrgs);
    }

    @Override
    public List<OvwYscomp> getList() {
        List<YutoDhrOrgs> list = this.baseMapper.selectList(Wrappers.<YutoDhrOrgs> query().lambda().orderByAsc(YutoDhrOrgs::getPkOrg));
        return list.stream().map(this::format).collect(Collectors.toList());
    }



    private OvwYscomp format(YutoDhrOrgs yutoDhrOrgs) {
        if (Objects.isNull(yutoDhrOrgs)) {
            return null;
        }
        OvwYscomp ovwYscomp = new OvwYscomp();
        ovwYscomp.setCompid(yutoDhrOrgs.getPkOrg());
        ovwYscomp.setTitle(yutoDhrOrgs.getName());
        ovwYscomp.setCompAbbr(yutoDhrOrgs.getName());
//        ovwYscomp.setCompGrade();
        ovwYscomp.setAdminID(yutoDhrOrgs.getPkFatherOrg());
        ovwYscomp.setEzid(yutoDhrOrgs.getPkOrg());
        ovwYscomp.setASzstatus(Integer.valueOf(2).equals(yutoDhrOrgs.getEnableState()) ? "UPD" : "NEW");
        return ovwYscomp;
    }
}
