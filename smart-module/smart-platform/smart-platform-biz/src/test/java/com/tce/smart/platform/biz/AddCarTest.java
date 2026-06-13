package com.tce.smart.platform.biz;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
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
import java.util.*;

/**
 * @description: AddCarTest
 * @date: 2020/11/25 19:54
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SmartPlatformApplication.class)
public class AddCarTest {
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

	@Resource
	private SmtDormitoryStaffService smtDormitoryStaffService;

	/**
	 * 导入员工车辆
	 */
	//@Test
	public void test1(){
		Map<String,String> map = new HashMap<>();
		map.put("川A1Q9X1_E","051870");
		map.put("川BUT631_E","103173");
		map.put("川ET6E81_E","050928");
		map.put("鄂JGS702_E","051838");
		map.put("鄂SKR372_E","050590");
		map.put("赣AVU970_E","051534");
		map.put("赣BPC570_E","052198");
		map.put("赣BTZ323_E","052091");
		map.put("赣F32W21_E","60362");
		map.put("赣G688F2_E","8003160");
		map.put("赣GQ0040_E","027306");
		map.put("鲁F3H983_F","104136");
		map.put("鲁F9331J_E","051744");
		map.put("苏ARX735_F","HZ00131");
		map.put("苏E76CQ1_E","122849");
		map.put("皖APE887_E","IDP046");
		map.put("湘DHU985_E","AYW0003");
		map.put("湘E183BW_E","051552");
		map.put("湘JHL936_E","M00006");
		map.put("湘MA317S_E","051087");
		map.put("湘MDR012_E","cy1224");
		map.put("豫A08TY0_E","104326");
		map.put("豫A251XY_E","052210");
		map.put("豫A793D0_E","052047");
		map.put("豫KQF119_E","HZ00095");
		map.put("豫R203TF_E","052237");
		map.put("豫SG8M52_E","052206");
		map.put("豫SL0S07_E","HZ00148");
		map.put("豫SW5G85_E","051036");
		map.put("粤A2U99X_E","051799");
		map.put("粤A399TG_F","052376");
		map.put("粤A6GW36_F","051623");
		map.put("粤A717KU_E","124401");
		map.put("粤B004MA_F","052011");
		map.put("粤B025NH_F","051759");
		map.put("粤B03Q97_F","018399");
		map.put("粤B071ZL_E","052383");
		map.put("粤B075GW_E","051696");
		map.put("粤B078DM_F","024823");
		map.put("粤B07GA7_F","052163");
		map.put("粤B0B7F0_E","621728");
		map.put("粤B0MG39_E","031362");
		map.put("粤B0PE66_E","050978");
		map.put("粤B0PN91_E","051389");
		map.put("粤B0UA27_F","052146");
		map.put("粤B0V30X_E","051356");
		map.put("粤B112QY_F","051785");
		map.put("粤B123PJ_E","023512");
		map.put("粤B1385P_E","051620");
		map.put("粤B16D19_F","051069");
		map.put("粤B1PX32_F","000115");
		map.put("粤B1Q08K_E","050509");
		map.put("粤B1R1S5_E","051775");
		map.put("粤B1SA98_E","032675");
		map.put("粤B1TM83_E","050722");
		map.put("粤B1X7J6_F","051069");
		map.put("粤B207JV_E","050935");
		map.put("粤B28F06_E","051212");
		map.put("粤B28Z36_F","018399");
		map.put("粤B2D0S6_E","051789");
		map.put("粤B2E30A_E","027868");
		map.put("粤B2W30C_E","052328");
		map.put("粤B307FM_E","104167");
		map.put("粤B35A00_E","000100");
		map.put("粤B36CQ9_E","023722");
		map.put("粤B36J52_F","050123");
		map.put("粤B36U55_E","094652");
		map.put("粤B3A8X8_E","090023");
		map.put("粤B3B2L2_E","050249");
		map.put("粤B3G81T_F","018399");
		map.put("粤B3N38C_E","051397");
		map.put("粤B3Q205_E","cy1319");
		map.put("粤B453ZA_E","M00009");
		map.put("粤B490SB_E","LS26");
		map.put("粤B4G6Q8_E","118508");
		map.put("粤B4M6G0_F","051860");
		map.put("粤B4M9C8_E","020998");
		map.put("粤B513ZW_E","000123");
		map.put("粤B51W26_E","HZ00153");
		map.put("粤B56JW2_E","017660");
		map.put("粤B573AC_F","052139");
		map.put("粤B585AV_F","001633");
		map.put("粤B587FY_E","052244");
		map.put("粤B5981T_F","029834");
		map.put("粤B5AX28_E","026606");
		map.put("粤B5B5G9_E","050967");
		map.put("粤B5D29Q_E","CG0035");
		map.put("粤B5DY20_E","100654");
		map.put("粤B5H12T_F","IDP508");
		map.put("粤B5L58U_F","023233");
		map.put("粤B5SL93_F","014225");
		map.put("粤B659YK_E","051124");
		map.put("粤B65FY7_E","090007");
		map.put("粤B662GT_F","051086");
		map.put("粤B667BH_F","007818");
		map.put("粤B671GG_E","051464");
		map.put("粤B67CT6_F","026150");
		map.put("粤B687EY_E","032943");
		map.put("粤B695DW_F","050333");
		map.put("粤B6B1K2_E","127056");
		map.put("粤B6N9P9_F","DIT1351");
		map.put("粤B6V33R_F","051727");
		map.put("粤B6W410_E","052124");
		map.put("粤B717KF_F","HZ00001");
		map.put("粤B738JC_F","033184");
		map.put("粤B751LF_F","050364");
		map.put("粤B759SU_E","052207");
		map.put("粤B787EK_F","051290");
		map.put("粤B78V52_E","104327");
		map.put("粤B7F28Y_E","104183");
		map.put("粤B7S08B_E","051581");
		map.put("粤B7Y19W_F","052278");
		map.put("粤B82Y66_E","124848");
		map.put("粤B856VE_E","050132");
		map.put("粤B85JS8_E","051173");
		map.put("粤B879C5_E","HZ00111");
		map.put("粤B87BN5_E","097395");
		map.put("粤B887JD_E","HZ00098");
		map.put("粤B8D01C_E","051882");
		map.put("粤B8EP69_E","012303");
		map.put("粤B8KW46_F","IDP002");
		map.put("粤B8Q6G6_F","029059");
		map.put("粤B8V5Z9_E","102513");
		map.put("粤B8X0H8_E","051690");
		map.put("粤B8ZN45_E","HZ00101");
		map.put("粤B955KL_F","IDP044");
		map.put("粤B95NA8_E","052173");
		map.put("粤B9791W_F","000073");
		map.put("粤B9K4Q8_E","021491");
		map.put("粤B9KC56_E","050867");
		map.put("粤B9M4G1_F","051086");
		map.put("粤B9T9P3_E","cg20110315");
		map.put("粤B9WE91_E","093796");
		map.put("粤BA57870_E","019123");
		map.put("粤BA581F_E","011686");
		map.put("粤BA846Y_F","050156");
		map.put("粤BB78F7_F","050027");
		map.put("粤BB95R9_E","103162");
		map.put("粤BB9K30_E","051763");
		map.put("粤BBR458_F","IDP001");
		map.put("粤BC712K_E","050040");
		map.put("粤BCE133_F","051762");
		map.put("粤BD22578_F","050904");
		map.put("粤BD35788_E","052175");
		map.put("粤BD5W11_E","051750");
		map.put("粤BD62591_E","082766");
		map.put("粤BD63029_E","051963");
		map.put("粤BD69550_E","ZS000062");
		map.put("粤BD889R_E","052131");
		map.put("粤BDB4788_F","094008");
		map.put("粤BDF6233_F","052353");
		map.put("粤BE98D9_F","050826");
		map.put("粤BF12425_E","050265");
		map.put("粤BF21876_E","052148");
		map.put("粤BF23747_E","051250");
		map.put("粤BF25272_F","051943");
		map.put("粤BF29627_E","051342");
		map.put("粤BF35978_E","051939");
		map.put("粤BF42979_E","050903");
		map.put("粤BF45851_E","050624");
		map.put("粤BF49622_E","050900");
		map.put("粤BF53856_E","050120");
		map.put("粤BF60Z7_E","012052");
		map.put("粤BF62689_E","007440");
		map.put("粤BF86Z6_E","050794");
		map.put("粤BF94272_E","051542");
		map.put("粤BF95997_E","100723");
		map.put("粤BF98515_E","cy1351");
		map.put("粤BFA0200_E","052243");
		map.put("粤BFB7859_E","IDP165");
		map.put("粤BFB7979_E","051800");
		map.put("粤BFC0167_E","052240");
		map.put("粤BFJ0805_F","052006");
		map.put("粤BH04M7_E","051589");
		map.put("粤BH87U0_E","052357");
		map.put("粤BH97P0_E","051674");
		map.put("粤BJ63L1_F","052309");
		map.put("粤BK125X_E","051778");
		map.put("粤BK26Y2_E","051934");
		map.put("粤BK37N8_E","IDP007");
		map.put("粤BK3D47_E","126417");
		map.put("粤BK469V_E","051229");
		map.put("粤BK625V_E","051932");
		map.put("粤BK68Z9_F","052178");
		map.put("粤BL23Q7_E","009559");
		map.put("粤BL905Y_F","050246");
		map.put("粤BM21A2_E","050740");
		map.put("粤BM5093_E","001446");
		map.put("粤BMN633_F","050836");
		map.put("粤BN454X_F","051850");
		map.put("粤BN4745_E","051103");
		map.put("粤BOP39B_E","000357");
		map.put("粤BQ21V9_E","050575");
		map.put("粤BQ45F1_E","300421");
		map.put("粤BQ8B33_E","018386");
		map.put("粤BQD631_F","051974");
		map.put("粤BR019C_E","052305");
		map.put("粤BR55H0_E","050973");
		map.put("粤BRC371_E","051798");
		map.put("粤BT162W_E","061176");
		map.put("粤BU2907_F","017491");
		map.put("粤BU40E7_E","HZ00132");
		map.put("粤BV29Q6_F","051401");
		map.put("粤BW6A82_F","026120");
		map.put("粤BWIS56_F","023762");
		map.put("粤BWM145_E","028809");
		map.put("粤BXQ686_F","000050");
		map.put("粤BY3X00_F","IDP003");
		map.put("粤BZ24D8_F","007818");
		map.put("粤BZ327B_E","052271");
		map.put("粤BZ562P_F","026237");
		map.put("粤BZ756Q_F","017491");
		map.put("粤BZ82M5_E","IDP057");
		map.put("粤BZ8F95_E","129750");
		map.put("粤BZ8N33_E","051930");
		map.put("粤CMP065_F","052289");
		map.put("粤E8119D_E","130423");
		map.put("粤FA6566_F","052187");
		map.put("粤FQU958_E","000019");
		map.put("粤GN8804_E","080644");
		map.put("粤H19T93_E","052310");
		map.put("粤HU2893_E","051929");
		map.put("粤K2V965_E","052307");
		map.put("粤K5T485_E","029485");
		map.put("粤L007JH_E","051151");
		map.put("粤L1R155_E","006491");
		map.put("粤L28L36_E","050138");
		map.put("粤L2P332_E","100255");
		map.put("粤L36F07_E","050299");
		map.put("粤L44E12_E","014171");
		map.put("粤L7Y557_E","060527");
		map.put("粤L87H08_E","094579");
		map.put("粤L92A33_F","051574");
		map.put("粤L95Y63_E","O50002");
		map.put("粤LR362Q_E","051101");
		map.put("粤LTR590_E","M00003");
		map.put("粤LY723W_F","060636");
		map.put("粤M1H697_E","IT000699");
		map.put("粤MF8311_E","051830");
		map.put("粤MH3546_E","050029");
		map.put("粤MN7496_E","051265");
		map.put("粤N98192_E","050576");
		map.put("粤P91319_E","050339");
		map.put("粤QNX892_E","052125");
		map.put("粤RKT610_E","051303");
		map.put("粤S006TE_E","052344");
		map.put("粤S022W7_E","051244");
		map.put("粤S052W3_E","052216");
		map.put("粤S0W2Q0_E","051171");
		map.put("粤S1FU92_F","Y1094");
		map.put("粤S251AC_F","050195");
		map.put("粤S2PB69_E","100258");
		map.put("粤S2TQ75_F","050427");
		map.put("粤S3C09A_E","050341");
		map.put("粤S402P7_E","050746");
		map.put("粤S4HF60_E","052074");
		map.put("粤S510LF_E","052342");
		map.put("粤S584W2_E","051749");
		map.put("粤S5AG39_E","028936");
		map.put("粤S5EP08_E","052140");
		map.put("粤S6KW78_E","052311");
		map.put("粤S6L36T_E","CQ0324");
		map.put("粤S701TH_F","050984");
		map.put("粤S7DW95_E","051217");
		map.put("粤S8B3K1_E","051931");
		map.put("粤S8H9D7_E","125343");
		map.put("粤S912ZT_E","127665");
		map.put("粤S963ZG_F","052167");
		map.put("粤S9FG98_E","051998");
		map.put("粤S9HX59_E","052316");
		map.put("粤SF3D21_E","500428");
		map.put("粤SG8D87_E","061972");
		map.put("粤SG8P28_E","050850");
		map.put("粤SQ89U3_E","051913");
		map.put("粤SS535X_E","051660");
		map.put("粤SV8J25_E","051276");
		map.put("粤T3532W_E","813922");
		map.put("浙JU7H88_E","052081");
		map.put("粤B722JL_F","Y1101");

		int E = 5000006;
		int F = 5000021;

		List<SmtDeviceAuthorityRelation> authorityRelationsE = smtDeviceAuthorityRelationService.list(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>()
				.eq(SmtDeviceAuthorityRelation::getAuthorityId, E)
		);

		List<SmtDeviceAuthorityRelation> authorityRelationsF = smtDeviceAuthorityRelationService.list(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>()
				.eq(SmtDeviceAuthorityRelation::getAuthorityId, F)
		);


		for(String platStr : map.keySet()){
			String plate = platStr.split("_")[0];
			String levl = platStr.split("_")[1];
			String badge = map.get(platStr);
			addStaffCar(badge,plate,levl,E,F,authorityRelationsE,authorityRelationsF);
		}
	}


	@Transactional
	public void addStaffCar(String badge,String plate,String levl,Integer E,Integer F,List<SmtDeviceAuthorityRelation> authorityRelationsE,List<SmtDeviceAuthorityRelation> authorityRelationsF){
		//获取员工信息
		SmtStaff staff = smtStaffService.getOne(new LambdaQueryWrapper<SmtStaff>().eq(SmtStaff::getBadge, badge));
		if(null == staff){
			//员工信息为null
			log.info("员工不存在:{}",badge);
			return;
		}

		List<SmtVehicle> vehicleList = smtVehicleService.list(new LambdaQueryWrapper<SmtVehicle>().eq(SmtVehicle::getVehiclePlate, plate));

		boolean isDown = false;

		for(SmtVehicle smtVehicle : vehicleList){
			//判断车辆是否下发过
			int count = smtTaskDownRecordService.count(new LambdaQueryWrapper<SmtTaskDownRecord>()
					.eq(SmtTaskDownRecord::getCardNo, smtVehicle.getId()));
			if(count > 0){
				//已下发了设备的车辆 不需要再处理
				isDown = true;
				continue;
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
			}
		}

		if(isDown){
			return;
		}

		SmtVehicle smtVehicle = null;
		if(CollectionUtil.isEmpty(vehicleList)) {
			//添加车辆表
			smtVehicle = new SmtVehicle();
			smtVehicle.setParkId("161");
			smtVehicle.setVehiclePlate(plate);
			smtVehicle.setVehicleBrand("其他");
			smtVehicle.setVehicleColor(4);            //黑色
			smtVehicle.setVehicleType(2);            //小型车
			smtVehicle.setVehicleAscription(1);        //员工车辆
			smtVehicle.setCreateTime(LocalDateTime.now());
			smtVehicle.setIsDelete(0);
			smtVehicleService.save(smtVehicle);
		} else {
			smtVehicle = vehicleList.get(0);
		}

		SmtVehicleStaff vehicleStaff = smtVehicleStaffService.getOne(new LambdaQueryWrapper<SmtVehicleStaff>()
				.eq(SmtVehicleStaff::getStaffId, staff.getId())
				.eq(SmtVehicleStaff::getVehicleId, smtVehicle.getId())
		);
		if(null == vehicleStaff) {
			//添加员工车辆表
			SmtVehicleStaff smtVehicleStaff = new SmtVehicleStaff();
			smtVehicleStaff.setStaffId(staff.getId());
			smtVehicleStaff.setVehicleId(smtVehicle.getId());
			smtVehicleStaffService.save(smtVehicleStaff);
		}

		SmtVehicleApply vehicleApply = smtVehicleApplyService.getOne(new LambdaQueryWrapper<SmtVehicleApply>()
				.eq(SmtVehicleApply::getParkId, 161)
				.eq(SmtVehicleApply::getVehiclePlate, plate)
		);
		if(null == vehicleApply) {
			//添加车辆申请表
			SmtVehicleApply smtVehicleApply = new SmtVehicleApply();
			smtVehicleApply.setVehicleId(smtVehicle.getId());
			smtVehicleApply.setParkId("161");
			smtVehicleApply.setStatus(1);        //直接通过
			smtVehicleApply.setApprover("platform");
			smtVehicleApply.setCreateTime(LocalDateTime.now());
			smtVehicleApply.setVehiclePlate(plate);
			smtVehicleApply.setAuthorityId(E);
			if (levl.equalsIgnoreCase("F")) {
				smtVehicleApply.setAuthorityId(F);
			}
			smtVehicleApplyService.save(smtVehicleApply);
		} else {
			return;
		}

		List<SmtDeviceAuthorityRelation> authorityRelations = authorityRelationsE;
		if (levl.equalsIgnoreCase("F")) {
			authorityRelations = authorityRelationsF;
		}

		//写入设备权限
		for(SmtDeviceAuthorityRelation smtDeviceAuthorityRelation : authorityRelations){
			String sNo = UUID.randomUUID().toString().replaceAll("-", "");
			SmtDeviceTask smtDeviceTask = new SmtDeviceTask();
			smtDeviceTask.setAction(DeviceTaskActionEnum.DOWN.getCode());
			smtDeviceTask.setStatus(0);
			smtDeviceTask.setDeviceType(2);
			smtDeviceTask.setOverTime(33117041415L);				//最大值
			smtDeviceTask.setStartTime(DateUtils.currentSeconds() + (60 * 60));
			smtDeviceTask.setTimes(101);
			smtDeviceTask.setDeviceCode(smtDeviceAuthorityRelation.getDeviceId());
			smtDeviceTask.setCardNo(smtVehicle.getId().toString());
			smtDeviceTask.setGeneral(smtVehicle.getVehiclePlate());
			smtDeviceTask.setCardType(SmtVisitorEnum.CARD_TYPE_1.getType());
			smtDeviceTask.setServiceType(DeviceTaskServiceTypeEnum.CAR_STAFF.getCode());
			smtDeviceTask.setSerialNo(sNo);
			smtDeviceTaskService.save(smtDeviceTask);
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

	//@Test
	public void test3(){
		List<String> badgeList = new ArrayList<>();
		badgeList.add("1069444");
		badgeList.add("1069538");
		badgeList.add("1069448");
		badgeList.add("1069449");
		badgeList.add("1069481");
		badgeList.add("1069482");
		badgeList.add("1069563");
		badgeList.add("1069641");
		badgeList.add("1069584");
		badgeList.add("1069440");
		badgeList.add("1069458");
		badgeList.add("1069637");
		badgeList.add("1069429");
		badgeList.add("1069583");
		badgeList.add("1069475");
		badgeList.add("1069487");
		badgeList.add("1069555");
		badgeList.add("1069560");
		badgeList.add("1069650");
		badgeList.add("1069525");
		badgeList.add("1069442");
		badgeList.add("1069443");
		badgeList.add("1069455");
		badgeList.add("1069582");
		badgeList.add("1069562");
		badgeList.add("1069531");
		badgeList.add("1069591");
		badgeList.add("1069509");
		badgeList.add("1069510");
		badgeList.add("1069578");
		badgeList.add("1069567");
		badgeList.add("1069500");
		badgeList.add("1069537");
		badgeList.add("1069540");
		badgeList.add("1069452");
		badgeList.add("1069515");
		badgeList.add("1069541");
		badgeList.add("1069446");
		badgeList.add("1069445");
		badgeList.add("1069539");
		badgeList.add("1069438");
		badgeList.add("1069471");
		badgeList.add("1069453");
		badgeList.add("1069484");
		badgeList.add("1069533");
		badgeList.add("1069451");
		badgeList.add("1069499");
		badgeList.add("1069593");
		badgeList.add("1069459");
		badgeList.add("1069480");
		badgeList.add("1069522");
		badgeList.add("1069550");
		badgeList.add("1069494");
		badgeList.add("1069549");
		badgeList.add("1069526");
		badgeList.add("1069465");
		badgeList.add("1069545");
		badgeList.add("1069450");
		badgeList.add("1069490");
		badgeList.add("1069544");
		badgeList.add("1069546");
		badgeList.add("1069497");
		badgeList.add("1069483");
		badgeList.add("1069543");
		badgeList.add("1069498");
		badgeList.add("1069463");
		badgeList.add("1069523");
		badgeList.add("1069614");
		badgeList.add("1069547");
		badgeList.add("1069466");
		badgeList.add("1069495");
		badgeList.add("1069514");
		badgeList.add("1069496");
		badgeList.add("1069548");
		badgeList.add("1069462");
		badgeList.add("1069477");
		badgeList.add("1069529");
		badgeList.add("1069476");
		badgeList.add("1069519");
		badgeList.add("1069474");
		badgeList.add("1069590");
		badgeList.add("1069556");
		badgeList.add("1069456");
		badgeList.add("1069436");
		badgeList.add("1069649");
		badgeList.add("1069597");
		badgeList.add("1069517");
		badgeList.add("1069632");
		badgeList.add("1069630");
		badgeList.add("1069625");
		badgeList.add("1069489");
		badgeList.add("1069571");
		badgeList.add("1069572");
		badgeList.add("1069491");
		badgeList.add("1069574");

		for(String badge : badgeList){
			SmtStaff simpleSttaffByBadge = smtStaffService.getSimpleSttaffByBadge(badge);
			if(Objects.isNull(simpleSttaffByBadge) || StringUtils.isEmpty(simpleSttaffByBadge.getCertno())){
				continue;
			}
			List<SmtStaff> smtStaffList = smtStaffService.list(new LambdaQueryWrapper<SmtStaff>()
					.eq(SmtStaff::getCertno, simpleSttaffByBadge.getCertno())
					.ne(SmtStaff::getBadge,badge)
			);

			for(SmtStaff staff : smtStaffList){
				SmtDormitoryStaff smtDormitoryStaff = smtDormitoryStaffService.getOne(new LambdaQueryWrapper<SmtDormitoryStaff>()
						.eq(SmtDormitoryStaff::getStaffId, staff.getId())
				);
				if(Objects.isNull(smtDormitoryStaff)){
					continue;
				}
				//退宿
				smtDormitoryStaffService.checkOutDormitory(smtDormitoryStaff.getId(),null);

				//再入住
				smtDormitoryStaffService.addDormitoryStaff(badge,smtDormitoryStaff.getBedId());
			}
		}
	}

}
