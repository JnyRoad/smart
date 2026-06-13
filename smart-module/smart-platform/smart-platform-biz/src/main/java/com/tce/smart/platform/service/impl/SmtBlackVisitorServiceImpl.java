package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwEmphrYsService;
import com.tce.smart.platform.api.dto.req.visitormanage.BlackVisitorAddReqDTO;
import com.tce.smart.platform.core.entity.SmtBlackVisitor;
import com.tce.smart.platform.core.mapper.SmtBlackVisitorMapper;
import com.tce.smart.platform.core.vo.BlackVisitorVO;
import com.tce.smart.platform.service.SmtBlackVisitorService;
import com.tce.smart.tool.exception.TCEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class SmtBlackVisitorServiceImpl extends ServiceImpl<SmtBlackVisitorMapper, SmtBlackVisitor> implements SmtBlackVisitorService  {

	@Autowired
	private RemoteEvwEmphrYsService  remoteEvwEmphrYsService;

	@Override
	public  Result<Boolean> removeVisitorById(Integer id) {
		return new Result<>(this.removeById(id));
	}


	@Override
	public Result<Boolean> addVisitor(BlackVisitorAddReqDTO smtBlackVisitor) {
		String cardNo = smtBlackVisitor.getCardNo();
		if(StrUtil.isBlank(cardNo)){
			throw new TCEException("身份证为空不能加入黑名单");
		}
		SmtBlackVisitor selectOne = this.baseMapper.selectOne(Wrappers.<SmtBlackVisitor> query().lambda().eq(SmtBlackVisitor::getCardNo,cardNo));
		if(ObjectUtil.isNotNull(selectOne)) {
			throw new TCEException("此身份证号已经加入黑名单");
		}
		selectOne = new SmtBlackVisitor();
		selectOne.setCardNo(smtBlackVisitor.getCardNo());
		selectOne.setParkId(smtBlackVisitor.getParkId());
		selectOne.setPersonName(smtBlackVisitor.getPersonName());
		selectOne.setReason(smtBlackVisitor.getReason());
		selectOne.setCreateTime(LocalDateTime.now());
		selectOne.setCreateUser(SecurityUtils.getUser().getUsername());
		return new Result<>(this.save(selectOne));
	}


	@Override
	public IPage<BlackVisitorVO> page(Page page, SmtBlackVisitor smtBlackVisitor) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		return this.baseMapper.page(page, smtBlackVisitor,parkIdList);
	}

	@Override
	public Result getHrBlackPage(Page page, String cerNo, String name) {
		return remoteEvwEmphrYsService.getBlack(page.getCurrent(),page.getSize(), cerNo, name,SecurityConstants.FROM_IN);
	}

	@Override
	public List<BlackVisitorAddReqDTO> batchImport(List<BlackVisitorAddReqDTO> reqDTO, Integer parkId) {
		if (CollUtil.isEmpty(reqDTO)) {
			throw new SmartException("导入数据为空");
		}
		String username = SecurityUtils.getUser().getUsername();
		List<BlackVisitorAddReqDTO> failList = new ArrayList<>();
		for (BlackVisitorAddReqDTO item : reqDTO) {
			try {
				SmtBlackVisitor selectOne = getOne(Wrappers.<SmtBlackVisitor> lambdaQuery().eq(SmtBlackVisitor::getCardNo, item.getCardNo()));
				if(Objects.nonNull(selectOne)) {
					item.setFailReason("此身份证号已经加入黑名单");
					failList.add(item);
					continue;
				}
				SmtBlackVisitor blackVisitor = new SmtBlackVisitor();
				blackVisitor.setCardNo(item.getCardNo());
				blackVisitor.setPersonName(item.getPersonName());
				blackVisitor.setReason(item.getReason());
				blackVisitor.setParkId(parkId);
				blackVisitor.setCreateTime(LocalDateTime.now());
				blackVisitor.setCreateUser(username);
				save(blackVisitor);
			} catch (Exception ex) {
				item.setFailReason("保存数据失败");
				failList.add(item);
			}
		}
		return failList;
	}
}
