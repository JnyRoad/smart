package com.tce.smart.platform.biz;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.platform.SmartPlatformApplication;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtTaskDownRecordService;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @description: 导入龙岗车辆
 * @date: 2020/11/25 19:54
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SmartPlatformApplication.class)
@org.junit.Ignore("历史手工脚本壳：@Test 已全部注释，无可运行用例，整类忽略以避免 surefire 报 initializationError")
public class AddLGCarTest {
	@Resource
	private SmtStaffService smtStaffService;

	@Resource
	private SmtVehicleService smtVehicleService;

	@Resource
	private SmtVehicleApplyService smtVehicleApplyService;

	@Resource
	private SmtVehicleStaffService smtVehicleStaffService;

	@Resource
	private SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;

	@Resource
	private SmtDeviceTaskService smtDeviceTaskService;

	@Resource
	private SmtTaskDownRecordService smtTaskDownRecordService;

	/**
	 * 导入员工车辆
	 */
	//@Test
	public void test1(){
		Map<String,String> map = new HashMap<>();
		map.put("粤BW7H69","001196");
		map.put("粤SU220D","LG006756");
		map.put("粤B7F925","028518");
		map.put("粤L21W42","035937");
		map.put("浙C7PA92","LG004157");
		map.put("粤B1837Q","000568");
		map.put("赣CD2973","LG006752");
		map.put("粤BT531C","001622");
		map.put("粤S714JH","LG007256");
		map.put("粤P9Z158","036934");
		map.put("湘FLB387","LG003382");
		map.put("粤B4L6C8","LG001776");
		map.put("鄂KD2X30","cy1353");
		map.put("粤B8K63N","LG007101");
		map.put("粤L3F180","LG003125");
		map.put("粤PJ0738","LG007697");
		map.put("桂GZH767","052180");
		map.put("苏E66C3H","LG006070");
		map.put("粤BJ8B47","LG007868");
		map.put("粤S2K3K7","LG008117");
		map.put("粤L222QE","LG008454");
		map.put("粤S4NS35","LG002370");
		map.put("豫Q1C243","031979");
		map.put("粤L66K4I","LG008379");
		map.put("粤S12FZ2","011681");
		map.put("湘MPM626","035611");
		map.put("豫PK972G","035238");
		map.put("鄂A425HL","051550");
		map.put("湘L2VY05","025280");
		map.put("桂GTZ381","LG002014");
		map.put("粤S2DX48","LG002864");
		map.put("粤ST357S","LG003086");
		map.put("粤BZ86B8","LG001145");
		map.put("粤B8BP90","LG004162");
		map.put("湘LR9021","012945");
		map.put("粤RFF787","LG002506");
		map.put("湘LKC855","LG003872");
		map.put("粤S426RW","030626");
		map.put("鄂DR36W9","LG002930");
		map.put("赣G09Z27","LG000128");
		map.put("粤MP2007","LG000137");
		map.put("粤S5XS96","051063");
		map.put("粤S3NV88","LG005343");
		map.put("赣G99S65","070268");
		map.put("鄂DS56E1","018751");
		map.put("粤QBX291","050988");
		map.put("湘DAQ182","024520");
		map.put("粤S6G7P6","LG005684");
		map.put("粤S9G1H1","LG006122");
		map.put("粤S6H5Y6","LG004167");
		map.put("粤M94901","LG005414");
		map.put("粤BL0S08","050778");
		map.put("粤S8XA96","033756");
		map.put("赣D0K066","LG002782");
		map.put("桂DU9931","LG002797");
		map.put("粤B7A0X0","LG003627");
		map.put("粤L19M09","LG007188");
		map.put("粤L45J21","X1042688");
		map.put("湘E86K18","LG000325");
		map.put("皖K095Q2","113850");
		map.put("粤TY191J","LG007026");
		map.put("鄂A2UD61","cy1547");
		map.put("粤S0AC40","061678");
		map.put("湘C72C82","LG008319");
		map.put("贵D3N581","LG002332");
		map.put("鄂A2M9F6","LG008320");
		map.put("粤J45M65","LG007074");
		map.put("赣CFF253","LG003859");
		map.put("桂A962Y9","JX201906");
		map.put("粤L91P50","001002");
		map.put("粤B1Y62E","030859");
		map.put("湘B7U883","LG005374");
		map.put("粤BQ7V86","093313");
		map.put("云DCQ787","024238");
		map.put("豫Q75S99","080092");
		map.put("粤J965P2","035640");
		map.put("渝A1GR63","031565");
		map.put("云COH688","LG006215");
		map.put("贵HRY618","035050");
		map.put("粤L090RH","007797");
		map.put("粤B76PG3","LG007561");
		map.put("粤B19QJ9","080974");
		map.put("赣G2812P","LG002972");
		map.put("桂EF5887","012830");
		map.put("粤L8190K","035108");
		map.put("湘M69177","LG001196");
		map.put("粤S9NE52","LG003049");
		map.put("粤D92T47","030756");
		map.put("湘AD1L15","LG000124");
		map.put("粤L05N26","023046");
		map.put("粤BZ2P39","005529");
		map.put("粤FWN862","014443");
		map.put("粤L93957","035221");
		map.put("鄂J3U587","LG000605");
		map.put("湘JU3786","031730");
		map.put("粤RRC879","LG000792");
		map.put("湘L6MK00","050844");
		map.put("粤SK79L7","LG006380");
		map.put("湘EP9605","021543");
		map.put("湘AQP128","LG005596");
		map.put("湘L6JF60","AU00691");
		map.put("湘E51E63","LG000118");
		map.put("豫R958ZK","031651");
		map.put("粤K2A685","LG005486");
		map.put("桂C2M027","034771");
		map.put("粤KYF152","LG002068");
		map.put("豫SS3A61","LG006634");
		map.put("粤BDJ3855","LG006763");
		map.put("粤LYL092","LG004015");
		map.put("桂GHE136","LG006422");
		map.put("湘DHD398","016396");
		map.put("赣BZK182","LG003999");
		map.put("桂PE9802","035830");
		map.put("粤WMA869","LG000246");
		map.put("川R27L77","033872");
		map.put("粤FYV600","LG006774");
		map.put("鄂C05X56","LG007245");
		map.put("桂MG0163","LG006511");
		map.put("湘DHZ589","LG004121");
		map.put("湘EHS201","LG003439");
		map.put("豫Q90Z19","035145");
		map.put("粤BT18Z2","LG007060");
		map.put("赣B368J5","LG007641");
		map.put("豫PVB897","AU01207");
		map.put("粤L60V25","LG004382");
		map.put("桂C67755","LG007312");
		map.put("粤F87D80","LG003282");
		map.put("粤ADE8853","LG007448");
		map.put("粤PB9579","LG008299");
		map.put("赣A3071E","LG008318");
		map.put("渝F21M95","LG004916");
		map.put("粤WU5672","LG008414");
		map.put("湘GH8592","LG007548");
		map.put("粤TX369V","LG007569");
		map.put("鄂JJ1992","LG004931");
		map.put("豫N16F68","LG006364");
		map.put("粤B5Z8T9","LG001144");
		map.put("桂EY3384","LG007332");
		map.put("桂MHS265","XF0022");
		map.put("湘DHE061","LG007143");
		map.put("渝A6GY62","LG007597");
		map.put("贵C4G155","LG007639");

		int authId = 5000065;

		int parkId = 27;

		List<SmtDeviceAuthorityRelation> authorityRelations = smtDeviceAuthorityRelationService.list(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>()
				.eq(SmtDeviceAuthorityRelation::getAuthorityId, authId)
		);

		for(String platStr : map.keySet()){
			String badge = map.get(platStr);
			addStaffCar(badge,platStr,authorityRelations,authId,parkId);
		}
//		for(String platStr : map.keySet()) {
//			List<SmtVehicle> vehicleList = smtVehicleService.list(new LambdaQueryWrapper<SmtVehicle>().eq(SmtVehicle::getVehiclePlate, platStr));
//			if(CollectionUtil.isEmpty(vehicleList)){
//				log.info("车辆未添加:{}",platStr);
//			}
//		}
	}


	@Transactional
	public void addStaffCar(String badge,String plate,List<SmtDeviceAuthorityRelation> authorityRelations,int authId,int parkId){
		//获取员工信息
		SmtStaff staff = smtStaffService.getOne(new LambdaQueryWrapper<SmtStaff>().eq(SmtStaff::getBadge, badge));
		if(null == staff){
			//员工信息为null
			log.info("员工不存在:{}",badge);
			return;
		}

		SmtVehicle smtVehicle = smtVehicleService.getOne(new LambdaQueryWrapper<SmtVehicle>()
				.eq(SmtVehicle::getVehiclePlate, plate)
				.eq(SmtVehicle::getParkId,parkId)
		);

		if(null != smtVehicle){
			//判断车辆是否下发过
			int count = smtDeviceTaskService.count(new LambdaQueryWrapper<SmtDeviceTask>()
					.eq(SmtDeviceTask::getCardNo,smtVehicle.getId())
			);
			if(count > 0){
            } else {
				//重新导入车辆数据 和下发权限
				//删除车辆数据
				smtVehicleService.removeById(smtVehicle.getId());
				//删除员工车辆关联数据
				smtVehicleStaffService.remove(new LambdaQueryWrapper<SmtVehicleStaff>()
						.eq(SmtVehicleStaff::getVehicleId,smtVehicle.getId())
						.eq(SmtVehicleStaff::getStaffId,staff.getId())
				);
				//删除车辆申请表
				smtVehicleApplyService.remove(new LambdaQueryWrapper<SmtVehicleApply>()
						.eq(SmtVehicleApply::getVehicleId,smtVehicle.getId())
						.eq(SmtVehicleApply::getVehiclePlate,plate)
				);

				//添加车辆表
				smtVehicle = new SmtVehicle();
				smtVehicle.setParkId(String.valueOf(parkId));
				smtVehicle.setVehiclePlate(plate);
				smtVehicle.setVehicleBrand("其他");
				smtVehicle.setVehicleColor(4);            //黑色
				smtVehicle.setVehicleType(2);            //小型车
				smtVehicle.setVehicleAscription(1);        //员工车辆
				smtVehicle.setCreateTime(LocalDateTime.now());
				smtVehicle.setIsDelete(0);
				smtVehicleService.save(smtVehicle);

				//添加员工车辆表
				SmtVehicleStaff smtVehicleStaff = new SmtVehicleStaff();
				smtVehicleStaff.setStaffId(staff.getId());
				smtVehicleStaff.setVehicleId(smtVehicle.getId());
				smtVehicleStaffService.save(smtVehicleStaff);

				//添加车辆申请表
				SmtVehicleApply smtVehicleApply = new SmtVehicleApply();
				smtVehicleApply.setVehicleId(smtVehicle.getId());
				smtVehicleApply.setParkId(String.valueOf(parkId));
				smtVehicleApply.setStatus(1);        //直接通过
				smtVehicleApply.setApprover("platform");
				smtVehicleApply.setCreateTime(LocalDateTime.now());
				smtVehicleApply.setVehiclePlate(plate);
				smtVehicleApply.setAuthorityId(authId);
				smtVehicleApplyService.save(smtVehicleApply);

				//写入设备权限
				for(SmtDeviceAuthorityRelation smtDeviceAuthorityRelation : authorityRelations){
					String sNo = UUID.randomUUID().toString().replaceAll("-", "");
					SmtDeviceTask smtDeviceTask = new SmtDeviceTask();
					smtDeviceTask.setAction(DeviceTaskActionEnum.DOWN.getCode());
					smtDeviceTask.setStatus(0);
					smtDeviceTask.setDeviceType(2);
					smtDeviceTask.setOverTime(33117041415L);				//最大值
					smtDeviceTask.setStartTime(DateUtils.currentSeconds() + (60 * 60));
					smtDeviceTask.setTimes(27);
					smtDeviceTask.setDeviceCode(smtDeviceAuthorityRelation.getDeviceId());
					smtDeviceTask.setCardNo(smtVehicle.getId().toString());
					smtDeviceTask.setGeneral(smtVehicle.getVehiclePlate());
					smtDeviceTask.setCardType(SmtVisitorEnum.CARD_TYPE_1.getType());
					smtDeviceTask.setServiceType(DeviceTaskServiceTypeEnum.CAR_STAFF.getCode());
					smtDeviceTask.setSerialNo(sNo);
					smtDeviceTaskService.save(smtDeviceTask);
				}
			}
		}
	}


	/**
	 * 导入公司车辆
	 */
	//@Test
	public void test2(){
		Map<String,String> map = new HashMap<>();

		map.put("粤B095L7","LG000195");
		map.put("粤B0K70F","051215");
		map.put("粤B1ZY08","023533");
		map.put("粤B2G02F","050553");
		map.put("粤B2X05U","051945");
		map.put("粤B3H716","017787");
		map.put("粤B4M33H","GY0020");
		map.put("粤B5566Y","000115");
		map.put("粤B5G0Z7","DG001037");
		map.put("粤B627ZG","034852");
		map.put("粤B69BD6","052203");
		map.put("粤B705LY","051126");
		map.put("粤B7N25P","052203");
		map.put("粤B7Q8E0","000115");
		map.put("粤B7V56R","YXKJ131");
		map.put("粤B83D17","035611");
		map.put("粤B867LU","051126");
		map.put("粤B86K79","DIT004043");
		map.put("粤B8D76C","029030");
		map.put("粤B8E22L","051126");
		map.put("粤B9US25","000115");
		map.put("粤BA22J2","051039");
		map.put("粤BA699W","050026");
		map.put("粤BB14U0","030140");
		map.put("粤BB51V2","LG001513");
		map.put("粤BD716B","051912");
		map.put("粤BDR221","050831");
		map.put("粤BE1P56","000115");
		map.put("粤BG03J7","DG000976");
		map.put("粤BJ366C","IDP001");
		map.put("粤BM15V7","hy0610");
		map.put("粤BQQ365","011019");
		map.put("粤BY365T","050195");
		map.put("粤BY370T","060458");
		map.put("粤BZ107Z","016257");
		map.put("粤BZ120港","006352");
		map.put("粤S1YJ07","DIT002016");
		map.put("粤S7U9P1","D20016");
		map.put("粤B3SZ55","060778");

		int F = 5000021;

		List<SmtDeviceAuthorityRelation> authorityRelationsF = smtDeviceAuthorityRelationService.list(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>()
				.eq(SmtDeviceAuthorityRelation::getAuthorityId, F)
		);


		for(String plate : map.keySet()){
			String badge = map.get(plate);
			//获取员工信息
			SmtStaff staff = smtStaffService.getOne(new LambdaQueryWrapper<SmtStaff>().eq(SmtStaff::getBadge, badge));
			if(null == staff){
				//员工信息为null
				log.info("员工不存在:{}",badge);
				continue;
			}

			int count = smtVehicleService.count(new LambdaQueryWrapper<SmtVehicle>().eq(SmtVehicle::getVehiclePlate, plate));
			if(count > 0){
				log.info("che add :{}",plate);
				continue;
			}
			addData(plate,staff.getId(),F,authorityRelationsF);
		}
	}

	@Transactional
	public void addData(String plate,Long staffId,Integer authId, List<SmtDeviceAuthorityRelation> authorityRelations){
//添加车辆表
		SmtVehicle smtVehicle = new SmtVehicle();
		smtVehicle.setParkId("161");
		smtVehicle.setVehiclePlate(plate);
		smtVehicle.setVehicleBrand("其他");
		smtVehicle.setVehicleColor(4);			//黑色
		smtVehicle.setVehicleType(2);			//小型车
		smtVehicle.setVehicleAscription(VehicleBelongTypeEnum.PARK_VEHICLE.getCode());
		smtVehicle.setCreateTime(LocalDateTime.now());
		smtVehicle.setIsDelete(0);
		smtVehicleService.save(smtVehicle);

		//添加员工车辆表
		SmtVehicleStaff smtVehicleStaff = new SmtVehicleStaff();
		smtVehicleStaff.setStaffId(staffId);
		smtVehicleStaff.setVehicleId(smtVehicle.getId());
		smtVehicleStaffService.save(smtVehicleStaff);

		//添加车辆申请表
		SmtVehicleApply smtVehicleApply = new SmtVehicleApply();
		smtVehicleApply.setVehicleId(smtVehicle.getId());
		smtVehicleApply.setParkId("161");
		smtVehicleApply.setStatus(1);		//直接通过
		smtVehicleApply.setCreateTime(LocalDateTime.now());
		smtVehicleApply.setVehiclePlate(plate);
		smtVehicleApply.setAuthorityId(authId);

		smtVehicleApplyService.save(smtVehicleApply);

		//写入设备权限
		for(SmtDeviceAuthorityRelation smtDeviceAuthorityRelation : authorityRelations){
			String sNo = UUID.randomUUID().toString().replaceAll("-", "");
			SmtDeviceTask smtDeviceTask = new SmtDeviceTask();
			smtDeviceTask.setAction(DeviceTaskActionEnum.DOWN.getCode());
			smtDeviceTask.setStatus(0);
			smtDeviceTask.setDeviceType(2);
			smtDeviceTask.setOverTime(33117041415L);				//最大值
			smtDeviceTask.setStartTime(DateUtils.currentSeconds() + (60 * 60));
			smtDeviceTask.setTimes(100);
			smtDeviceTask.setDeviceCode(smtDeviceAuthorityRelation.getDeviceId());
			smtDeviceTask.setCardNo(smtVehicle.getId().toString());
			smtDeviceTask.setGeneral(smtVehicle.getVehiclePlate());
			smtDeviceTask.setCardType(SmtVisitorEnum.CARD_TYPE_1.getType());
			smtDeviceTask.setServiceType(DeviceTaskServiceTypeEnum.CAR_COMPANY.getCode());
			smtDeviceTask.setSerialNo(sNo);
			smtDeviceTaskService.save(smtDeviceTask);
		}
	}
}
