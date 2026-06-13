package com.tce.smart.platform.biz;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.platform.SmartPlatformApplication;
import com.tce.smart.platform.core.dto.SearchStaffDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.service.SmtTaskDownRecordService;
import com.tce.smart.platform.core.vo.StaffListVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskServiceTypeEnum;
import com.tce.smart.tool.enums.VehicleBelongTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @description: ExportStaffImgTest
 * @date: 2020/11/25 19:54
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SmartPlatformApplication.class)
public class ExportStaffImgTest {

	@Resource
	private SmtStaffMapper smtStaffMapper;

	@Resource
	private SmtImageService smtImageService;


	/**
	 * 导出员工图片
	 */
	//@Test
	public void test1(){
		Page page = new Page(1,-1);
		SearchStaffDTO staffDTO = new SearchStaffDTO();
		staffDTO.setFacePicId("1");
		List<Integer> parkIdList = new ArrayList<>();
		parkIdList.add(161);
		parkIdList.add(27);
		IPage<StaffListVO> smtStaffPage = smtStaffMapper.getSmtStaffPage(page, staffDTO, parkIdList);
		if(CollectionUtil.isNotEmpty(smtStaffPage.getRecords())){
			smtStaffPage.getRecords().forEach(item -> {
				if(item.getStatus() == 0){
					return;
				}
				String filePath = "E:\\裕同石岩-龙岗人脸图片\\" + item.getCertno()+".jpg";
				File file = new File(filePath);
				if(file.exists()){
					return;
				}

				String filePath2 = "E:\\裕同石岩-龙岗人脸图片2\\" + item.getCertno()+".jpg";
				File file2 = new File(filePath2);
				if(file2.exists()){
					return;
				}

				byte[] imageBinaryByCode = smtImageService.getImageBinaryByCode(item.getFacePicId());
				try {
					FileOutputStream fileOutputStream = new FileOutputStream(file2);
					fileOutputStream.write(imageBinaryByCode);
					fileOutputStream.flush();
					fileOutputStream.close();
				}catch (Exception e){
					e.printStackTrace();
				}
			});
		}
	}
}
