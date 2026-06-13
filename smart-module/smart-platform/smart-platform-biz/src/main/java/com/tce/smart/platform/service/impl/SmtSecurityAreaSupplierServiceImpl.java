package com.tce.smart.platform.service.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.enums.ExceptionEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.SmtSecurityAreaSupplierReqDTO;
import com.tce.smart.platform.api.dto.req.securityarea.*;
import com.tce.smart.platform.api.dto.resp.SmtSecurityAreaSupplierPersonRespDTO;
import com.tce.smart.platform.api.dto.resp.SmtSecurityAreaSupplierRespDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeLossExcelRespDTO;
import com.tce.smart.platform.api.dto.resp.securityarea.SecurityAreaSupplierDTO;
import com.tce.smart.platform.api.dto.resp.securityarea.SecurityAreaSupplierDetailDTO;
import com.tce.smart.platform.api.dto.resp.securityarea.SecurityAreaSupplierExcelRespDTO;
import com.tce.smart.platform.api.dto.resp.securityarea.SecurityAreaSupplierListDTO;
import com.tce.smart.platform.core.dto.SmtSecurityAreaSupplierDTO;
import com.tce.smart.platform.core.dto.securityarea.SecuritySupplierDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtSecurityAreaSupplier;
import com.tce.smart.platform.core.entity.SmtSupplierPerson;
import com.tce.smart.platform.core.entity.badge.SmtBadgeLoss;
import com.tce.smart.platform.core.entity.securityarea.SmtSecurityAreaNotify;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthDelete;
import com.tce.smart.platform.core.mapper.SmtSecurityAreaSupplierMapper;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtSecurityAreaSupplierService;
import com.tce.smart.platform.service.SmtSupplierPersonService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.IOUtils;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: SmtSecurityAreaSupplierServiceImpl
 * @date: 2020-07-21 9:19
 * @author: wuling
 * @version: 1.0
 */
@Service
@AllArgsConstructor
public class SmtSecurityAreaSupplierServiceImpl extends ServiceImpl<SmtSecurityAreaSupplierMapper, SmtSecurityAreaSupplier> implements SmtSecurityAreaSupplierService {

	private final SmtSecurityAreaSupplierMapper smtSecurityAreaSupplierMapper;

	private final SmtSupplierPersonService smtSupplierPersonService;

	private final SmtImageService smtImageService;

	private final SmtParkService smtParkService;

	private final ImageService imageService;

	@Override
	public IPage<SmtSecurityAreaSupplierDTO> getSecurityAreaSupplierPage(Page page, SmtSecurityAreaSupplierReqDTO smtSecurityAreaSupplierReqDTO) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		SmtSecurityAreaSupplierDTO smtSecurityAreaSupplierDTO = new SmtSecurityAreaSupplierDTO();
		BeanUtils.copyProperties(smtSecurityAreaSupplierReqDTO, smtSecurityAreaSupplierDTO);
		IPage<SmtSecurityAreaSupplierDTO> securityAreaSupplierPage = smtSecurityAreaSupplierMapper.getSecurityAreaSupplierPage(page, smtSecurityAreaSupplierDTO, parkIdList);
		//计算当前状态
		for (SmtSecurityAreaSupplierDTO item : securityAreaSupplierPage.getRecords()) {
			long currTime = new Date().getTime();
			long beginTime = item.getBeginEffectTime().getTime();
			long endTime = item.getEndEffectTime().getTime();
			if (currTime > beginTime && currTime < endTime) {
				//生效时间内
				item.setStatus(SecurityAreaSupplierEnum.ENABLE.getCode());
			} else {
				//生效时间外
				item.setStatus(SecurityAreaSupplierEnum.DISABLE.getCode());
			}
		}
		return securityAreaSupplierPage;
	}

	@Override
	public List<SecurityAreaSupplierDTO> getSecurityAreaSupplierList(String supplierName, Integer parkId) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		List<SecuritySupplierDTO> supplierList = this.baseMapper.getSupplierList(supplierName, parkId, parkIdList);

		//组织响应数据
		List<SecurityAreaSupplierDTO> securityAreaSupplierDTOS = new ArrayList<>();
		for (SecuritySupplierDTO supplierDTO : supplierList) {
			SecurityAreaSupplierDTO securityAreaSupplierDTO = new SecurityAreaSupplierDTO();
			BeanUtils.copyProperties(supplierDTO, securityAreaSupplierDTO);
			securityAreaSupplierDTOS.add(securityAreaSupplierDTO);
			if (StringUtils.isNotEmpty(supplierDTO.getProtocolCode())) {
				securityAreaSupplierDTO.setHasProtocol(1);
			} else {
				securityAreaSupplierDTO.setHasProtocol(0);
			}
			if (StrUtil.isNotBlank(supplierDTO.getCarryItem())) {
				securityAreaSupplierDTO.setItemList(Arrays.stream(supplierDTO.getCarryItem().split(",")).collect(Collectors.toList()));
			}
		}

		return securityAreaSupplierDTOS;
	}

	@Override
	public List<SecurityAreaSupplierListDTO> getSecurityAreaSupplier(String supplierName, Integer parkId) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		if (CollectionUtils.isEmpty(parkIdList)) {
			return null;
		}
		List<SecuritySupplierDTO> supplierList = this.baseMapper.getSupplierList(supplierName, parkId, parkIdList);
		//组织响应数据
		List<SecurityAreaSupplierDTO> securityAreaSupplierDTOS = new ArrayList<>();
		for (SecuritySupplierDTO supplierDTO : supplierList) {
			SecurityAreaSupplierDTO securityAreaSupplierDTO = new SecurityAreaSupplierDTO();
			BeanUtils.copyProperties(supplierDTO, securityAreaSupplierDTO);
			securityAreaSupplierDTOS.add(securityAreaSupplierDTO);
			if (StringUtils.isNotEmpty(supplierDTO.getProtocolCode())) {
				securityAreaSupplierDTO.setHasProtocol(1);
			} else {
				securityAreaSupplierDTO.setHasProtocol(0);
			}
			if (StrUtil.isNotBlank(supplierDTO.getCarryItem())) {
				securityAreaSupplierDTO.setItemList(Arrays.stream(supplierDTO.getCarryItem().split(",")).collect(Collectors.toList()));
			}

		}
		Map<Integer, List<SecurityAreaSupplierDTO>> map = securityAreaSupplierDTOS.stream()
				.collect(Collectors.groupingBy(SecurityAreaSupplierDTO::getParkId));
		List<SecurityAreaSupplierListDTO> resp = new ArrayList<>();
		parkIdList.forEach(id -> {
			SecurityAreaSupplierListDTO listDTO = new SecurityAreaSupplierListDTO();
			listDTO.setParkId(id);
			listDTO.setParkName(smtParkService.getById(id).getParkName());
			listDTO.setChildren(map.get(id));
			resp.add(listDTO);
		});
		return resp;
	}

	@Override
	public List<SmtSecurityAreaSupplierPersonRespDTO> getSecurityAreaSupplierPersonList(Long spId) {
		List<SmtSupplierPerson> smtSupplierPeople = smtSupplierPersonService.list(new LambdaQueryWrapper<SmtSupplierPerson>().eq(SmtSupplierPerson::getSupplierId, spId));
		List<SmtSecurityAreaSupplierPersonRespDTO> list = new ArrayList<>();
		smtSupplierPeople.forEach(item -> {
			SmtSecurityAreaSupplierPersonRespDTO respDTO = new SmtSecurityAreaSupplierPersonRespDTO();
			BeanUtils.copyProperties(item, respDTO);
			respDTO.setVisitCardId(item.getIdCard());
			list.add(respDTO);
		});
		return list;
	}

	@Transactional
	@Override
	public boolean saveSecurityAreaSupplier(SecurityAreaSupplierAddReqDTO supplierAddReqDTO) {

		SmtSecurityAreaSupplier smtSecurityAreaSupplier = new SmtSecurityAreaSupplier();
		BeanUtil.copyProperties(supplierAddReqDTO, smtSecurityAreaSupplier);

		if (supplierAddReqDTO.getId() != null) {
			//查询供应商记录
			SmtSecurityAreaSupplier areaSupplier = this.getById(supplierAddReqDTO.getId());
			if (null == areaSupplier) {
				//记录不存在
				throw new TCEException("记录不存在");
			}
			//修改记录
			if (CollectionUtil.isNotEmpty(supplierAddReqDTO.getProContents())) {
				//保存协议内容
				String codes = "";
				for (String base64Str : supplierAddReqDTO.getProContents()) {
					if (base64Str.startsWith("http")) {
						continue;
					}
					String code = smtImageService.saveImage(supplierAddReqDTO.getParkId(), base64Str, SmtImageEnum.SECURITY_AREA_PRO.getCode());
					if (!codes.equals("")) {
						codes += ",";
					}
					codes += code;
				}
				if (StringUtils.isNotEmpty(codes)) {
					smtSecurityAreaSupplier.setProtocolCode(codes);
				}
			}
			if (areaSupplier.getEndEffectTime().getTime() != supplierAddReqDTO.getEndEffectTime().getTime()) {
				//协议到期时间有更改 通知状态重置为未通知
				smtSecurityAreaSupplier.setNotifyFlag(SupplierNotifyEnum.NON_NOTIFY.getCode());
			}
			smtSecurityAreaSupplier.setUpdateTime(new Date());
			return this.updateById(smtSecurityAreaSupplier);
		}
		//查询最后添加的记录
		List<SmtSecurityAreaSupplier> list = this.list(new LambdaQueryWrapper<SmtSecurityAreaSupplier>()
				.eq(SmtSecurityAreaSupplier::getParkId, smtSecurityAreaSupplier.getParkId())
				.orderByDesc(SmtSecurityAreaSupplier::getCreateTime));
		Integer newSerNum = 0;
		if (CollectionUtil.isNotEmpty(list)) {
			newSerNum = list.get(0).getSerNum() + 1;
			if (1000 == newSerNum) {
				throw new SmartException("已超过保密区编号上限");
			}
		}
		String compCode = "";
		for (int i = 0; i < (3 - String.valueOf(newSerNum).length()); i++) {
			compCode += "0";
		}
		compCode += newSerNum;
		if (CollectionUtil.isNotEmpty(supplierAddReqDTO.getProContents())) {
			//保存协议内容
			String codes = "";
			for (String base64Str : supplierAddReqDTO.getProContents()) {
				String code = smtImageService.saveImage(supplierAddReqDTO.getParkId(), base64Str, SmtImageEnum.SECURITY_AREA_PRO.getCode());
				if (!codes.equals("")) {
					codes += ",";
				}
				codes += code;
			}
			smtSecurityAreaSupplier.setProtocolCode(codes);
		}
		smtSecurityAreaSupplier.setSerNum(newSerNum);
		smtSecurityAreaSupplier.setCompanyCode(compCode);
		smtSecurityAreaSupplier.setNotifyFlag(SupplierNotifyEnum.NON_NOTIFY.getCode());
		smtSecurityAreaSupplier.setCreateTime(new Date());
		return this.save(smtSecurityAreaSupplier);
	}


	@Override
	public ResponseEntity<byte[]> downLoadExcel(Integer parkId) {
		//获得导出数据
		List<SmtSecurityAreaSupplier> list = this.list(new LambdaQueryWrapper<SmtSecurityAreaSupplier>()
				.eq(SmtSecurityAreaSupplier::getParkId, parkId)
				.orderByDesc(SmtSecurityAreaSupplier::getCreateTime));
		if (CollUtil.isEmpty(list)) {
			throw new SmartException("暂无保密区供应商记录");
		}
		List<SecurityAreaSupplierExcelRespDTO> data = new ArrayList<>();
		list.forEach(supplier -> {
			SecurityAreaSupplierExcelRespDTO resp = BeanUtils.transform(SecurityAreaSupplierExcelRespDTO.class, supplier);
			resp.setEndEffectTime(DateUtil.formatDate(supplier.getEndEffectTime()));
			resp.setBeginEffectTime(DateUtil.formatDate(supplier.getBeginEffectTime()));
			data.add(resp);
		});
		ResponseEntity<byte[]> responseEntity;
		String fileName = ExportTypeEnum.SECURITY_SUPPLIER.getDesc() + SymbolConstants.FULL_POINT + ExportTypeEnum.SECURITY_SUPPLIER.getFileSuffix();
		try (Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(), SecurityAreaSupplierExcelRespDTO.class, data)) {
			responseEntity = IOUtils.getExcelResponse(fileName, workbook);
			//responseEntity = IOUtils.getExcelResp(fileName, workbook);
		} catch (IOException e) {
			log.error("excel导出异常", e);
			throw new SmartException(ExceptionEnum.UNKNOWN.getCode(), "excel导出异常");
		}
		return responseEntity;
	}

	@Transactional
	@Override
	public boolean uploadSecurityAreaSupplier(SecurityAreaSupplierUploadReqDTO supplierUploadReqDTO) {
		Assert.notNull(supplierUploadReqDTO.getParkId(), "园区不能为NULL");
		Assert.notNull(supplierUploadReqDTO.getSupplierType(), "供应商类型不能为NULL");
		if (CollectionUtil.isNotEmpty(supplierUploadReqDTO.getSupplierInfoList())) {
			supplierUploadReqDTO.getSupplierInfoList().forEach(item -> {
				SecurityAreaSupplierAddReqDTO supplierAddReqDTO = new SecurityAreaSupplierAddReqDTO();
				BeanUtil.copyProperties(item, supplierAddReqDTO);
				supplierAddReqDTO.setParkId(supplierUploadReqDTO.getParkId());
				supplierAddReqDTO.setSupplierType(supplierUploadReqDTO.getSupplierType());
				this.saveSecurityAreaSupplier(supplierAddReqDTO);
			});
		}
		return true;
	}

	@Override
	public boolean delSecurityAreaSupplier(Long id) {
		//判断供应商人员是否存在
		List<SmtSupplierPerson> supplierPersonList = smtSupplierPersonService.list(new QueryWrapper<SmtSupplierPerson>().lambda().eq(SmtSupplierPerson::getSupplierId, id));
		if (CollectionUtil.isNotEmpty(supplierPersonList)) {
			throw new TCEException("已关联授权人员，请删除授权人员后在删除供应商");
		}
		return this.removeById(id);
	}

	@Override
	public SecurityAreaSupplierDetailDTO getDetailById(Long id) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		SmtSecurityAreaSupplier supplier = this.getById(id);
		if (null == supplier || !parkIds.contains(supplier.getParkId())) {
			throw new TCEException("供应商记录不存在");
		}
		SmtPark park = smtParkService.getById(supplier.getParkId());
		SecurityAreaSupplierDetailDTO detailDTO = new SecurityAreaSupplierDetailDTO();
		BeanUtil.copyProperties(supplier, detailDTO);
		detailDTO.setParkName(park.getParkName());

		List<String> content = new ArrayList<>();
		if (ProtocolTypeEnum.IMG.getCode().equals(supplier.getProtocolType())) {
			//图片
			String[] codes = supplier.getProtocolCode().split(",");
			for (String code : codes) {
				String imageUrl = imageService.buildImageUrl(code);
				content.add(imageUrl);
			}
		} else if (ProtocolTypeEnum.PDF.getCode().equals(supplier.getProtocolType())) {
			//PDF
			String downUrl = imageService.buildDownloadUrl(supplier.getProtocolCode(), supplier.getProtocolName());
			content.add(downUrl);
		}
		if (StrUtil.isNotBlank(supplier.getCarryItem())) {
			detailDTO.setItemList(Arrays.stream(supplier.getCarryItem().split(",")).collect(Collectors.toList()));
		}
		detailDTO.setContents(content);
		return detailDTO;
	}

	@Override
	public boolean delBatchSupplier(List<Long> ids) {
		for (Long id : ids) {
			//判断供应商人员是否存在
			List<SmtSupplierPerson> supplierPersonList = smtSupplierPersonService.list(new QueryWrapper<SmtSupplierPerson>().lambda().eq(SmtSupplierPerson::getSupplierId, id));
			if (CollectionUtil.isEmpty(supplierPersonList)) {
				this.removeById(id);
			}
		}
		return true;
	}

	@Override
	public boolean batchSetAuthor(SecuritySupplierAddAuthorReqDTO authorReqDTO) {
		List<SmtSecurityAreaSupplier> suppliers = new ArrayList<>();
		authorReqDTO.getIds().forEach(item -> {
			SmtSecurityAreaSupplier supplier = new SmtSecurityAreaSupplier();
			supplier.setId(Long.parseLong(item));
			supplier.setAuthorList(authorReqDTO.getAuthorList());
			suppliers.add(supplier);
		});
		return this.updateBatchById(suppliers);
	}

	@Override
	public boolean updateNotifyStatus(SupplierNotifyStatusReqDTO statusReqDTO) {
		SmtSecurityAreaSupplier supplier = new SmtSecurityAreaSupplier();
		supplier.setId(statusReqDTO.getId());
		supplier.setNotifyFlag(statusReqDTO.getNotifyStatus());
		supplier.setUpdateTime(new Date());
		return this.updateById(supplier);
	}

	@Override
	public List<SmtSecurityAreaSupplierRespDTO> notifyList(SecurityAreaNotifyListDTO notifyListDTO) {
		List<SmtSecurityAreaSupplierDTO> notifyList = this.baseMapper.getNotifyList(notifyListDTO.getParkId(), notifyListDTO.getDays());

		List<SmtSecurityAreaSupplierRespDTO> supplierRespDTOS = new ArrayList<>();
		if (CollectionUtil.isNotEmpty(notifyList)) {
			notifyList.forEach(item -> {
				SmtSecurityAreaSupplierRespDTO respDTO = new SmtSecurityAreaSupplierRespDTO();
				BeanUtil.copyProperties(item, respDTO);
				supplierRespDTOS.add(respDTO);
			});
		}
		return supplierRespDTOS;
	}

	@Override
	public boolean notifyConfig(SecurityAreaNotifyConfigDTO configReqDTO) {
		SmtSecurityAreaNotify notify = new SmtSecurityAreaNotify();
		BeanUtil.copyProperties(configReqDTO, notify);
		notify.setAccountList(configReqDTO.getAccounts().stream().collect(Collectors.joining(",")));
		notify.setDelFlag(DeleteStatusEnum.NOT_DELETE.getCode());
		notify.setCreateTime(new Date());
		return notify.insert();
	}

	@Override
	public SecurityAreaNotifyConfigDTO getNotifyConfig(Integer parkId) {
		SmtSecurityAreaNotify notify = new SmtSecurityAreaNotify();
		notify = notify.selectOne(new LambdaQueryWrapper<SmtSecurityAreaNotify>()
				.eq(SmtSecurityAreaNotify::getParkId, parkId)
				.eq(SmtSecurityAreaNotify::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode())
		);
		SecurityAreaNotifyConfigDTO configDTO = new SecurityAreaNotifyConfigDTO();
		BeanUtil.copyProperties(notify, configDTO);
		if (null != notify && StringUtils.isNotEmpty(notify.getAccountList())) {
			configDTO.setAccounts(Arrays.asList(notify.getAccountList().split(",")));
		}
		return configDTO;
	}

	@Override
	public List<SecurityAreaNotifyConfigDTO> getNotifyAllConfig() {
		SmtSecurityAreaNotify notify = new SmtSecurityAreaNotify();
		List<SmtSecurityAreaNotify> configs = notify.selectList(new LambdaQueryWrapper<SmtSecurityAreaNotify>()
				.eq(SmtSecurityAreaNotify::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode())
		);
		List<SecurityAreaNotifyConfigDTO> configDTOS = new ArrayList<>();
		if (CollectionUtil.isNotEmpty(configs)) {
			configs.forEach(item -> {
				SecurityAreaNotifyConfigDTO configDTO = new SecurityAreaNotifyConfigDTO();
				BeanUtil.copyProperties(item, configDTO);
				configDTO.setAccounts(Arrays.asList(item.getAccountList().split(",")));
				configDTOS.add(configDTO);
			});
		}
		return configDTOS;
	}
}
