package com.tce.smart.transfer;

import com.tce.smart.transfer.service.DBTableCompareService;
import com.tce.smart.transfer.service.StaffImageTransferService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: com.tce.smart.transfer.Test1
 * @date: 2020/11/12 15:51
 * @author: wuling
 * @version: 1.0
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TransferApplication.class)
@org.junit.Ignore("手工数据迁移脚本：会执行真实员工照片搬运作业且无网络超时（曾挂起构建 40 分钟），禁止自动构建执行")
public class Test1 {

	@Autowired
	private StaffImageTransferService staffImageTransferService;

	@Autowired
	private DBTableCompareService dbTableCompareService;

	@Test
	public void imgTransfer(){
		try {
			staffImageTransferService.transferStaffImg();
		} catch (Exception e){
			e.printStackTrace();
		}

	}

	//@Test
	public void dbTableCompare(){
		dbTableCompareService.tableCompare();
	}

	//@Test
	public void dbDataTransfer(){
		dbTableCompareService.copyData();
	}

	//@Test
	public void testSplit(){
		int sepNum = 10;
		String tempStr = "public static string[] SplitByLen(string str, int separatorCharNum)";
		List<String> strList = new ArrayList<>();
		int iMax = tempStr.length() / sepNum;//获取循环次数
		for (int i = 0; i <= iMax; i++)
		{
			String subStr = tempStr.substring(0, tempStr.length() > sepNum ? sepNum : tempStr.length());
			strList.add(subStr);
			if (tempStr.length() > sepNum)
			{
				tempStr = tempStr.substring(sepNum);
			}
		}

		strList.forEach(item -> {
			System.out.print(item);
		});
	}

	@Test
	public void enableTrigger(){
		dbTableCompareService.enableAllTrigger();
	}
}
