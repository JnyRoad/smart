package com.tce.smart.platform.service.admittance.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.oa.resp.WorkflowSelectitemRespDTO;
import com.tce.smart.data.api.feign.businesstrip.RemoteWorkflowSelectitemService;
import com.tce.smart.platform.core.entity.admittance.SmtOaAreaType;
import com.tce.smart.platform.core.mapper.SmtOaAreaTypeMapper;
import com.tce.smart.platform.service.admittance.SmtOaAreaTypeService;
import com.tce.smart.tool.enums.OaSelectItemTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 *
 *
 * @author fushiping
 * @date 2021-08-17 17:45:30
 */
@Service
public class SmtOaAreaTypeServiceImpl extends ServiceImpl<SmtOaAreaTypeMapper, SmtOaAreaType> implements SmtOaAreaTypeService {

	@Autowired
	private RemoteWorkflowSelectitemService remoteWorkflowSelectitemService;
	/**
	 * 查询OA区域时，SELECT_VALUE默认值
	 */
	private final Integer[] securitySelectIds = new Integer[]{0, 5, 8, 11, 18, 14, 15, 20, 17};
	/**
	 * 查询OA区域类型时，SELECT_VALUE默认值
	 */
	private final Integer[] admittanceSelectIds = new Integer[]{7,11,5,10,13,15};
	/**
	 * 查询OA工厂区域类型时，SELECT_VALUE默认值
	 */
	private final Integer[] admittanceFactoryIds = new Integer[]{15,16,17};
	/**
	 * 查询OA区域类型时，FIELD_ID默认值
	 */
	private final Integer admittanceFieldId = 19628;
	/**
	 * 查询OA区域时，FIELD_ID默认值
	 */
	private final Integer securityFieldId = 10254;

	/**
	 * 编辑本地同步oa区域
	 *
	 * @param oaEditList
	 * @param oaRemoveList
	 * @return
	 */
	private Boolean editOaRelation(List<WorkflowSelectitemRespDTO> oaEditList, List<String> oaRemoveList, Integer type) {
		if (CollUtil.isNotEmpty(oaEditList)) {
			oaEditList.forEach(oaArea -> {
				SmtOaAreaType relation = this.getByCode(oaArea.getID());
				if (Objects.nonNull(relation)) {
					//编辑
					relation.setTypeValue(oaArea.getSELECTVALUE());
					relation.setTypeName(oaArea.getSELECTNAME());
					this.updateById(relation);
				} else {
					//新增
					SmtOaAreaType relationAdd = SmtOaAreaType.builder().type(type).typeValue(oaArea.getSELECTVALUE())
							.typeId(oaArea.getID()).typeName(oaArea.getSELECTNAME()).build();
					this.save(relationAdd);
				}
			});
		}
		if (CollUtil.isNotEmpty(oaRemoveList)) {
			//删除
			this.remove(Wrappers.<SmtOaAreaType>query().lambda().in(SmtOaAreaType::getTypeId, oaRemoveList));
		}
		return Boolean.TRUE;
	}

	/**
	 * 构建md5String
	 *
	 * @param id
	 * @param name
	 * @return
	 */
	private String buildMd5(String id, String name) {
        String builder = id +
                name;
		return SecureUtil.md5(builder);
	}

	@Override
	public SmtOaAreaType getByCode(String code) {
		return this.getOne(Wrappers.<SmtOaAreaType>query().lambda().eq(SmtOaAreaType::getTypeId, code));
	}

	@Override
	public SmtOaAreaType getByValue(String typeValue, Integer type) {
		return this.getOne(Wrappers.<SmtOaAreaType>query().lambda().eq(SmtOaAreaType::getTypeValue, typeValue).eq(SmtOaAreaType::getType, type));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean syncOaTask() {
		//this.syncArea(OaSelectItemTypeEnum.ADMITTANCE_AREA_TYPE.getCode());
		this.syncArea(OaSelectItemTypeEnum.SECURITY_AREA.getCode());
		this.syncArea(OaSelectItemTypeEnum.ADMITTANCE_FACTORY_TYPE.getCode());
		return Boolean.TRUE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean syncArea(Integer type) {
		Result<List<WorkflowSelectitemRespDTO>> oaAreaResult = new Result<>();
		OaSelectItemTypeEnum typeEnum = OaSelectItemTypeEnum.getEnum(type);
		List<Integer> selectIdList;
		switch (typeEnum) {
			case ADMITTANCE_AREA_TYPE:
				//获得入厂申请OA区域
				selectIdList = new ArrayList<>(Arrays.asList(admittanceSelectIds));
				oaAreaResult = remoteWorkflowSelectitemService.getList(selectIdList, admittanceFieldId, SecurityConstants.FROM_IN);
				break;
			case SECURITY_AREA:
				//获得保密区区域
				selectIdList = new ArrayList<>(Arrays.asList(securitySelectIds));
				oaAreaResult = remoteWorkflowSelectitemService.getList(selectIdList, securityFieldId, SecurityConstants.FROM_IN);
				break;
			case ADMITTANCE_FACTORY_TYPE:
				//获得入厂工厂区域类型
				selectIdList = new ArrayList<>(Arrays.asList(admittanceFactoryIds));
				oaAreaResult = remoteWorkflowSelectitemService.getList(selectIdList, admittanceFieldId, SecurityConstants.FROM_IN);
				break;
		}
		List<WorkflowSelectitemRespDTO> oaAreaList = oaAreaResult.getData();
		if (CollUtil.isEmpty(oaAreaList)) {
			return Boolean.FALSE;
		}
		Map<String, String> md5OaMap = new HashMap<>(oaAreaList.size());
		oaAreaList.forEach(oa -> {
			md5OaMap.put(oa.getID(), this.buildMd5(oa.getID(), oa.getSELECTNAME()));
		});
		//查询本地同步OA区域
		List<SmtOaAreaType> oaAreaRelations = this.getAreaType(type);
		Map<String, String> md5RelationMap = new HashMap<>(oaAreaRelations.size());
		oaAreaRelations.forEach(relation -> {
			md5RelationMap.put(relation.getTypeId(), this.buildMd5(relation.getTypeId(), relation.getTypeName()));
		});
		//对比差异
		//oa新增或编辑列表
		List<WorkflowSelectitemRespDTO> oaEditList = new ArrayList<>();
		for (Map.Entry<String, String> oaEntry : md5OaMap.entrySet()) {
			if (!md5RelationMap.containsKey(oaEntry.getKey())
					|| !md5RelationMap.get(oaEntry.getKey()).equalsIgnoreCase(oaEntry.getValue())) {
				WorkflowSelectitemRespDTO selectItem = oaAreaList.stream().filter(item -> oaEntry.getKey().equals(item.getID())).findFirst().orElse(null);
				oaEditList.add(selectItem);
			}
		}
		//oa移除列表
		List<String> oaRemoveList = new ArrayList<>();
		for (Map.Entry<String, String> relationEntry : md5RelationMap.entrySet()) {
			if (!md5OaMap.containsKey(relationEntry.getKey())) {
				oaRemoveList.add(relationEntry.getKey());
			}
		}
		return this.editOaRelation(oaEditList, oaRemoveList, type);
	}

	@Override
	public List<SmtOaAreaType> getAreaType(Integer type) {
		return this.list(Wrappers.<SmtOaAreaType>query().lambda().eq(SmtOaAreaType::getType, type));
	}
}
