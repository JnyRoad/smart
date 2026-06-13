package com.tce.smart.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.dto.SearchPhotoRecordDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtStaffPhotoUploadRecord;
import com.tce.smart.platform.core.mapper.SmtStaffPhotoUploadRecordMapper;
import com.tce.smart.platform.core.vo.StaffPhotoUploadDetailVO;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtStaffPhotoUploadRecordService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@AllArgsConstructor
@Service
public class SmtStaffPhotoUploadRecordServiceImpl extends ServiceImpl<SmtStaffPhotoUploadRecordMapper, SmtStaffPhotoUploadRecord> implements SmtStaffPhotoUploadRecordService {

	@Autowired
	private SmtParkBuService smtParkBuService;

	@Autowired
	private ImageService imageService;

	@Override
	public IPage<SmtStaffPhotoUploadRecord> getSmtRecordPage(Page page, SearchPhotoRecordDTO searchPhotoRecordDTO) {
		// TODO Auto-generated method stub
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		return this.baseMapper.getSmtRecordPage(page,searchPhotoRecordDTO,parkIdList);
	}

	@Override
	public StaffPhotoUploadDetailVO getSmtRecordDetail(Integer id) {
		// TODO Auto-generated method stub

		SmtStaffPhotoUploadRecord selectById = this.baseMapper.selectById(id);
		StaffPhotoUploadDetailVO vo=new StaffPhotoUploadDetailVO();
		BeanUtil.copyProperties(selectById,vo);
		 List<SmtPark> parkList = smtParkBuService.getParkListByBu(Long.parseLong(selectById.getCompId()));
		 String parkName="";
		 for (SmtPark smtPark : parkList) {
			 parkName+=smtPark.getParkName()+",";
		}

		 if(!parkName.equals("")) {
			 parkName=parkName.substring(0,parkName.length()-1);
		 }
		 vo.setParkName(parkName);
		 //SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		 vo.setCreateTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(selectById.getCreateTime()));
		 if(selectById.getStatus().equals(1) && selectById.getFacePicId()!=null)
		 {
			 String facePicUrl = imageService.buildImageUrl(selectById.getFacePicId());
			 vo.setFacePicUrl(facePicUrl);
		 }
		 return vo;
	}

}
