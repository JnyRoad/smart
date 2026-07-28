package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.data.api.dto.ehrview.req.OvwYsConComanyReqDTO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsConComanyRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYscompRespDTO;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYsConComanyService;
import com.tce.smart.platform.core.ao.RecruitSetSaveAO;
import com.tce.smart.platform.core.entity.SmtRecruitmentSetting;
import com.tce.smart.platform.core.mapper.SmtRecruitmentSettingMapper;
import com.tce.smart.platform.core.vo.RecruitSetCompListVO;
import com.tce.smart.platform.core.vo.RecruitSetListVO;
import com.tce.smart.platform.core.vo.RecruitSetWorkBaseListVO;
import com.tce.smart.platform.service.SmtRecruitmentService;
import com.tce.smart.platform.service.SmtRecruitmentSettingService;
import com.tce.smart.tool.constant.DictConstants;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 招聘设置服务实现类
 *
 * @author mckaywu
 * @date 2019-11-20 10:35:16
 */
@Slf4j
@Service
public class SmtRecruitmentSettingServiceImpl extends ServiceImpl<SmtRecruitmentSettingMapper, SmtRecruitmentSetting> implements SmtRecruitmentSettingService {

	@Autowired
	private RemoteOvwYsConComanyService remoteOvwYsConComanyService;

	@Autowired
	private RemoteDictService remoteDictService;

	@Autowired
	private SmtRecruitmentService smtRecruitmentService;

	@Override
	public RecruitSetListVO listRecruit(Integer parkId, String buId) {
		RecruitSetListVO recruitSetListVO = null;
		List<SmtRecruitmentSetting> recruitmentSettingList = this.list(Wrappers.<SmtRecruitmentSetting>query().lambda().eq(SmtRecruitmentSetting::getParkId, parkId)
				.eq(StringUtils.isNotBlank(buId), SmtRecruitmentSetting::getWorkCompId, buId));
		if (CollectionUtils.isNotEmpty(recruitmentSettingList)) {
			recruitSetListVO = new RecruitSetListVO();
			recruitSetListVO.setParkId(parkId);
			recruitSetListVO.setCompOrgList(new ArrayList<>());

			for (SmtRecruitmentSetting element : recruitmentSettingList) {
				//设置工作地点
				if (!StringUtil.isNullOrEmpty(element.getWorkBaseCode())) {
					String workBaseCode = element.getWorkBaseCode();
					//翻译工作地点
					Result<SysDict> findBWorkBaseDictRs = remoteDictService.findByValue(DictConstants.WORK_BASE_CODE, workBaseCode, SecurityConstants.FROM_IN);
					if(findBWorkBaseDictRs.isSuccess() && Objects.nonNull(findBWorkBaseDictRs.getData())){
						String workBaseName = findBWorkBaseDictRs.getData().getDescription();
						element.setWorkBaseName(workBaseName);
					}
				}

				//翻译BU
				if(!StringUtil.isNullOrEmpty(element.getWorkCompId())) {
					Result<SysDict> findByValueRs = remoteDictService.findByValue(DictConstants.COMP_ABBR, element.getWorkCompId(), SecurityConstants.FROM_IN);
					if (findByValueRs.isSuccess() && Objects.nonNull(findByValueRs.getData())) {
						element.setWorkCompTitle(findByValueRs.getData().getDescription());
					}
				}

				//翻译签约单位
				if(!StringUtil.isNullOrEmpty(element.getWorkOrgId())) {
					Result<OvwYsConComanyRespDTO> getByCompIdRs = remoteOvwYsConComanyService.getByCompId(Integer.parseInt(element.getWorkOrgId()), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
					if (getByCompIdRs.isSuccess() && Objects.nonNull(getByCompIdRs.getData())) {
						element.setWorkOrgName(getByCompIdRs.getData().getTitle());
					}
				}
				recruitSetListVO.getCompOrgList().add(element);
			}
		}
		return recruitSetListVO;
	}

	@Override
	public Boolean removeByParkId(Integer parkId) {
		return this.remove(Wrappers.<SmtRecruitmentSetting>query().lambda().eq(SmtRecruitmentSetting::getParkId, parkId));
//		baseMapper.delete(Wrappers.<SmtRecruitmentSetting>update().lambda().eq(SmtRecruitmentSetting::getParkId,parkId));
	}

	@Override
	public List<RecruitSetCompListVO> getListByTitle(String keyword) {
		List<RecruitSetCompListVO> compList = null;
		OvwYsConComanyReqDTO queryOvwYsConComany = new OvwYsConComanyReqDTO();
		queryOvwYsConComany.setTitle(keyword);
		Result<List<OvwYsConComanyRespDTO>> getListRs = remoteOvwYsConComanyService.getByTitle(queryOvwYsConComany, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (getListRs.isSuccess() && CollectionUtils.isNotEmpty(getListRs.getData())) {
			compList = new ArrayList<>();
			for (OvwYsConComanyRespDTO element : getListRs.getData()) {
				compList.add(new RecruitSetCompListVO(element.getCompId() + "", element.getTitle()));
			}
		}
		return compList;
	}

	@Override
	public List<RecruitSetWorkBaseListVO> getWorkBaseCodeList(String keyword) {
		List<RecruitSetWorkBaseListVO> workBaseLis = null;
		Result<List<SysDict>> getDictListRs = remoteDictService.findByType(DictConstants.WORK_BASE_CODE, SecurityConstants.FROM_IN);
		if (getDictListRs.isSuccess() && CollectionUtils.isNotEmpty(getDictListRs.getData())) {
			boolean isKeyWorkSearch = !StringUtil.isNullOrEmpty(keyword);
			workBaseLis = new ArrayList<>();
			for (SysDict element : getDictListRs.getData()) {
				//过滤
				if (isKeyWorkSearch && !element.getDescription().contains(keyword)) {
					continue;
				}

				workBaseLis.add(new RecruitSetWorkBaseListVO(element.getValue(), element.getDescription()));
			}
		}
		return workBaseLis;
	}

	@Override
	public List<OvwYscompRespDTO> getCompeList(String keyword) {
		List<OvwYscompRespDTO> ovwYscompList = null;
		Result<List<OvwYscompRespDTO>> getYscompList = smtRecruitmentService.getComp();
		if (Objects.nonNull(getYscompList) && CollectionUtils.isNotEmpty(getYscompList.getData())) {
			boolean isKeyWorkSearch = !StringUtil.isNullOrEmpty(keyword);
			ovwYscompList = new ArrayList<>();
			for (OvwYscompRespDTO element : getYscompList.getData()) {
				//过滤
				if (isKeyWorkSearch && !element.getTitle().contains(keyword)) {
					continue;
				}

				ovwYscompList.add(element);
			}
		}
		return ovwYscompList;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean batchSaveRecruit(RecruitSetSaveAO recruitSetSaveAO) {
		if (Objects.nonNull(recruitSetSaveAO) && CollectionUtils.isNotEmpty(recruitSetSaveAO.getCompOrgList())) {
			this.removeByParkId(recruitSetSaveAO.getParkId());
			recruitSetSaveAO.getCompOrgList().forEach(element -> {
				element.setParkId(recruitSetSaveAO.getParkId());
				element.setCreateTime(LocalDateTime.now());
				this.save(element);
			});

			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}
}
