package com.tce.smart.transfer.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.tool.util.ImageUtils;
import com.tce.smart.transfer.dao1.Platform1Dao;
import com.tce.smart.transfer.dao2.Platform2Dao;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @description: DBTableCompareService
 * @date: 2021/3/22 0022 8:40
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
public class DBTableCompareService {

	@Autowired
	private Platform1Dao platform1Dao;

	@Autowired
	private Platform2Dao platform2Dao;

	public void tableCompare(){
		//查询一期platform库 所有的用户表
		List<String> tableNameList = platform1Dao.getUserTablesName();

		StringBuilder stringBuilder = new StringBuilder();

		String separator = System.getProperty("line.separator");

		for(String tabName : tableNameList){
			//查询一期数据库中表的字段
			List<String> tableColumns1 = platform1Dao.getTableColumns(tabName);

			//查询二期数据库中表的字段
			List<String> tableColumns2 = platform2Dao.getTableColumns(tabName);

			for(String colName : tableColumns1){
				if(!tableColumns2.contains(colName)){
					//一期的字段在二期中不存在
					String info = String.format("一期%s表%s字段在二期不存在",tabName,colName);
					log.info(info);
					stringBuilder.append(info).append(separator);
				}
			}

			for(String colName : tableColumns2){
				if(!tableColumns1.contains(colName)){
					//二期的字段在一期中不存在
					String info = String.format("二期%s表%s字段在一期不存在",tabName,colName);
					log.info(info);
					stringBuilder.append(info).append(separator);
				}
			}
		}

		System.out.println(stringBuilder);
	}

	private String getSql(String tabName,int current,int size){
		String sql = "SELECT\n" +
				"\t* \n" +
				"FROM\n" +
				"\t( SELECT tt.*, ROWNUM AS rowno FROM ( SELECT * FROM "+tabName+" ) tt WHERE ROWNUM <= "+(current*size)+" ) table_alias \n" +
				"WHERE\n" +
				"\ttable_alias.rowno >= ("+ (current-1) +"* "+ size +"+1 )";

		return sql;
	}

	private String getPartDataSql(String tabName,int current,int size){
		String sql = "SELECT\n" +
				"\t* \n" +
				"FROM\n" +
				"\t( SELECT tt.*, ROWNUM AS rowno FROM ( SELECT * FROM "+tabName+" where CREATE_TIME>TO_DATE('2021-01-01 00:00:00', 'yyyy-MM-dd HH24:mi:ss') order by CREATE_TIME desc ) tt WHERE ROWNUM <= "+(current*size)+" ) table_alias \n" +
				"WHERE\n" +
				"\ttable_alias.rowno >= ("+ (current-1) +"* "+ size +"+1 )";

		return sql;
	}

	public String getDateStr(Long second){
		DateTime date = DateUtil.date(second * 1000);
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
	}

	// 将字CLOB转成STRING类型
	public String ClobToString(Clob clob){
		if(null == clob){
			return null;
		}

		String reString = "";
		try {
			java.io.Reader is = clob.getCharacterStream();// 得到流
			BufferedReader br = new BufferedReader(is);
			String s = br.readLine();
			StringBuffer sb = new StringBuffer();
			while (s != null) {
				// 执行循环将字符串全部取出付值给StringBuffer由StringBuffer转成STRING
				sb.append(s);
				s = br.readLine();
			}
			reString = sb.toString();
		} catch (Exception e){
			log.error("Clob转String异常",e);
		}

		reString = reString.replaceAll("'","''");

		if(reString.length() < 2000){
			return "'" + reString + "'";
		}

		int sepNum = 2000;
		String tempStr = reString;
		List<String> strList = new ArrayList<>();
		int iMax = reString.length() / sepNum;//获取循环次数
		for (int i = 0; i <= iMax; i++)
		{
			String subStr = tempStr.substring(0, tempStr.length() > sepNum ? sepNum : tempStr.length());
			strList.add(subStr);
			if (tempStr.length() > sepNum)
			{
				tempStr = tempStr.substring(sepNum);
			}
		}

		StringBuilder resSBuilder = new StringBuilder();

		strList.forEach(item -> {
			if(!"".contentEquals(resSBuilder)){
				resSBuilder.append("||");
			}
			resSBuilder.append("to_clob('").append(item).append("')");
		});
		return resSBuilder.toString();
	}

	/**
	 * 处理一期的设备下发已成功的数据
	 * 	处理逻辑为：
	 * 		组织数据，往二期的 SMT_TASK_DOWN_RECORD 表写入数据
	 * 		同时生成一条对应的删除记录
	 */
	public void copyDeviceTaskData(){
		int current = 1;
		int size = 200;
		List<Map<String, Object>> deviceTaskSuccList = platform1Dao.getDeviceTaskSuccList(current, size);

		do {
			if(CollectionUtil.isEmpty(deviceTaskSuccList)){
				break;
			}

			deviceTaskSuccList.forEach(item -> {
				//Integer id = Integer.parseInt(item.get("ID").toString());
				Integer parkId = 26;
				Integer type = Integer.parseInt(item.get("DEVICE_TYPE").toString());
				Integer action = Integer.parseInt(item.get("ACTION").toString());
				String general = item.get("GENERAL").toString();
				String deviceCode = item.get("DEVICE_CODE").toString();

				String badge = general.split("-")[0];

				//通过工号在二期系统查询员工信息
				Map<String, Object> staff = platform2Dao.getStaff(badge);
				if(Objects.isNull(staff)){
					return;
				}

				String cardNo = staff.get("ID").toString();
				JSONObject object = JSONUtil.parseObj(item.get("CONTENT").toString());
				String imageId = object.getStr("faceImageId");
				String startTime = getDateStr(Long.parseLong(item.get("START_TIME").toString()));
				String overTime = getDateStr(Long.parseLong(item.get("OVER_TIME").toString()));

				String createTime = item.get("CREATE_TIME").toString();
				if(createTime.indexOf(".") > -1){
					//去掉毫秒
					createTime = createTime.substring(0,createTime.indexOf("."));
				}

				Integer taskId = Integer.parseInt(item.get("ID").toString());

				String exSql = "select count(*) from SMT_TASK_DOWN_RECORD where CARD_NO='" + cardNo + "' AND DEVICE_CODE='" + deviceCode + "'";
				int count = platform2Dao.selectCount(exSql);
				if(count > 0){
					return;
				}

				String insertSql = "INSERT INTO \"SMT_TASK_DOWN_RECORD\"(\"PARK_ID\", \"DEVICE_TYPE\", \"ACTION\", \"GENERAL\"," +
						" \"DEVICE_CODE\", \"CARD_NO\", \"IMAGE_ID\", \"START_TIME\"," +
						" \"OVER_TIME\", \"CREATE_TIME\", \"TASK_ID\", \"SERVICE_TYPE\", \"TASK_TYPE\", \"REMARK\") \n" +
						"VALUES " +
						"(%d, %d, %d, '%s', '%s', '%s', '%s',TO_DATE('%s', 'yyyy-MM-dd HH24:mi:ss'), \n" +
						"TO_DATE('%s', 'yyyy-MM-dd HH24:mi:ss'), TO_DATE('%s', 'yyyy-MM-dd HH24:mi:ss'), %d, '77', '77', NULL)";

				insertSql = String.format(insertSql,parkId,type,action,general,deviceCode,cardNo,imageId,
						startTime,overTime,createTime,taskId);

				platform2Dao.insertRecord(insertSql);

				//添加一条删除任务

//				String insertTaskSql = "INSERT INTO \"SMT_DEVICE_TASK\"(\"ID\",\"ACTION\", \"STATUS\", \"DEVICE_TYPE\", \"OVER_TIME\", " +
//						"\"CREATE_TIME\", \"START_TIME\", \"DEVICE_CODE\", \"CARD_NO\", \"REMARK\", \"CODE\", \n" +
//						"\"CONSUME\", \"TIMES\", \"UPDATE_TIME\", \"GENERAL\", \"CARD_TYPE\", \"IMAGE_ID\", \"SERVICE_TYPE\"," +
//						" \"SERIAL_NO\") \n" +
//						"VALUES (%d,'2', '0', %d, %d, TO_DATE('%s', 'SYYYY-MM-DD HH24:MI:SS'), %d, '%s', '%s', \n" +
//						"null, null, null, null, null, '%s', '77', \n" +
//						"'%s', '77', '%s')";
//				long overTimeSecord = Long.parseLong(item.get("OVER_TIME").toString());
//				long startTimeSecord = Long.parseLong(item.get("START_TIME").toString());
//				String serialNo = UUID.randomUUID().toString().replaceAll("-", "");
//				insertTaskSql = String.format(insertTaskSql,id,type,overTimeSecord,createTime,startTimeSecord,
//						deviceCode,cardNo,general,imageId,serialNo);
//
//				platform2Dao.insertRecord(insertTaskSql);

			});
			current++;
			deviceTaskSuccList = platform1Dao.getDeviceTaskSuccList(current, size);
		} while (true);

	}

	public void copyCarDeviceTaskData(){
		int current = 1;
		int size = 200;
		List<Map<String, Object>> deviceTaskSuccList = platform1Dao.getCarDeviceTaskSuccList(current, size);

		do {
			if(CollectionUtil.isEmpty(deviceTaskSuccList)){
				break;
			}

			deviceTaskSuccList.forEach(item -> {
				Integer parkId = 26;
				Integer type = Integer.parseInt(item.get("DEVICE_TYPE").toString());
				Integer action = Integer.parseInt(item.get("ACTION").toString());
				String general = item.get("GENERAL").toString();
				String deviceCode = item.get("DEVICE_CODE").toString();

				String cardNo = item.get("CARD_NO").toString();
				String startTime = getDateStr(Long.parseLong(item.get("START_TIME").toString()));
				String overTime = getDateStr(Long.parseLong(item.get("OVER_TIME").toString()));

				String createTime = item.get("CREATE_TIME").toString();
				if(createTime.indexOf(".") > -1){
					//去掉毫秒
					createTime = createTime.substring(0,createTime.indexOf("."));
				}

				Integer taskId = Integer.parseInt(item.get("ID").toString());

				String exSql = "select count(*) from SMT_TASK_DOWN_RECORD where CARD_NO='" + cardNo + "' AND DEVICE_CODE='" + deviceCode + "'";
				int count = platform2Dao.selectCount(exSql);
				if(count > 0){
					return;
				}

				String insertSql = "INSERT INTO \"SMT_TASK_DOWN_RECORD\"(\"PARK_ID\", \"DEVICE_TYPE\", \"ACTION\", \"GENERAL\"," +
						" \"DEVICE_CODE\", \"CARD_NO\", \"START_TIME\"," +
						" \"OVER_TIME\", \"CREATE_TIME\", \"TASK_ID\", \"SERVICE_TYPE\", \"TASK_TYPE\") \n" +
						"VALUES " +
						"(%d, %d, %d, '%s', '%s', '%s', TO_DATE('%s', 'yyyy-MM-dd HH24:mi:ss'), \n" +
						"TO_DATE('%s', 'yyyy-MM-dd HH24:mi:ss'), TO_DATE('%s', 'yyyy-MM-dd HH24:mi:ss'), %d, '77', '77')";

				insertSql = String.format(insertSql,parkId,type,action,general,deviceCode,cardNo,
						startTime,overTime,createTime,taskId);

				platform2Dao.insertRecord(insertSql);

			});
			current++;
			deviceTaskSuccList = platform1Dao.getCarDeviceTaskSuccList(current, size);
		} while (true);

	}

	public void copyVehicleStaffData(){
		int current = 1;
		int size = 200;
		List<Map<String, Object>> deviceTaskSuccList = platform1Dao.getVehicleStaffList(current, size);

		do {
			if(CollectionUtil.isEmpty(deviceTaskSuccList)){
				break;
			}

			deviceTaskSuccList.forEach(item -> {
				if(Objects.isNull(item.get("VEHICLE_ID")) || Objects.isNull(item.get("STAFF_ID"))){
					return;
				}
				Long vehicleId = Long.parseLong(item.get("VEHICLE_ID").toString());
				Long staffId = Long.parseLong(item.get("STAFF_ID").toString());

				Map<String, Object> staffMap = platform1Dao.getStaffInfoById(staffId);

				if(Objects.isNull(staffMap)){
					return;
				}

				String badge = staffMap.get("BADGE").toString();

				//通过工号在二期系统查询员工信息
				Map<String, Object> staff = platform2Dao.getStaff(badge);
				if(Objects.isNull(staff)){
					return;
				}

				Long staffId2 = Long.parseLong(staff.get("ID").toString());

				String exSql = "select count(*) from SMT_VEHICLE_STAFF where VEHICLE_ID=" + vehicleId + " AND STAFF_ID=" + staffId2;
				int count = platform2Dao.selectCount(exSql);
				if(count > 0){
					return;
				}

				String insertSql = "INSERT INTO \"SMT_VEHICLE_STAFF\"(\"VEHICLE_ID\", \"STAFF_ID\") \n" +
						"VALUES " +
						"(%d, %d)";

				insertSql = String.format(insertSql,vehicleId,staffId2);

				platform2Dao.insertRecord(insertSql);

			});
			current++;
			deviceTaskSuccList = platform1Dao.getVehicleStaffList(current, size);
		} while (true);
	}

	public void copyVehicleApplyData(){
		int current = 1;
		int size = 200;
		List<Map<String, Object>> deviceTaskSuccList = platform1Dao.getVehicleApplyList(current, size);

		do {
			if(CollectionUtil.isEmpty(deviceTaskSuccList)){
				break;
			}

			deviceTaskSuccList.forEach(item -> {
				Integer parkId = 26;
				Long vehicleId = Long.parseLong(item.get("VEHICLE_ID").toString());
				String vehiclePlate = item.get("VEHICLE_PLATE").toString();
				String createTime = item.get("CREATE_TIME").toString();
				if(createTime.indexOf(".") > -1){
					//去掉毫秒
					createTime = createTime.substring(0,createTime.indexOf("."));
				}

				Long staffId = Long.parseLong(item.get("STAFF_ID").toString());

				Map<String, Object> staffMap = platform1Dao.getStaffInfoById(staffId);

				if(Objects.isNull(staffMap)){
					return;
				}

				String badge = staffMap.get("BADGE").toString();

				//通过工号在二期系统查询员工信息
				Map<String, Object> staff = platform2Dao.getStaff(badge);
				if(Objects.isNull(staff)){
					return;
				}

				Long staffId2 = Long.parseLong(staff.get("ID").toString());

				String exSql = "select count(*) from SMT_VEHICLE_STAFF where VEHICLE_ID=" + vehicleId + " AND STAFF_ID=" + staffId2;
				int count = platform2Dao.selectCount(exSql);
				if(count > 0){
					return;
				}

				String insertSql = "INSERT INTO \"SMT_VEHICLE_STAFF\"(\"VEHICLE_ID\", \"STAFF_ID\") \n" +
						"VALUES " +
						"(%d, %d)";

				insertSql = String.format(insertSql,vehicleId,staffId2);

				platform2Dao.insertRecord(insertSql);

			});
			current++;
			deviceTaskSuccList = platform1Dao.getDeviceTaskSuccList(current, size);
		} while (true);
	}

	public void copyApplicationData(){
		int current = 1;
		int size = 200;
		List<Map<String, Object>> mapList = platform1Dao.getApplicationList(current, size);

		do {
			if(CollectionUtil.isEmpty(mapList)){
				break;
			}

			mapList.forEach(item -> {
				Integer parkId = 26;
				Integer id = Integer.parseInt(item.get("ID").toString());
				Integer status = Integer.parseInt(item.get("STATUS").toString());
				String cuName = "";
				if(Objects.nonNull(item.get("CREATE_USER_NAME"))){
					cuName = item.get("CREATE_USER_NAME").toString();
				}
				String createTime = item.get("CREATE_TIME").toString();
				Long appId = Long.parseLong(item.get("APPLICATION_ID").toString());
				String remark = null;
				if(Objects.nonNull(item.get("REMARK"))){
					remark = item.get("REMARK").toString();
				}
				if(createTime.indexOf(".") > -1){
					//去掉毫秒
					createTime = createTime.substring(0,createTime.indexOf("."));
				}

				String exSql = "select count(*) from SMT_APPLICATION_PROCESS where ID=" + id;
				int count = platform2Dao.selectCount(exSql);
				if(count > 0){
					return;
				}

				String insertSql = "INSERT INTO SMT_APPLICATION_PROCESS(\"ID\", \"STATUS\", \"CREATE_USER_NAME\", \"PARK_ID\", \"APPLICATION_ID\", \"CREATE_TIME\") " +
						"VALUES (%d, %d, '%s', %d, %d, TO_DATE('%s', 'SYYYY-MM-DD HH24:MI:SS'))";

				if(Objects.nonNull(remark)){
					insertSql = "INSERT INTO SMT_APPLICATION_PROCESS(\"ID\", \"STATUS\", \"CREATE_USER_NAME\", \"PARK_ID\", \"APPLICATION_ID\", \"CREATE_TIME\", \"REMARK\") " +
							"VALUES (%d, %d, '%s', %d, %d, TO_DATE('%s', 'SYYYY-MM-DD HH24:MI:SS'), '%s')";
				}

				insertSql = String.format(insertSql,id,status,cuName,parkId,appId,createTime,remark);

				platform2Dao.insertRecord(insertSql);

			});
			current++;
			mapList = platform1Dao.getApplicationList(current, size);
		} while (true);
	}

	public void copyApplicationEmailData(){
		int current = 1;
		int size = 200;
		List<Map<String, Object>> mapList = platform1Dao.getApplicationEmailList(current, size);

		do {
			if(CollectionUtil.isEmpty(mapList)){
				break;
			}

			mapList.forEach(item -> {
				Integer id = Integer.parseInt(item.get("ID").toString());
				Long appId = Long.parseLong(item.get("APPLICATION_ID").toString());
				String email = null;
				if(Objects.nonNull(item.get("EMAIL"))){
					email = item.get("EMAIL").toString();
				}

				String exSql = "select count(*) from SMT_APPLICATION_EMAIL where ID=" + id;
				int count = platform2Dao.selectCount(exSql);
				if(count > 0){
					return;
				}

				String insertSql = "INSERT INTO SMT_APPLICATION_EMAIL(\"ID\", \"APPLICATION_ID\") " +
						"VALUES (%d, %d)";

				if(Objects.nonNull(email)){
					insertSql = "INSERT INTO SMT_APPLICATION_EMAIL(\"ID\", \"APPLICATION_ID\",\"EMAIL\") " +
							"VALUES (%d, %d, '%s')";
				}

				insertSql = String.format(insertSql,id,appId,email);

				platform2Dao.insertRecord(insertSql);

			});
			current++;
			mapList = platform1Dao.getApplicationEmailList(current, size);
		} while (true);
	}

	public void copyApplicationEducationData(){
		int current = 1;
		int size = 200;
		List<Map<String, Object>> mapList = platform1Dao.getApplicationEducationList(current, size);

		do {
			if(CollectionUtil.isEmpty(mapList)){
				break;
			}

			mapList.forEach(item -> {

				List<String> columnList = new ArrayList<>();

				List<String> valueList = new ArrayList<>();

				for(String key: item.keySet()){
					if("ROWNO".equals(key)){
						continue;
					}
					if(Objects.nonNull(item.get(key))){
						columnList.add(key);
						valueList.add(item.get(key).toString());
					}
				}

				StringBuilder stringBuilder = new StringBuilder();

				stringBuilder.append("INSERT INTO SMT_APPLICATION_EDUCATION(");

				for(int i = 0;i<columnList.size();i++){
					if(i == columnList.size() - 1){
						stringBuilder.append("\"").append(columnList.get(i)).append("\"");
					} else {
						stringBuilder.append("\"").append(columnList.get(i)).append("\",");
					}
				}
				stringBuilder.append(") VALUES (");

				for(int i = 0;i<valueList.size();i++){
					if(i == valueList.size() - 1){
						stringBuilder.append("'").append(valueList.get(i)).append("'");
					} else {
						stringBuilder.append("'").append(valueList.get(i)).append("',");
					}
				}

				stringBuilder.append(")");

				Integer id = Integer.parseInt(item.get("ID").toString());

				String exSql = "select count(*) from SMT_APPLICATION_EDUCATION where ID=" + id;
				int count = platform2Dao.selectCount(exSql);
				if(count > 0){
					return;
				}

				String insertSql = stringBuilder.toString();

				platform2Dao.insertRecord(insertSql);

			});
			current++;
			mapList = platform1Dao.getApplicationEducationList(current, size);
		} while (true);
	}

	public void copySnapPersonData(){
		int current = 1;
		int size = 200;
		List<Map<String, Object>> mapList = platform1Dao.getSnapPersonList(current, size);

		do {
			if(CollectionUtil.isEmpty(mapList)){
				break;
			}

			mapList.forEach(item -> {

				List<String> columnList = new ArrayList<>();

				List<String> valueList = new ArrayList<>();

				for(String key: item.keySet()){
					if("ROWNO".equals(key)){
						continue;
					}

					if("PARK_ID".equals(key)){
						columnList.add(key);
						valueList.add(String.valueOf(26));
					}

					if(Objects.nonNull(item.get(key))){
						String val = item.get(key).toString();

						if("SNAP_TIME".equalsIgnoreCase(key) || "CREATE_TIME".equalsIgnoreCase(key)){
							if(val.indexOf(".") > -1){
								//去掉毫秒
								val = val.substring(0,val.indexOf("."));

								val = "TO_DATE('"+val+"', 'yyyy-MM-dd HH24:mi:ss')";
							}
						}

						if("PERSON_PHONE".equalsIgnoreCase(key) && val.indexOf("—") != -1){
							val = val.split("—")[0];
						}

						columnList.add(key);
						valueList.add(val);
					}
				}

				Integer id = Integer.parseInt(item.get("ID").toString());

				String exSql = "select count(*) from SMT_SNAP_PERSON where ID=" + id;
				int count = platform2Dao.selectCount(exSql);
				if(count > 0){
					return;
				}

				//查询人员ID是否为员工表主键或访客表主键
				Long personId = Long.parseLong(item.get("PERSON_ID").toString());
				Map<String,Object> staffMap = platform1Dao.getStaffInfoById(personId);
				if(Objects.isNull(staffMap)){
					return;
				}

				//根据员工工号查询二期员工信息
				Map<String, Object> dao2Staff = platform2Dao.getStaffInclude0(staffMap.get("BADGE").toString());
				if(Objects.isNull(dao2Staff) || Objects.isNull(dao2Staff.get("STATUS"))){
					return;
				}

				if(Integer.parseInt(dao2Staff.get("STATUS").toString()) == 0){
					//员工离职状态 根据身份证号查询在职的记录
					dao2Staff = platform2Dao.getStaffByCertno(staffMap.get("CERTNO").toString());
					if(Objects.isNull(dao2Staff)){
						return;
					}
				}

				long staffId2 = Long.parseLong(dao2Staff.get("ID").toString());

				//替换通行记录的PERSON_ID信息
				for(int i = 0;i<columnList.size();i++){
					if("PERSON_ID".equals(columnList.get(i))){
						valueList.set(i,String.valueOf(staffId2));
						break;
					}
				}

				StringBuilder stringBuilder = new StringBuilder();

				stringBuilder.append("INSERT INTO SMT_SNAP_PERSON(");

				for(int i = 0;i<columnList.size();i++){
					if(i == columnList.size() - 1){
						stringBuilder.append("\"").append(columnList.get(i)).append("\"");
					} else {
						stringBuilder.append("\"").append(columnList.get(i)).append("\",");
					}
				}
				stringBuilder.append(") VALUES (");

				for(int i = 0;i<valueList.size();i++){
					if(!valueList.get(i).startsWith("TO_DATE(")){
						stringBuilder.append("'");
					}
					stringBuilder.append(valueList.get(i));
					if(!valueList.get(i).startsWith("TO_DATE(")){
						stringBuilder.append("'");
					}

					if(i != valueList.size() - 1){
						stringBuilder.append(",");
					}
				}

				stringBuilder.append(")");

				String insertSql = stringBuilder.toString();
				platform2Dao.insertRecord(insertSql);

			});
			current++;
			mapList = platform1Dao.getSnapPersonList(current, size);
		} while (true);
	}

	public void copySnapVehicleData(){
		int current = 1;
		int size = 200;
		List<Map<String, Object>> mapList = platform1Dao.getSnapVehicleList(current, size);

		do {
			if(CollectionUtil.isEmpty(mapList)){
				break;
			}

			mapList.forEach(item -> {

				List<String> columnList = new ArrayList<>();

				List<String> valueList = new ArrayList<>();

				for(String key: item.keySet()){
					if("ROWNO".equals(key)){
						continue;
					}

					if("PARK_ID".equals(key)){
						columnList.add(key);
						valueList.add(String.valueOf(26));
					} else {
						if(Objects.nonNull(item.get(key))){
							String val = item.get(key).toString();

							if("SNAP_TIME".equalsIgnoreCase(key) || "CREATE_TIME".equalsIgnoreCase(key)){
								if(val.indexOf(".") > -1){
									//去掉毫秒
									val = val.substring(0,val.indexOf("."));

									val = "TO_DATE('"+val+"', 'yyyy-MM-dd HH24:mi:ss')";
								}
							}

							columnList.add(key);
							valueList.add(val);
						}
					}
				}

				Integer id = Integer.parseInt(item.get("ID").toString());

				String exSql = "select count(*) from SMT_SNAP_VEHICLE where ID=" + id;
				int count = platform2Dao.selectCount(exSql);
				if(count > 0){
					return;
				}

				//查询人员ID是否为员工表主键或访客表主键
				Long personId = Long.parseLong(item.get("DRIVER_ID").toString());
				Map<String,Object> staffMap = platform1Dao.getStaffInfoById(personId);
				if(Objects.isNull(staffMap)){
					return;
				}

				//根据员工工号查询二期员工信息
				Map<String, Object> dao2Staff = platform2Dao.getStaffInclude0(staffMap.get("BADGE").toString());
				if(Objects.isNull(dao2Staff) || Objects.isNull(dao2Staff.get("STATUS"))){
					return;
				}

				if(Integer.parseInt(dao2Staff.get("STATUS").toString()) == 0){
					//员工离职状态 根据身份证号查询在职的记录
					dao2Staff = platform2Dao.getStaffByCertno(staffMap.get("CERTNO").toString());
					if(Objects.isNull(dao2Staff)){
						return;
					}
				}

				long staffId2 = Long.parseLong(dao2Staff.get("ID").toString());

				//替换通行记录的PERSON_ID信息
				for(int i = 0;i<columnList.size();i++){
					if("DRIVER_ID".equals(columnList.get(i))){
						valueList.set(i,String.valueOf(staffId2));
						break;
					}
				}

				StringBuilder stringBuilder = new StringBuilder();

				stringBuilder.append("INSERT INTO SMT_SNAP_VEHICLE(");

				for(int i = 0;i<columnList.size();i++){
					if(i == columnList.size() - 1){
						stringBuilder.append("\"").append(columnList.get(i)).append("\"");
					} else {
						stringBuilder.append("\"").append(columnList.get(i)).append("\",");
					}
				}
				stringBuilder.append(") VALUES (");

				for(int i = 0;i<valueList.size();i++){
					if(!valueList.get(i).startsWith("TO_DATE(")){
						stringBuilder.append("'");
					}
					stringBuilder.append(valueList.get(i));
					if(!valueList.get(i).startsWith("TO_DATE(")){
						stringBuilder.append("'");
					}

					if(i != valueList.size() - 1){
						stringBuilder.append(",");
					}
				}

				stringBuilder.append(")");

				String insertSql = stringBuilder.toString();
				platform2Dao.insertRecord(insertSql);

			});
			current++;
			mapList = platform1Dao.getSnapVehicleList(current, size);
		} while (true);
	}

	public void copyVisitorData(){
		int current = 1;
		int size = 200;
		List<Map<String, Object>> mapList = platform1Dao.getVisitorList(current, size);

		do {
			if(CollectionUtil.isEmpty(mapList)){
				break;
			}

			mapList.forEach(item -> {

				List<String> columnList = new ArrayList<>();

				List<String> valueList = new ArrayList<>();

				for(String key: item.keySet()){
					if("ROWNO".equals(key)){
						continue;
					}

					if("PARK_ID".equals(key)){
						columnList.add(key);
						valueList.add(String.valueOf(26));
					} else {
						if(Objects.nonNull(item.get(key))){
							String val = item.get(key).toString();

							if("START_TIME".equalsIgnoreCase(key) || "END_TIME".equalsIgnoreCase(key) || "CREATE_TIME".equalsIgnoreCase(key)){
								if(val.indexOf(".") > -1){
									//去掉毫秒
									val = val.substring(0,val.indexOf("."));

									val = "TO_DATE('"+val+"', 'yyyy-MM-dd HH24:mi:ss')";
								}
							}

							columnList.add(key);
							valueList.add(val);
						}
					}
				}

				Long id = Long.parseLong(item.get("ID").toString());

				String exSql = "select count(*) from SMT_VISITOR where ID=" + id;
				int count = platform2Dao.selectCount(exSql);
				if(count > 0){
					return;
				}

				//查询访客随时人员记录
				String fellowVisitorSql = "select * from SMT_FELLOW_VISITOR where VISITOR_ID=" + id;
				List<Map<String, Object>> objectMap = platform1Dao.execRecord(fellowVisitorSql);
				if(CollectionUtils.isNotEmpty(objectMap)){
					for(Map<String, Object> map : objectMap){
						//查询访客随时人员记录是否已添加到二期
						String id1 = map.get("ID").toString();
						String fellowVisitorSql2 = "select * from SMT_FELLOW_VISITOR where ID=" + id1;
						Map<String, Object> objectMap2 = platform2Dao.execOneRecord(fellowVisitorSql2);
						if(Objects.isNull(objectMap2)){
							//添加访客随性人员记录到二期
                            String fellowVisInsSql = "INSERT INTO SMT_FELLOW_VISITOR(ID,FELLOW_NAME,FELLOW_PHOTO_ID,VISITOR_ID) VALUES (" +
                                    "'" + map.get("ID").toString() + "'," +
                                    "'" + map.get("FELLOW_NAME").toString() + "'," +
                                    "'" + map.get("FELLOW_PHOTO_ID").toString() + "'," +
                                    "'" + map.get("VISITOR_ID").toString() + "'" +
                                    ")";
							platform2Dao.insertRecord(fellowVisInsSql);
						}
					}
				}

				//查询访客审批处理记录
				String visitorProcessSql = "select * from SMT_VISITOR_PROCESS_RECORD where VISITOR_ID=" + id;
				List<Map<String, Object>> objectMapPro = platform1Dao.execRecord(visitorProcessSql);
				if(CollectionUtils.isNotEmpty(objectMapPro)){
					Map<String, Object> objectMap1 = objectMapPro.get(0);
					//查询访客审批处理记录是否已添加到二期
					Map<String, Object> objectMapPro2 = platform2Dao.execOneRecord(visitorProcessSql);
					if(Objects.isNull(objectMapPro2)){
						//添加访客审批处理记录到二期
						StringBuilder visProInsSql = new StringBuilder();
						visProInsSql.append("INSERT INTO SMT_VISITOR_PROCESS_RECORD(ID,VISITOR_ID,STAFF_BADGE,STAFF_NAME,STATUS,RECORD_DATE,CREATE_DATE,RECORD_NODE,STATUS_NAME,STAFF_JCHE) VALUES (");

						String recordDate = objectMap1.get("RECORD_DATE").toString();
						if(recordDate.indexOf(".") > -1){
							//去掉毫秒
							recordDate = recordDate.substring(0,recordDate.indexOf("."));
						}

						String createDate = objectMap1.get("CREATE_DATE").toString();
						if(createDate.indexOf(".") > -1){
							//去掉毫秒
							createDate = createDate.substring(0,createDate.indexOf("."));
						}

						visProInsSql.append("'").append(objectMap1.get("ID").toString()).append("',")
								.append("'").append(objectMap1.get("VISITOR_ID").toString()).append("',")
								.append("'").append(objectMap1.get("STAFF_BADGE").toString()).append("',")
								.append("'").append(objectMap1.get("STAFF_NAME").toString()).append("',")
								.append("'").append(objectMap1.get("STATUS").toString()).append("',")
								.append("TO_DATE('").append(recordDate).append("', 'yyyy-MM-dd HH24:mi:ss'),")
								.append("TO_DATE('").append(createDate).append("', 'yyyy-MM-dd HH24:mi:ss'),")
								.append("'").append(objectMap1.get("RECORD_NODE").toString()).append("',")
								.append("'").append(objectMap1.get("STATUS_NAME").toString()).append("',")
								.append("'").append(objectMap1.get("STAFF_JCHE").toString()).append("'");
						visProInsSql.append(")");
						platform2Dao.insertRecord(visProInsSql.toString());
					}
				}


				StringBuilder stringBuilder = new StringBuilder();

				stringBuilder.append("INSERT INTO SMT_VISITOR(");

				for(int i = 0;i<columnList.size();i++){
					if(i == columnList.size() - 1){
						stringBuilder.append("\"").append(columnList.get(i)).append("\"");
					} else {
						stringBuilder.append("\"").append(columnList.get(i)).append("\",");
					}
				}
				stringBuilder.append(") VALUES (");

				for(int i = 0;i<valueList.size();i++){
					if(!valueList.get(i).startsWith("TO_DATE(")){
						stringBuilder.append("'");
					}
					stringBuilder.append(valueList.get(i));
					if(!valueList.get(i).startsWith("TO_DATE(")){
						stringBuilder.append("'");
					}

					if(i != valueList.size() - 1){
						stringBuilder.append(",");
					}
				}

				stringBuilder.append(")");

				String insertSql = stringBuilder.toString();
				platform2Dao.insertRecord(insertSql);

			});
			current++;
			mapList = platform1Dao.getVisitorList(current, size);
		} while (true);
	}

	public void downRecordData(String device){
		int current = 1;
		int size = 200;
		List<Map<String, Object>> mapList = platform2Dao.getTaskRecordList(current, size,device);

		do {
			if(CollectionUtil.isEmpty(mapList)){
				break;
			}
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			mapList.forEach(item -> {

				try {
					String over_time = item.get("OVER_TIME").toString();
					if(over_time.indexOf(".") > -1){
						//去掉毫秒
						over_time = over_time.substring(0,over_time.indexOf("."));
					}
					Date endDate = simpleDateFormat.parse(over_time);
					long overTime = endDate.getTime() / 1000;

					String start_time = item.get("START_TIME").toString();
					if(start_time.indexOf(".") > -1){
						//去掉毫秒
						start_time = start_time.substring(0,start_time.indexOf("."));
					}
					Date startDate = simpleDateFormat.parse(start_time);
					long startTime = startDate.getTime() / 1000;

					String createTime = item.get("CREATE_TIME").toString();
					if(createTime.indexOf(".") > -1){
						//去掉毫秒
						createTime = createTime.substring(0,createTime.indexOf("."));
					}

					String deviceCode = item.get("DEVICE_CODE").toString();

					String cardNo1 = item.get("CARD_NO").toString();

					Long staffId = Long.parseLong(cardNo1);

					String cardNo2 = cardNo1;

					//查询一期的员工表数据
					Map<String, Object> staffInfoById = platform1Dao.getStaffInfoById(staffId);

					if(Objects.nonNull(staffInfoById)){
						String certno = staffInfoById.get("CERTNO").toString();

						//通过身份证查询二期员工数据
						Map<String, Object> staffByCertno = platform2Dao.getStaffByCertno(certno);

						if(Objects.isNull(staffByCertno)){
							return;
						}
						cardNo2 = staffByCertno.get("ID").toString();
					}

					String general = item.get("GENERAL").toString();

					String card_type = item.get("TASK_TYPE").toString();

					String image_id = item.get("IMAGE_ID").toString();

					String service_type = item.get("SERVICE_TYPE").toString();

					String serialNo = UUID.randomUUID().toString().replaceAll("-", "");

					String action = "123";

					String exSql = "select count(*) from SMT_DEVICE_TASK where CARD_NO='" + cardNo2 + "' AND DEVICE_CODE='" + deviceCode + "' AND ACTION=" + action;
					int count = platform2Dao.selectCount(exSql);
					if(count > 0){
						return;
					}

					String sql = "INSERT INTO SMT_DEVICE_TASK(\"ACTION\", \"STATUS\", \"DEVICE_TYPE\", \"OVER_TIME\", \"CREATE_TIME\"," +
							" \"START_TIME\", \"DEVICE_CODE\", \"CARD_NO\", \"CODE\"," +
							"\"GENERAL\", \"CARD_TYPE\", \"IMAGE_ID\", \"SERVICE_TYPE\", \"SERIAL_NO\") " +
							"VALUES ('%s', 0, '1', '%s', TO_DATE('%s', 'SYYYY-MM-DD HH24:MI:SS'), '%s', '%s', '%s', '0',  '%s', '%s', '%s', '%s', '%s')";


					String insertSql = String.format(sql,action,overTime,createTime,startTime,deviceCode,cardNo2,general,
							card_type,image_id,service_type,serialNo);

					platform2Dao.insertRecord(insertSql);
				} catch (Exception e){
					e.printStackTrace();
				}

			});
			current++;
			mapList = platform2Dao.getTaskRecordList(current, size,device);
		} while (true);
	}

	public void downVehicleRecordData(){
		int current = 1;
		int size = 200;
		List<Map<String, Object>> mapList = platform2Dao.getVehicleTaskRecordList(current, size);

		do {
			if(CollectionUtil.isEmpty(mapList)){
				break;
			}
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			mapList.forEach(item -> {

				try {

					String general = item.get("GENERAL").toString();
					String exSql = "select count(*) from SMT_VEHICLE where VEHICLE_PLATE='" + general + "' and PARK_ID=26 and IS_DELETE=0";
					int count = platform2Dao.selectCount(exSql);
					if(count <= 0){
						return;
					}


					String over_time = item.get("OVER_TIME").toString();
					if(over_time.indexOf(".") > -1){
						//去掉毫秒
						over_time = over_time.substring(0,over_time.indexOf("."));
					}
					Date endDate = simpleDateFormat.parse(over_time);
					long overTime = endDate.getTime() / 1000;

					String start_time = item.get("START_TIME").toString();
					if(start_time.indexOf(".") > -1){
						//去掉毫秒
						start_time = start_time.substring(0,start_time.indexOf("."));
					}
					Date startDate = simpleDateFormat.parse(start_time);
					long startTime = startDate.getTime() / 1000;

					String createTime = item.get("CREATE_TIME").toString();
					if(createTime.indexOf(".") > -1){
						//去掉毫秒
						createTime = createTime.substring(0,createTime.indexOf("."));
					}

					String deviceCode = item.get("DEVICE_CODE").toString();

					String cardNo1 = item.get("CARD_NO").toString();

					String cardNo2 = cardNo1;

					String card_type = item.get("TASK_TYPE").toString();

					String service_type = item.get("SERVICE_TYPE").toString();

					String serialNo = UUID.randomUUID().toString().replaceAll("-", "");

					String action = "223";

					String exSql2 = "select count(*) from SMT_DEVICE_TASK where CARD_NO='" + cardNo2 + "' AND DEVICE_CODE='" + deviceCode + "' AND ACTION=" + action;
					int count2 = platform2Dao.selectCount(exSql2);
					if(count2 > 0){
						return;
					}

					String sql = "INSERT INTO SMT_DEVICE_TASK(\"ACTION\", \"STATUS\", \"DEVICE_TYPE\", \"OVER_TIME\", \"CREATE_TIME\"," +
							" \"START_TIME\", \"DEVICE_CODE\", \"CARD_NO\", \"CODE\"," +
							"\"GENERAL\", \"CARD_TYPE\", \"SERVICE_TYPE\", \"SERIAL_NO\") " +
							"VALUES ('%s', 0, '2', '%s', TO_DATE('%s', 'SYYYY-MM-DD HH24:MI:SS'), '%s', '%s', '%s', '0',  '%s', '%s', '%s', '%s')";


					String insertSql = String.format(sql,action,overTime,createTime,startTime,deviceCode,cardNo2,general,
							card_type,service_type,serialNo);

					platform2Dao.insertRecord(insertSql);
				} catch (Exception e){
					e.printStackTrace();
				}

			});
			current++;
			mapList = platform2Dao.getVehicleTaskRecordList(current, size);
		} while (true);
	}

	public void copyDormitoryHisData(){
		int current = 1;
		int size = 200;
		List<Map<String, Object>> mapList = platform1Dao.getSnapVehicleList(current, size);

		do {
			if(CollectionUtil.isEmpty(mapList)){
				break;
			}

			mapList.forEach(item -> {

				List<String> columnList = new ArrayList<>();

				List<String> valueList = new ArrayList<>();

				for(String key: item.keySet()){
					if("ROWNO".equals(key)){
						continue;
					}

					if("PARK_ID".equals(key)){
						columnList.add(key);
						valueList.add(String.valueOf(26));
					} else {
						if(Objects.nonNull(item.get(key))){
							String val = item.get(key).toString();

							if("SNAP_TIME".equalsIgnoreCase(key) || "CREATE_TIME".equalsIgnoreCase(key)){
								if(val.indexOf(".") > -1){
									//去掉毫秒
									val = val.substring(0,val.indexOf("."));

									val = "TO_DATE('"+val+"', 'yyyy-MM-dd HH24:mi:ss')";
								}
							}

							columnList.add(key);
							valueList.add(val);
						}
					}
				}

				Integer id = Integer.parseInt(item.get("ID").toString());

				String exSql = "select count(*) from SMT_SNAP_VEHICLE where ID=" + id;
				int count = platform2Dao.selectCount(exSql);
				if(count > 0){
					return;
				}

				//查询人员ID是否为员工表主键或访客表主键
				Long personId = Long.parseLong(item.get("DRIVER_ID").toString());
				Map<String,Object> staffMap = platform1Dao.getStaffInfoById(personId);
				if(Objects.isNull(staffMap)){
					return;
				}

				//根据员工工号查询二期员工信息
				Map<String, Object> dao2Staff = platform2Dao.getStaffInclude0(staffMap.get("BADGE").toString());
				if(Objects.isNull(dao2Staff) || Objects.isNull(dao2Staff.get("STATUS"))){
					return;
				}

				if(Integer.parseInt(dao2Staff.get("STATUS").toString()) == 0){
					//员工离职状态 根据身份证号查询在职的记录
					dao2Staff = platform2Dao.getStaffByCertno(staffMap.get("CERTNO").toString());
					if(Objects.isNull(dao2Staff)){
						return;
					}
				}

				long staffId2 = Long.parseLong(dao2Staff.get("ID").toString());

				//替换通行记录的PERSON_ID信息
				for(int i = 0;i<columnList.size();i++){
					if("DRIVER_ID".equals(columnList.get(i))){
						valueList.set(i,String.valueOf(staffId2));
						break;
					}
				}

				StringBuilder stringBuilder = new StringBuilder();

				stringBuilder.append("INSERT INTO SMT_SNAP_VEHICLE(");

				for(int i = 0;i<columnList.size();i++){
					if(i == columnList.size() - 1){
						stringBuilder.append("\"").append(columnList.get(i)).append("\"");
					} else {
						stringBuilder.append("\"").append(columnList.get(i)).append("\",");
					}
				}
				stringBuilder.append(") VALUES (");

				for(int i = 0;i<valueList.size();i++){
					if(!valueList.get(i).startsWith("TO_DATE(")){
						stringBuilder.append("'");
					}
					stringBuilder.append(valueList.get(i));
					if(!valueList.get(i).startsWith("TO_DATE(")){
						stringBuilder.append("'");
					}

					if(i != valueList.size() - 1){
						stringBuilder.append(",");
					}
				}

				stringBuilder.append(")");

				String insertSql = stringBuilder.toString();
				platform2Dao.insertRecord(insertSql);

			});
			current++;
			mapList = platform1Dao.getSnapVehicleList(current, size);
		} while (true);
	}

	public void cutImgData(){
		RestTemplate restTemplate = new RestTemplate();
		int current = 1;
		int size = 200;
		List<Map<String, Object>> mapList = platform2Dao.getTaskRecordImgList(current, size);

		do {
			if(CollectionUtil.isEmpty(mapList)){
				break;
			}

			mapList.forEach(item -> {

				Integer id = Integer.parseInt(item.get("ID").toString());
				Blob blob = (Blob)item.get("IMAGE");

				//String general = item.get("GENERAL").toString();

				InputStream is = null;
				byte[] b = null;
				try {
					is = blob.getBinaryStream();
					b = new byte[(int) blob.length()];
					is.read(b);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						is.close();
						is = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				String bigImgBase64 = Base64.encode(b);

				//调用裁剪接口
				String cutUrl = "https://tech.szyuto.com/algorithm/out/face/cut";

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);

				Map<String, Object> map = new HashMap<>();
				map.put("serialNo", UUID.randomUUID());
				map.put("imageData", bigImgBase64);

				HttpEntity<String> request = new HttpEntity<>(JSONUtil.toJsonStr(map), headers);
				String result = restTemplate.postForObject(cutUrl, request, String.class);
				JSONObject dataObj = JSONUtil.parseObj(result);
				if(dataObj.containsKey("code") && dataObj.getInt("code") == 0){
					String cutImg = dataObj.getStr("data");
					byte[] bytes = ImageUtils.base64StrToByte(cutImg);
					platform2Dao.updateImg(id,bytes);
				} else {
					System.out.println("调用裁剪接口失败:" + result);
				}

			});
			current++;
			mapList = platform2Dao.getTaskRecordImgList(current, size);
		} while (true);
	}

	public void copyData(){
		//查询一期platform库 所有的用户表
		//List<String> tableNameList = platform1Dao.getUserTablesName();
		List<String> tableNameList = new ArrayList<>();
		tableNameList.add("SMT_SNAP_PERSON");



		//需要过滤掉的表
		List<String> excludeTab = new ArrayList<>();
		excludeTab.add("TO_C6_EPHOTO");
		excludeTab.add("SMT_EMAIL_RECEIVE");
		excludeTab.add("SMT_CALLOWANCE_CANCEL_RECORD");
		excludeTab.add("SMT_WAGE_SIGN");
		excludeTab.add("SMT_MSG_RECORD");
		excludeTab.add("SMT_PROCESS_RECORD");
		excludeTab.add("SMT_APPLICATION_RESUME");

		//迁移部分数据的表 2021-01-01 00:00:00 时间后的数据
		List<String> partDataTab = new ArrayList<>();
		partDataTab.add("SMT_SNAP_PERSON");
		partDataTab.add("SMT_SNAP_VEHICLE");
		partDataTab.add("SMT_VISITOR");
		partDataTab.add("SMT_ALARM_RECORD");
		partDataTab.add("SMT_MSG_RECORD");

		long startTime = System.currentTimeMillis();

		ExecutorService cachedThreadPool = Executors.newFixedThreadPool(10);

		for(String tabName : tableNameList){
			if(excludeTab.contains(tabName)){
				continue;
			}
			cachedThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					handelData(tabName,partDataTab);
				}
			});
		}

		cachedThreadPool.shutdown();

		while (true) {
			if (cachedThreadPool.isTerminated()) {
				log.info("执行完成，耗时：{}ms", (System.currentTimeMillis() - startTime));
				break;
			}
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
			}
		}
	}

	private String getVarchar(Object obj){
		return obj == null ? "" : obj.toString().replaceAll("'","''");
	}

	public void handelData(String tabName,List<String> partDataTab){

		StringBuilder stringBuilder = new StringBuilder();
		String separator = System.getProperty("line.separator");

		//先禁用表的所有触发器
		List<String> tableTriggerList = platform2Dao.getTableTriggerList(tabName);
		if(CollectionUtil.isNotEmpty(tableTriggerList)){
			tableTriggerList.forEach(item -> {
				platform2Dao.disableTableTrigger(item);
			});
		}

		if("SMT_DEVICE_TASK".equalsIgnoreCase(tabName)){
			copyDeviceTaskData();
			return;
		}

		List<Map<String, String>> tableColumnAndDataType = platform1Dao.getTableColumnAndDataType(tabName);

		Map<String, List<Map<String, String>>> columnNameMap = tableColumnAndDataType.stream().collect(Collectors.groupingBy(e -> e.get("COLUMN_NAME")));

		//分页查询表数据
		int current = 1;
		int size = 10;
		String sql = getSql(tabName,current, size);

		if(partDataTab.contains(tabName)){
			sql = getPartDataSql(tabName,current, size);
		}

		List<Map<String,Object>> dataList = platform1Dao.getPage(sql);
		do {
			if(CollectionUtil.isEmpty(dataList)){
				//如果数据不存在 则跳过
				if(current == 1){
					//第一页都没有数据 表示该表是一个空表
					String info = "表 " + tabName + " 没有数据";
					stringBuilder.append(info).append(separator);
				}
				break;
			}


			dataList.forEach(item -> {
				StringBuilder insertBuilder = new StringBuilder();
				String fields = "";

				String vals = "";
				String id = "";
				//拼接值
				for (String key : item.keySet()){
					if("ROWNO".equalsIgnoreCase(key)){
						continue;
					}

					if("SMT_STAFF".equalsIgnoreCase(tabName) && "PARK_ID".equalsIgnoreCase(key)){
						//一期的员工表里的 PARK_ID 字段不能写入到二期数据库中
						continue;
					}

					if("PARK_ID".equalsIgnoreCase(key)){
						item.put(key,26);
					}

					if("ID".equalsIgnoreCase(key)){
						id = item.get(key).toString();
						//根据ID查询是否已存在
						String querySql = "select count(*) from " + tabName + " where ID=" + item.get(key);
						if("SMT_DEVICE".equalsIgnoreCase(tabName) || "SMT_PARKING".equalsIgnoreCase(tabName)){
							querySql = "select count(*) from " + tabName + " where ID='" + item.get(key) + "'";
						} else if("SMT_STAFF".equalsIgnoreCase(tabName)){
							//员工表应该判断工号是否存在
							querySql = "select count(*) from " + tabName + " where ID=" + item.get(key) + " or BADGE=" + item.get("BADGE");
						}
						int count = platform2Dao.selectCount(querySql);
						if(count > 0){
							return;
						}
					}

					String dataType = columnNameMap.get(key).get(0).get("DATA_TYPE");
					if("VARCHAR2".equals(dataType)){
						vals += ("'"+getVarchar(item.get(key)) + "',");
					} else if("DATE".equals(dataType)){
						String date = item.get(key).toString();
						if(date.indexOf(".") > -1){
							//去掉毫秒
							date = date.substring(0,date.indexOf("."));
						}
						vals += ("to_date('"+ date +"','yyyy-MM-dd HH24:mi:ss') " + ",");
					} else if("CLOB".equalsIgnoreCase(dataType)){
						vals += (ClobToString((Clob) item.get(key)) + ",");
					}
					else {
						vals += (item.get(key) + ",");
					}
					fields += key + ",";
				}

				//去掉最后一个逗号
				fields = fields.substring(0,fields.length()-1);
				vals = vals.substring(0,vals.length()-1);

				if("SMT_WAGE_SIGN".equalsIgnoreCase(tabName)){

					fields += ",SIGN_STATUS,NOTICE_STATUS";

					//拼接字段
					vals += ",1,2";
				} else if("SMT_MSG_RECORD".equalsIgnoreCase(tabName)
						|| "SMT_DEVICE_AUTHORITY".equalsIgnoreCase(tabName)
						|| "SMT_DORMITORY_TYPE".equalsIgnoreCase(tabName)
				){
					fields += ",PARK_ID";
					vals += ",26";
				} else if("SMT_VEHICLE".equalsIgnoreCase(tabName)){

				}

				String insertHead = "insert into " + tabName + " (" + fields + ") values ";
				insertBuilder.append(insertHead);

				insertBuilder.append("(").append(vals).append(")");

				int res = platform2Dao.insertRecord(insertBuilder.toString());
				if(res == 1){
					//写入成功
					log.info("表{}写入成功一条数据{}",tabName,id);
				} else {
					//写入失败
					log.info("表{}写入失败一条数据{}",tabName,id);
				}
			});


			current++;
			sql = getSql(tabName,current, size);
			dataList = platform1Dao.getPage(sql);

		} while (true);

		//启用表的所有触发器
		if(CollectionUtil.isNotEmpty(tableTriggerList)){
			tableTriggerList.forEach(item -> {
				platform2Dao.enableTableTrigger(item);
			});
		}

		log.info("表{}的数据迁移完成",tabName);
	}

	public void enableAllTrigger(){
		List<String> userTablesName = platform2Dao.getUserTablesName();
		userTablesName.forEach(item -> {
			List<String> tableTriggerList = platform2Dao.getTableTriggerList(item);
			tableTriggerList.forEach(trigger -> {
				platform2Dao.enableTableTrigger(trigger);
			});
		});
	}


}
