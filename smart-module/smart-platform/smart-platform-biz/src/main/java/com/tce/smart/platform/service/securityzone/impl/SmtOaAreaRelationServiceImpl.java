package com.tce.smart.platform.service.securityzone.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.api.dto.req.securityzone.OaAreaRelationEditReqDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.OaAreaRelationRespDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtOaAreaRelation;
import com.tce.smart.platform.core.mapper.SmtOaAreaRelationMapper;
import com.tce.smart.platform.core.service.SmtSecurityAreaService;
import com.tce.smart.platform.service.securityzone.SmtOaAreaRelationService;
import com.tce.smart.tool.constant.SymbolConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fushiping
 * @date 2021-07-29 11:13:44
 */
@Service
public class SmtOaAreaRelationServiceImpl extends ServiceImpl<SmtOaAreaRelationMapper, SmtOaAreaRelation> implements SmtOaAreaRelationService {

    @Autowired
    private SmtSecurityAreaService smtSecurityAreaService;

    @Override
    public String getAuthNameByAreaId(Integer parkId, List<Integer> areaId) {
        List<SmtOaAreaRelation> authList = this.getListByAreaId(parkId, areaId);
        if (CollUtil.isNotEmpty(authList)) {
            List<String> authName = authList.stream().map(SmtOaAreaRelation::getAuthName).collect(Collectors.toList());
            return StringUtils.join(authName, SymbolConstants.BRANCH);
        }
        return null;
    }

    @Override
    public Boolean editRelation(List<OaAreaRelationEditReqDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return Boolean.FALSE;
        }
        //删除
        this.remove(Wrappers.<SmtOaAreaRelation>query().lambda()
                .eq(SmtOaAreaRelation::getParkId, list.get(0).getParkId()));
        //新增
        for (OaAreaRelationEditReqDTO reqDTO : list) {
            if (CollUtil.isEmpty(reqDTO.getAuthIds())) {
                continue;
            }
            List<SmtOaAreaRelation> authList = reqDTO.getAuthIds().stream().map(auth -> {
                SmtOaAreaRelation typeAuth = SmtOaAreaRelation.builder()
                        .authId(auth.getAuthId()).authName(auth.getAuthName()).parkId(reqDTO.getParkId())
                        .authType(auth.getAuthType()).oaAreaId(reqDTO.getOaAreaId()).oaAreaName(reqDTO.getOaAreaName()).build();
                return typeAuth;
            }).collect(Collectors.toList());
            this.saveBatch(authList);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<OaAreaRelationRespDTO> getList(Integer parkId) {
        //获得所有OA区域类型
//		List<Map<String, Object>>  oaList = SecurityOaAreaEnum.getTypeList();
        List<Map<String, Object>> oaList = smtSecurityAreaService.list().stream().map(smtSecurityArea -> {
            Map<String, Object> map = BeanUtil.beanToMap(smtSecurityArea);
            map.remove("id");
            return map;
        }).collect(Collectors.toList());
        List<OaAreaRelationRespDTO> respDTOS = new ArrayList<>();
        for (Map<String, Object> map : oaList) {
            OaAreaRelationRespDTO resp = new OaAreaRelationRespDTO();
            resp.setParkId(parkId);
            resp.setOaAreaId((Integer) map.get("code"));
            resp.setOaAreaName(map.get("desc").toString());
            resp.setFactoryType((Integer) map.get("factoryDesc"));
            //获得区域类型相关联权限
            List<SmtOaAreaRelation> authList = this.list(Wrappers.<SmtOaAreaRelation>query().lambda()
                    .eq(SmtOaAreaRelation::getParkId, parkId).eq(SmtOaAreaRelation::getOaAreaId, resp.getOaAreaId()));
            if (CollUtil.isNotEmpty(authList)) {
                List<OaAreaRelationRespDTO.RelationAuth> authLists = authList.stream().map(auth -> {
                    OaAreaRelationRespDTO.RelationAuth authReq = new OaAreaRelationRespDTO.RelationAuth();
                    authReq.setAuthId(auth.getAuthId());
                    authReq.setAuthName(auth.getAuthName());
                    authReq.setAuthType(auth.getAuthType());
                    return authReq;
                }).collect(Collectors.toList());
                resp.setAuthList(authLists);
            }
            respDTOS.add(resp);
        }
        return respDTOS;
    }

    @Override
    public List<SmtOaAreaRelation> getListByAreaId(Integer parkId, List<Integer> oaAreaId) {
        List<SmtOaAreaRelation> list = this.list(Wrappers.<SmtOaAreaRelation>query().lambda()
                .eq(SmtOaAreaRelation::getParkId, parkId)
                .in(SmtOaAreaRelation::getOaAreaId, oaAreaId));
        if (CollUtil.isNotEmpty(list)) {
            //根据权限id去重
            List<SmtOaAreaRelation> newList = list.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SmtOaAreaRelation::getAuthId))), ArrayList::new));
            return newList;
        }
        return new ArrayList<>();
    }
}
