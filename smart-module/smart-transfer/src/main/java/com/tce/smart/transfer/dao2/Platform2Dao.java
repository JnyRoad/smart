package com.tce.smart.transfer.dao2;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface Platform2Dao {

	@Select("select count(*) from SMT_IMAGE where IMAGE_CODE=#{imgCode}")
	int imgCount(@Param("imgCode") String imgCode);

	@Delete("delete from SMT_IMAGE where IMAGE_CODE=#{imgCode}")
	int deleteImg(@Param("imgCode") String imgCode);

	@Insert("insert into SMT_IMAGE(IMAGE_CODE,IMAGE,IMAGE_SMALL,IMAGE_TYPE,REMARK,CREATE_TIME,PARK_ID) VALUES(#{imgCode}," +
			"#{image},#{imageSmall},#{type},#{remark}," +
			"to_date(#{createDate},'yyyy-MM-dd HH24:mi:ss')," +
			"#{parkId})")
	int saveImg(@Param("imgCode") String imgCode, @Param("image") byte[] image, @Param("imageSmall") byte[] imageSmall, @Param("type") int type,
				@Param("remark") String remark, @Param("createDate") String createDate,@Param("parkId") int parkId);

	@Select("SELECT column_name FROM user_tab_columns where table_name = upper(#{tableName})")
	List<String> getTableColumns(@Param("tableName")String tableName);

	@Update("update SMT_STAFF set CERTNO_PIC_ID=#{certNoId},FACE_PIC_ID=#{facePicId} where BADGE=#{badge}")
	int updateStaffImg(@Param("certNoId") String certNoId, @Param("facePicId") String facePicId, @Param("badge") String badge);

	@Insert("${sql}")
	int insertRecord(@Param("sql") String sql);

	@Select("${sql}")
	int selectCount(@Param("sql") String sql);

	@Select("select trigger_name from all_triggers where table_name=upper(#{tableName})")
	List<String> getTableTriggerList(@Param("tableName")String tableName);

	@Select("ALTER TRIGGER ${triggerName} DISABLE")
	void disableTableTrigger(@Param("triggerName") String triggerName);

	@Select("ALTER TRIGGER ${triggerName} ENABLE")
	void enableTableTrigger(@Param("triggerName") String triggerName);


	@Select("select t.table_name from user_tables t")
	List<String> getUserTablesName();

	@Select("select ID,BADGE from SMT_STAFF where BADGE=#{badge} and STATUS != 0")
	Map<String,Object> getStaff(@Param("badge") String badge);

	@Select("select ID,BADGE,STATUS from SMT_STAFF where BADGE=#{badge}")
	Map<String,Object> getStaffInclude0(@Param("badge") String badge);

	@Select("select ID,BADGE,CERTNO from SMT_STAFF where CERTNO=#{certNo} and STATUS != 0")
	Map<String,Object> getStaffByCertno(@Param("certNo") String certNo);

	@Select("select count(*) from SMT_STAFF_DEVICE_AUTH where STAFF_ID=#{staffId} and AUTH_ID=#{authId}")
	int getCountDeviceAuth(@Param("staffId") Long staffId, @Param("authId") Integer authId);

	@Insert("insert into SMT_STAFF_DEVICE_AUTH(STAFF_ID,AUTH_ID,CREATE_TIME) VALUES(#{staffId},#{authId},to_date(#{createTime},'yyyy-MM-dd HH24:mi:ss'))")
	int saveDeviceAuth(@Param("staffId") Long staffId, @Param("authId") Integer authId, @Param("createTime") String createTime);

	@Select("${sql}")
	Map<String,Object> execOneRecord(@Param("sql") String sql);

	@Select("SELECT *\n" +
			"\n" +
			"  FROM (SELECT tt.*, ROWNUM AS rowno\n" +
			"\n" +
			"          FROM (SELECT * FROM SMT_TASK_DOWN_RECORD WHERE DEVICE_CODE=#{deviceId}) tt\n" +
			"\n" +
			"         WHERE ROWNUM <= (#{currentPage} * #{size})) table_alias\n" +
			"\n" +
			" WHERE table_alias.rowno >= ((#{currentPage}-1)*#{size} + 1)")
	List<Map<String,Object>> getTaskRecordList(@Param("currentPage") Integer currentPage,@Param("size") Integer size,@Param("deviceId")String deviceId);

	@Select("SELECT *\n" +
			"\n" +
			"  FROM (SELECT tt.*, ROWNUM AS rowno\n" +
			"\n" +
			"          FROM (select * from SMT_TASK_DOWN_RECORD WHERE DEVICE_CODE in (\n" +
			"\t'b5ab6d2e6f7349d2ae7aea02e7c3684c','3fd5764af0b444e3bf60bb776c962aae','0e4b8c9ce72e42aea4c4e4aa92c2e4f5','dfade4ba03804192967d9aff1ab71495','3cde530e29f440718332a4cbeabb86b2'\n" +
			")) tt\n" +
			"\n" +
			"         WHERE ROWNUM <= (#{currentPage} * #{size})) table_alias\n" +
			"\n" +
			" WHERE table_alias.rowno >= ((#{currentPage}-1)*#{size} + 1)")
	List<Map<String,Object>> getVehicleTaskRecordList(@Param("currentPage") Integer currentPage,@Param("size") Integer size);


	@Select("SELECT *\n" +
			"\n" +
			"  FROM (SELECT tt.*, ROWNUM AS rowno\n" +
			"\n" +
			"          FROM (select b.ID,b.IMAGE from SMT_IMAGE b where exists (select 1 from SMT_DEVICE_TASK a where a.IMAGE_ID=b.IMAGE_CODE and a.DEVICE_CODE in (\n" +
			"'385488233e8643f1b5fc2766524aab32',\n" +
			"'34fb7a7216bf464eb982e94f511d5063',\n" +
			"'b8a989dcc3574a36b2ab9b728b29d4e4',\n" +
			"'d9e11bba9a40432bb1494ff96db50050'\n" +
			") and a.ACTION=1 and a.status != 1) and LENGTH(b.IMAGE) > 20000) tt" +
			"         WHERE ROWNUM <= (#{currentPage} * #{size})) table_alias\n" +
			"\n" +
			" WHERE table_alias.rowno >= ((#{currentPage}-1)*#{size} + 1)")
	List<Map<String,Object>> getTaskRecordImgList(@Param("currentPage") Integer currentPage,@Param("size") Integer size);

	@Update("UPDATE SMT_IMAGE SET IMAGE=#{image} where ID=#{id}")
	int updateImg(@Param("id") Integer id, @Param("image") byte[] image);

}
