package com.tce.smart.ehrview.core.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.OvwYsdep;
import com.tce.smart.ehrview.core.entity.YutoDhrDept;
import com.tce.smart.ehrview.core.mapper.YutoDhrDeptMapper;
import com.tce.smart.ehrview.core.service.YutoDhrDeptService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class YotoDhrDeptServiceImpl extends ServiceImpl<YutoDhrDeptMapper, YutoDhrDept> implements YutoDhrDeptService {


    @Override
    public List<OvwYsdep> getByCompId(Integer compId) {
        return this.baseMapper.selectList(Wrappers.<YutoDhrDept>query().lambda()
                        .eq(YutoDhrDept::getPkOrg, compId))
                .stream().map(this::format)
                .collect(Collectors.toList());
    }

    @Override
    public OvwYsdep getByDepId(Integer depId) {
        YutoDhrDept ovwYsdep = this.baseMapper.selectOne(Wrappers.<YutoDhrDept>query().lambda().eq(YutoDhrDept::getPkDept, depId));
        return format(ovwYsdep);
    }

    @Override
    public List<OvwYsdep> getParentDep(Integer depId) {
        OvwYsdep selectOne = getByDepId(depId);
        List<OvwYsdep> list = new ArrayList<>();
        list.add(selectOne);
        Integer grade = Integer.parseInt(ObjectUtil.isNotNull(selectOne) ? selectOne.getDepGrade() : "0");
        for (int i = grade; i > 0; i--) {
            if (selectOne.getAdminID() != null) {
                selectOne = getByDepId(selectOne.getAdminID());
                list.add(selectOne);
            }
        }
        return list;
    }

    private OvwYsdep format(YutoDhrDept yutoDhrDept) {
        if (Objects.isNull(yutoDhrDept)) {
            return null;
        }
        OvwYsdep ovwYsdep = new OvwYsdep();
        ovwYsdep.setDepid(yutoDhrDept.getPkDept());
        ovwYsdep.setDepname(yutoDhrDept.getName());
        ovwYsdep.setDepAbbr(yutoDhrDept.getName());
        ovwYsdep.setCompID(yutoDhrDept.getPkOrg());
        ovwYsdep.setDirector(yutoDhrDept.getDirector());
        ovwYsdep.setDirecName(yutoDhrDept.getDirectorName());
        ovwYsdep.setDepGrade(DeptGrade.code(yutoDhrDept.getDeptLevel()));
        ovwYsdep.setAdminID(yutoDhrDept.getPkFatherOrg());
        ovwYsdep.setDepCost(yutoDhrDept.getGlbdef3());
        ovwYsdep.setASzstatus(Integer.valueOf(2).equals(yutoDhrDept.getEnableState()) ? "UPD" : "NEW");
        return ovwYsdep;
    }

    @Getter
    @AllArgsConstructor
    enum DeptGrade {
        /**
         * 部门等级
         */
        CENTER("1", "中心"),
        DEPT("2", "部门"),
        CLASSROOM("3", "课室"),
        GROUP("4", "组");

        private final String code;

        private final String name;

        public static String code(String name) {
            return Arrays.stream(values()).filter(e -> StrUtil.isNotBlank(name) && name.equals(e.getName())).map(DeptGrade::getCode).findFirst().orElse(null);
        }
    }
}
