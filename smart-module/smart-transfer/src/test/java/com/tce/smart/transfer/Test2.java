package com.tce.smart.transfer;

import com.tce.smart.transfer.service.DBTableCompareService;
import com.tce.smart.transfer.service.StaffImageTransferService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @program: smart-module
 * @description:
 * @author: Wuling
 * @create: 2021-11-04 15:56
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TransferApplication.class)
@org.junit.Ignore("手工数据迁移脚本：会执行真实数据拷贝作业且异常被吞造成假通过，禁止自动构建执行")
public class Test2 {

	@Autowired
	private StaffImageTransferService staffImageTransferService;

	@Autowired
	private DBTableCompareService dbTableCompareService;

	//@Test
	public void deviceAuthTransfer(){
		try {
		//	staffImageTransferService.addPlatform2DeviceAuth();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	//@Test
	public void deviceDownTransfer(){
		try {
			dbTableCompareService.copyDeviceTaskData();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	//@Test
	public void carDeviceDownTransfer(){
		try {
			dbTableCompareService.copyCarDeviceTaskData();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	//@Test
	public void vehicleStaffTransfer(){
		try {
			dbTableCompareService.copyVehicleStaffData();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	//@Test
	public void vehicleApplyTransfer(){
		try {
			dbTableCompareService.copyVehicleApplyData();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	//@Test
	public void applicationProcessTransfer(){
		try {
			dbTableCompareService.copyApplicationData();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	//@Test
	public void applicationEmailTransfer(){
		try {
			dbTableCompareService.copyApplicationEmailData();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	//@Test
	public void applicationEducationTransfer(){
		try {
			dbTableCompareService.copyApplicationEducationData();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	//@Test
	public void snapPersonTransfer(){
		try {
			dbTableCompareService.copySnapPersonData();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	//@Test
	public void snapVehicleTransfer(){
		try {
			dbTableCompareService.copySnapVehicleData();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	//@Test
	public void visitorTransfer(){
		try {
			dbTableCompareService.copyVisitorData();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	//@Test
	public void downRecordTransfer(){
		//String device = "d9e11bba9a40432bb1494ff96db50050";
		//String device = "b8a989dcc3574a36b2ab9b728b29d4e4";
		//String device = "385488233e8643f1b5fc2766524aab32";
		String device = "34fb7a7216bf464eb982e94f511d5063";
		try {
			dbTableCompareService.downRecordData(device);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	@Test
	public void downVehicleRecordTransfer(){
		try {
			dbTableCompareService.downVehicleRecordData();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	//@Test
	public void dormitoryHisTransfer(){
		try {
			dbTableCompareService.copyDormitoryHisData();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	//@Test
	public void cutImg(){
		try {
			dbTableCompareService.cutImgData();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
