package com.tce.smart.platform.biz;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.data.api.feign.xcc6.RemoteXCRsEmpService;
import com.tce.smart.platform.SmartPlatformApplication;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtDormitoryStaffHistoryMapper;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.service.SmtTaskDownRecordService;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskServiceTypeEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import com.tce.smart.tool.enums.SmtImageEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import sun.misc.BASE64Encoder;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @description: AddDormitoryTest
 * @date: 2020/12/11 19:54
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SmartPlatformApplication.class)
public class ModiftyDormitorData {
	@Resource
	private SmtStaffService smtStaffService;

	@Resource
	private SmtDormitoryRoomService smtDormitoryRoomService;

	@Resource
	private SmtDormitoryService smtDormitoryService;

	@Resource
	private SmtDormitoryFloorService smtDormitoryFloorService;

	@Resource
	private SmtDormitoryBedService smtDormitoryBedService;

	@Resource
	private SmtDormitoryStaffService smtDormitoryStaffService;

	@Resource
	private SmtDeviceTaskService smtDeviceTaskService;

	@Resource
	private SmtDeviceService smtDeviceService;

	@Resource
	private SmtTaskDownRecordService smtTaskDownRecordService;

	@Resource
	private SmtVisitorService smtVisitorService;

	@Resource
	private SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;

	@Resource
	private SmtDormitoryStaffHistoryService smtDormitoryStaffHistoryService;

	@Resource
	private SmtDormitoryStaffHistoryMapper smtDormitoryStaffHistoryMapper;

	@Resource
	private RemoteXCRsEmpService remoteXCRsEmpService;

	@Resource
	private SmtImageService smtImageService;



	//@Test
	public void modifyData(){
		//查询错误数据
		List<SmtDormitoryStaffHistory> needModifyData = smtDormitoryStaffHistoryMapper.getNeedModifyData();

		if(CollectionUtil.isNotEmpty(needModifyData)){
			needModifyData.forEach(item -> {
				//查找对应的入住数据
				List<SmtDormitoryStaffHistory> smtDormitoryStaffHistories = smtDormitoryStaffHistoryService.list(new LambdaQueryWrapper<SmtDormitoryStaffHistory>()
						.eq(SmtDormitoryStaffHistory::getStaffBadge, item.getStaffBadge())
						.eq(SmtDormitoryStaffHistory::getType, 0)
						.lt(SmtDormitoryStaffHistory::getCreateTime,item.getCreateTime())
						.orderByDesc(SmtDormitoryStaffHistory::getCreateTime)
				);
				if(CollectionUtil.isEmpty(smtDormitoryStaffHistories)){
					return;
				}
				//第一条就是对应的入住数据
				int isSSS = 1;

				SmtDormitoryStaffHistory smtDormitoryStaffHistory = smtDormitoryStaffHistories.get(0);

				if(item.getRoomId().equals(smtDormitoryStaffHistory.getRoomId())){
					isSSS = 0;
				}

				item.setDormitoryId(smtDormitoryStaffHistory.getDormitoryId());
				item.setDormitoryName(smtDormitoryStaffHistory.getDormitoryName());
				item.setDormitoryTypeId(smtDormitoryStaffHistory.getDormitoryTypeId());
				item.setDormitoryTypeName(smtDormitoryStaffHistory.getDormitoryTypeName());
				item.setFloorId(smtDormitoryStaffHistory.getFloorId());
				item.setFloorName(smtDormitoryStaffHistory.getFloorName());
				item.setRoomId(smtDormitoryStaffHistory.getRoomId());
				item.setRoomName(smtDormitoryStaffHistory.getRoomName());
				item.setBedId(smtDormitoryStaffHistory.getBedId());
				item.setBedNumber(smtDormitoryStaffHistory.getBedNumber());
				item.setInTime(smtDormitoryStaffHistory.getInTime());

				//如果是同一个房间或者入住天数小于3天的 是否统计字段设置为否
				if((item.getTime().getTime() - item.getInTime().getTime()) / 1000 / 60 / 60 / 24 <= 3){
					isSSS = 0;
				}
				item.setStatisFlag(isSSS);

				smtDormitoryStaffHistoryService.updateById(item);
			});
		}
	}

	//@Test
	public void modifyStatisData(){
		//查询错误数据
		List<SmtDormitoryStaffHistory> needModifyData = smtDormitoryStaffHistoryMapper.getNeedModifyStatisData();

		if(CollectionUtil.isNotEmpty(needModifyData)){
			needModifyData.forEach(item -> {
				//查找对应的入住数据
				List<SmtDormitoryStaffHistory> smtDormitoryStaffHistories = smtDormitoryStaffHistoryService.list(new LambdaQueryWrapper<SmtDormitoryStaffHistory>()
						.eq(SmtDormitoryStaffHistory::getStaffBadge, item.getStaffBadge())
						.eq(SmtDormitoryStaffHistory::getType, 0)
						.eq(SmtDormitoryStaffHistory::getInTime,item.getInTime())
						.orderByDesc(SmtDormitoryStaffHistory::getCreateTime)
				);
				if(CollectionUtil.isEmpty(smtDormitoryStaffHistories)){
					return;
				}
				//第一条就是对应的入住数据
				int isSSS = 1;

				SmtDormitoryStaffHistory smtDormitoryStaffHistory = smtDormitoryStaffHistories.get(0);

				if(item.getRoomId().equals(smtDormitoryStaffHistory.getRoomId())){
					isSSS = 0;
				}

				item.setDormitoryId(smtDormitoryStaffHistory.getDormitoryId());
				item.setDormitoryName(smtDormitoryStaffHistory.getDormitoryName());
				item.setDormitoryTypeId(smtDormitoryStaffHistory.getDormitoryTypeId());
				item.setDormitoryTypeName(smtDormitoryStaffHistory.getDormitoryTypeName());
				item.setFloorId(smtDormitoryStaffHistory.getFloorId());
				item.setFloorName(smtDormitoryStaffHistory.getFloorName());
				item.setRoomId(smtDormitoryStaffHistory.getRoomId());
				item.setRoomName(smtDormitoryStaffHistory.getRoomName());
				item.setBedId(smtDormitoryStaffHistory.getBedId());
				item.setBedNumber(smtDormitoryStaffHistory.getBedNumber());
				item.setInTime(smtDormitoryStaffHistory.getInTime());

				//如果是同一个房间或者入住天数小于3天的 是否统计字段设置为否
				if((item.getTime().getTime() - item.getInTime().getTime()) / 1000 / 60 / 60 / 24 <= 3){
					isSSS = 0;
				}
				item.setStatisFlag(isSSS);

				smtDormitoryStaffHistoryService.updateById(item);
			});
		}
	}

	//@Test
	public void getEmpPhote(){
		List<String> empNoList = new ArrayList<>();
		empNoList.add("LY900021");
		empNoList.add("ZX2000004");
		empNoList.add("LH761680");
		empNoList.add("LH761810");
		empNoList.add("A180451");
		empNoList.add("LH761602");
		empNoList.add("A180614");
		empNoList.add("LA220008");
		empNoList.add("LH761698");
		empNoList.add("LH761702");
		empNoList.add("LH761707");
		empNoList.add("ZX2000156");
		empNoList.add("LH761632");
		empNoList.add("LH761730");
		empNoList.add("LA220006");
		empNoList.add("H761558");
		empNoList.add("A180371");
		empNoList.add("A180487");
		empNoList.add("LA180892");
		empNoList.add("ZX2000006");
		empNoList.add("LH761936");
		empNoList.add("LA180806");
		empNoList.add("LH761638");
		empNoList.add("LA220005");
		empNoList.add("LH761626");
		empNoList.add("A180630");
		empNoList.add("LH761630");
		empNoList.add("LH761922");
		empNoList.add("LM180122");
		empNoList.add("LA180928");
		empNoList.add("LH761677");
		empNoList.add("ZX2000310");
		empNoList.add("LA220003");
		empNoList.add("A180570");
		empNoList.add("LH761596");
		empNoList.add("LH761654");
		empNoList.add("A180600");
		empNoList.add("A180636");
		empNoList.add("A180366");
		empNoList.add("LA180734");
		empNoList.add("LH761919");
		empNoList.add("ZX2000027");
		empNoList.add("LA180804");
		empNoList.add("ZX2000153");
		empNoList.add("LH761678");
		empNoList.add("LH761645");
		empNoList.add("LH761690");
		empNoList.add("LH761627");
		empNoList.add("LH761794");
		empNoList.add("LA180771");
		empNoList.add("A180278");
		empNoList.add("LH220005");
		empNoList.add("ZX2000150");
		empNoList.add("A180628");
		empNoList.add("LA180698");
		empNoList.add("LC190044");
		empNoList.add("H761589");
		empNoList.add("A180340");
		empNoList.add("A180613");
		empNoList.add("LA220001");
		empNoList.add("ZX2000014");
		empNoList.add("A180510");
		empNoList.add("LH220008");
		empNoList.add("LH220006");
		empNoList.add("ZX2000012");
		empNoList.add("LA180766");
		empNoList.add("WL8003448");
		empNoList.add("LH761726");
		empNoList.add("ZX2000252");
		empNoList.add("LH761628");
		empNoList.add("LH761928");
		empNoList.add("LH220010");
		empNoList.add("LH761609");
		empNoList.add("A180646");
		empNoList.add("ZX2000015");
		empNoList.add("A180605");
		empNoList.add("LA180927");
		empNoList.add("ZX2000005");
		empNoList.add("LH761805");
		empNoList.add("LH761861");
		empNoList.add("ZX2000221");
		empNoList.add("A180308");
		empNoList.add("ZX2000276");
		empNoList.add("LA180929");
		empNoList.add("LA180787");
		empNoList.add("H761572");
		empNoList.add("LH761631");
		empNoList.add("H761560");
		empNoList.add("LH761673");
		empNoList.add("LH761943");
		empNoList.add("H761568");
		empNoList.add("LA180833");
		empNoList.add("H761594");
		empNoList.add("LA180925");
		empNoList.add("ZX2000030");
		empNoList.add("LA180799");
		empNoList.add("A180632");
		empNoList.add("H761586");
		empNoList.add("LH761668");
		empNoList.add("LC190004");
		empNoList.add("LH761679");
		empNoList.add("ZX2000292");
		empNoList.add("ZX2000122");
		empNoList.add("LH761887");
		empNoList.add("LH761784");
		empNoList.add("LA180923");
		empNoList.add("LH220011");
		empNoList.add("ZX2000242");
		empNoList.add("LH220004");
		empNoList.add("LA180932");
		empNoList.add("A180577");
		empNoList.add("LA220004");
		empNoList.add("A180509");
		empNoList.add("ZX2000260");
		empNoList.add("ZX2000243");
		empNoList.add("LH761705");
		empNoList.add("LA180796");
		empNoList.add("LA180801");
		empNoList.add("ZX2000236");
		empNoList.add("A180586");
		empNoList.add("LA180805");
		empNoList.add("LH220003");
		empNoList.add("A180655");
		empNoList.add("LH220013");
		empNoList.add("LM180124");
		empNoList.add("ZX2000195");
		empNoList.add("LH220002");
		empNoList.add("ZX2000106");
		empNoList.add("A180561");
		empNoList.add("LH761636");
		empNoList.add("ZX2000180");
		empNoList.add("LH761675");
		empNoList.add("ZX2000008");
		empNoList.add("LH220009");
		empNoList.add("LA180930");
		empNoList.add("LH761629");
		empNoList.add("LH761701");
		empNoList.add("A180261");
		empNoList.add("ZX2000148");
		empNoList.add("LH761706");
		empNoList.add("LH220001");
		empNoList.add("LA180874");
		empNoList.add("LA220009");
		empNoList.add("ZX2000199");
		empNoList.add("LH220012");
		empNoList.add("LA180916");
		empNoList.add("LC190015");
		empNoList.add("LC190003");
		empNoList.add("H761585");
		empNoList.add("ZX2000169");
		empNoList.add("LA220002");
		empNoList.add("A180601");
		empNoList.add("A180283");
		empNoList.add("LH761603");
		empNoList.add("LH220007");
		empNoList.add("LA220010");
		empNoList.add("ZX2000013");
		empNoList.add("LH761799");
		empNoList.add("ZX2000017");
		empNoList.add("LA180803");
		empNoList.add("LH761909");
		empNoList.add("LH761599");
		empNoList.add("ZX2000170");
		empNoList.add("ZX2000231");
		empNoList.add("LA180868");
		empNoList.add("LC190037");
		empNoList.add("A180656");
		empNoList.add("ZX2000018");
		empNoList.add("LA220007");
		empNoList.add("LH761676");
		empNoList.add("LH761853");
		empNoList.add("LA180912");
		empNoList.add("A180562");
		empNoList.add("H761579");
		empNoList.add("ZX2000001");
		empNoList.add("LH761598");
		empNoList.add("LH761727");
		empNoList.add("LH761767");
		empNoList.add("WH1801");
		empNoList.add("WL8002705");
		empNoList.add("WL8002962");
		empNoList.add("WL8002963");

		for(String empNo : empNoList){
			//查询员工是否存在
			SmtStaff staff = smtStaffService.getStaffByBadge(empNo);
			if(Objects.isNull(staff) || StringUtils.isNotEmpty(staff.getFacePicId())){
				continue;
			}

			//查询照片
			Result<Map<String, Object>> empPhotoRes = remoteXCRsEmpService.getEmpPhoto(empNo, SecurityConstants.FROM_IN);

			if(!empPhotoRes.isSuccess() || Objects.isNull(empPhotoRes.getData())){
				continue;
			}

			Map<String, Object> data = empPhotoRes.getData();

			if(data.get("EmpPhotoImage") == null){
				continue;
			}
			Blob blob=(Blob) data.get("EmpPhotoImage");

			try {
				InputStream msgContent = blob.getBinaryStream();
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				byte[] buffer = new byte[100];
				int n = 0;
				while (-1 != (n = msgContent.read(buffer))) {
					output.write(buffer, 0, n);
				}
				String result = new BASE64Encoder().encode(output.toByteArray());
				output.close();
				//保存照片
				String imgId = smtImageService.saveImage(10322,result, SmtImageEnum.TYPE_STAFF_FACE.getCode());
				staff.setFacePicId(imgId);

				smtStaffService.updateById(staff);
			}catch (Exception e){

			}
		}
	}
}
