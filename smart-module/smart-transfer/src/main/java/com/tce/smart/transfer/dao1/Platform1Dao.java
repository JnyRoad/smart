package com.tce.smart.transfer.dao1;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface Platform1Dao {

	@Select("SELECT *\n" +
			"\n" +
			"  FROM (SELECT tt.*, ROWNUM AS rowno\n" +
			"\n" +
			"          FROM ( select CERTNO_PIC_ID,FACE_PIC_ID,BADGE,to_char(CREATE_TIME,'YYYY-MM-DD HH24:MI:SS') AS CREATE_TIME from SMT_STAFF where STATUS<>0 and FACE_PIC_ID is not null ORDER BY ID) tt\n" +
			"\n" +
			"         WHERE ROWNUM <= (#{currentPage} * #{size})) table_alias\n" +
			"\n" +
			" WHERE table_alias.rowno >= ((#{currentPage}-1)*#{size} + 1)")
	List<Map<String,Object>> getStaffList(@Param("currentPage") Integer currentPage,@Param("size") Integer size);


	@Select("SELECT *\n" +
			"\n" +
			"  FROM (SELECT tt.*, ROWNUM AS rowno\n" +
			"\n" +
			"          FROM ( select * from SMT_DEVICE_TASK where DEVICE_TYPE = 1 AND  ACTION=1 AND STATUS=1 AND LENGTH(GENERAL)>6) tt\n" +
			"\n" +
			"         WHERE ROWNUM <= (#{currentPage} * #{size})) table_alias\n" +
			"\n" +
			" WHERE table_alias.rowno >= ((#{currentPage}-1)*#{size} + 1)")
	List<Map<String,Object>> getDeviceTaskSuccList(@Param("currentPage") Integer currentPage,@Param("size") Integer size);

	@Select("SELECT *\n" +
			"\n" +
			"  FROM (SELECT tt.*, ROWNUM AS rowno\n" +
			"\n" +
			"          FROM ( select * from SMT_DEVICE_TASK where DEVICE_TYPE = 2 AND  ACTION=1 AND STATUS=1) tt\n" +
			"\n" +
			"         WHERE ROWNUM <= (#{currentPage} * #{size})) table_alias\n" +
			"\n" +
			" WHERE table_alias.rowno >= ((#{currentPage}-1)*#{size} + 1)")
	List<Map<String,Object>> getCarDeviceTaskSuccList(@Param("currentPage") Integer currentPage,@Param("size") Integer size);

	@Select("SELECT *\n" +
			"\n" +
			"  FROM (SELECT tt.*, ROWNUM AS rowno\n" +
			"\n" +
			"          FROM ( select * from SMT_VEHICLE_STAFF) tt\n" +
			"\n" +
			"         WHERE ROWNUM <= (#{currentPage} * #{size})) table_alias\n" +
			"\n" +
			" WHERE table_alias.rowno >= ((#{currentPage}-1)*#{size} + 1)")
	List<Map<String,Object>> getVehicleStaffList(@Param("currentPage") Integer currentPage,@Param("size") Integer size);

	@Select("SELECT *\n" +
			"\n" +
			"  FROM (SELECT tt.*, ROWNUM AS rowno\n" +
			"\n" +
			"          FROM (select * from SMT_VEHICLE_APPLY where STATUS=1) tt\n" +
			"\n" +
			"         WHERE ROWNUM <= (#{currentPage} * #{size})) table_alias\n" +
			"\n" +
			" WHERE table_alias.rowno >= ((#{currentPage}-1)*#{size} + 1)")
	List<Map<String,Object>> getVehicleApplyList(@Param("currentPage") Integer currentPage,@Param("size") Integer size);


	@Select("SELECT *\n" +
			"\n" +
			"  FROM (SELECT tt.*, ROWNUM AS rowno\n" +
			"\n" +
			"          FROM (select * from SMT_APPLICATION_PROCESS) tt\n" +
			"\n" +
			"         WHERE ROWNUM <= (#{currentPage} * #{size})) table_alias\n" +
			"\n" +
			" WHERE table_alias.rowno >= ((#{currentPage}-1)*#{size} + 1)")
	List<Map<String,Object>> getApplicationList(@Param("currentPage") Integer currentPage,@Param("size") Integer size);

	@Select("SELECT *\n" +
			"\n" +
			"  FROM (SELECT tt.*, ROWNUM AS rowno\n" +
			"\n" +
			"          FROM (select * from SMT_APPLICATION_EMAIL) tt\n" +
			"\n" +
			"         WHERE ROWNUM <= (#{currentPage} * #{size})) table_alias\n" +
			"\n" +
			" WHERE table_alias.rowno >= ((#{currentPage}-1)*#{size} + 1)")
	List<Map<String,Object>> getApplicationEmailList(@Param("currentPage") Integer currentPage,@Param("size") Integer size);

	@Select("SELECT *\n" +
			"\n" +
			"  FROM (SELECT tt.*, ROWNUM AS rowno\n" +
			"\n" +
			"          FROM (select * from SMT_APPLICATION_EDUCATION) tt\n" +
			"\n" +
			"         WHERE ROWNUM <= (#{currentPage} * #{size})) table_alias\n" +
			"\n" +
			" WHERE table_alias.rowno >= ((#{currentPage}-1)*#{size} + 1)")
	List<Map<String,Object>> getApplicationEducationList(@Param("currentPage") Integer currentPage,@Param("size") Integer size);

	@Select("SELECT *\n" +
			"\n" +
			"  FROM (SELECT tt.*, ROWNUM AS rowno\n" +
			"\n" +
			"          FROM (select * from SMT_SNAP_PERSON where PERSON_TYPE=1 and CREATE_TIME > TO_DATE('2021-09-01 00:00:00', 'yyyy-MM-dd HH24:mi:ss') ORDER BY CREATE_TIME DESC) tt\n" +
			"\n" +
			"         WHERE ROWNUM <= (#{currentPage} * #{size})) table_alias\n" +
			"\n" +
			" WHERE table_alias.rowno >= ((#{currentPage}-1)*#{size} + 1)")
	List<Map<String,Object>> getSnapPersonList(@Param("currentPage") Integer currentPage,@Param("size") Integer size);

	@Select("SELECT *\n" +
			"\n" +
			"  FROM (SELECT tt.*, ROWNUM AS rowno\n" +
			"\n" +
			"          FROM (select * from SMT_SNAP_VEHICLE where DRIVER_TYPE=1 and CREATE_TIME > TO_DATE('2021-09-01 00:00:00', 'yyyy-MM-dd HH24:mi:ss') ORDER BY CREATE_TIME DESC) tt\n" +
			"\n" +
			"         WHERE ROWNUM <= (#{currentPage} * #{size})) table_alias\n" +
			"\n" +
			" WHERE table_alias.rowno >= ((#{currentPage}-1)*#{size} + 1)")
	List<Map<String,Object>> getSnapVehicleList(@Param("currentPage") Integer currentPage,@Param("size") Integer size);

	@Select("SELECT *\n" +
			"\n" +
			"  FROM (SELECT tt.*, ROWNUM AS rowno\n" +
			"\n" +
			"          FROM (select * from SMT_VISITOR where CREATE_TIME > TO_DATE('2021-09-01 00:00:00', 'yyyy-MM-dd HH24:mi:ss') ORDER BY CREATE_TIME DESC) tt\n" +
			"\n" +
			"         WHERE ROWNUM <= (#{currentPage} * #{size})) table_alias\n" +
			"\n" +
			" WHERE table_alias.rowno >= ((#{currentPage}-1)*#{size} + 1)")
	List<Map<String,Object>> getVisitorList(@Param("currentPage") Integer currentPage,@Param("size") Integer size);


	@Select("select t.table_name from user_tables t")
	List<String> getUserTablesName();

	@Select("SELECT column_name FROM user_tab_columns where table_name = upper(#{tableName})")
	List<String> getTableColumns(@Param("tableName")String tableName);

	@Select("SELECT COLUMN_NAME,DATA_TYPE FROM user_tab_columns where table_name = upper(#{tableName})")
	List<Map<String,String>> getTableColumnAndDataType(@Param("tableName")String tableName);

	@Select("${sql}")
	List<Map<String,Object>> getPage(@Param("sql") String sql);

	@Select("SELECT *\n" +
			"\n" +
			"  FROM (SELECT tt.*, ROWNUM AS rowno\n" +
			"\n" +
			"          FROM ( select a.STAFF_ID,b.BADGE,a.AUTH_ID,a.CREATE_TIME from SMT_STAFF_DEVICE_AUTH a join SMT_STAFF b on a.STAFF_ID=b.ID) tt\n" +
			"\n" +
			"         WHERE ROWNUM <= (#{currentPage} * #{size})) table_alias\n" +
			"\n" +
			" WHERE table_alias.rowno >= ((#{currentPage}-1)*#{size} + 1)")
	List<Map<String,Object>> getDeviceAuthData(@Param("currentPage") Integer currentPage,@Param("size") Integer size);

	@Select("select * from SMT_STAFF WHERE id=#{staffId}")
	Map<String,Object> getStaffInfoById(@Param("staffId")Long staffId);

	@Select("${sql}")
	int selectCount(@Param("sql") String sql);

	@Select("${sql}")
	List<Map<String,Object>> execRecord(@Param("sql") String sql);
}
