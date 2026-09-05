package com.tce.smart.platform.core.mapper;

import com.tce.smart.platform.core.entity.energy.SmtEnergyMeterDayFact;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

/** 通过 MyBatis 实际展开动态语句，验证日期分流和终态入队边界；不代表 Oracle 执行计划验证。 */
public class SmtEnergyRecoveryMapperContractTest {
    /** 实时区间含昨天，历史区间严格小于昨天，两类不重叠且都受预算限制。 */
    @Test public void candidateDateRangesAreDisjointAndBounded() throws Exception {
        Configuration config=configuration("SmtEnergyProjectionQueueMapper");
        Map<String,Object> parameters=new HashMap<>(); parameters.put("limit",100); parameters.put("now",LocalDateTime.of(2026,9,5,12,0));
        parameters.put("fromDate",LocalDate.of(2026,9,4));
        BoundSql recent=config.getMappedStatement("com.tce.smart.platform.core.mapper.energy.SmtEnergyProjectionQueueMapper.selectCandidatesByDate").getBoundSql(parameters);
        assertTrue(recent.getSql().contains("STAT_DATE >= ?")); assertFalse(recent.getSql().contains("STAT_DATE < ?"));
        assertTrue(recent.getSql().contains("ROWNUM <= ?"));
        parameters.remove("fromDate"); parameters.put("beforeDate",LocalDate.of(2026,9,4));
        String history=config.getMappedStatement("com.tce.smart.platform.core.mapper.energy.SmtEnergyProjectionQueueMapper.selectCandidatesByDate").getBoundSql(parameters).getSql();
        assertTrue(history.contains("STAT_DATE < ?")); assertFalse(history.contains("STAT_DATE >= ?"));
    }

    /** 补齐的条件更新只能覆盖终态，不能清空活跃任务重试或租约。 */
    @Test public void backfillRequeueOnlyUpdatesTerminalRows() throws Exception {
        Configuration config=configuration("SmtEnergyProjectionQueueMapper");
        String sql=config.getMappedStatement("com.tce.smart.platform.core.mapper.energy.SmtEnergyProjectionQueueMapper.requeueIdle").getBoundSql(new HashMap<>()).getSql();
        assertTrue(sql.substring(sql.indexOf("WHERE")).contains("QUEUE_STATUS IN ('DONE','FAILED')"));
    }

    /** 日事实插入绑定传入园区，匹配更新不得覆盖首次保存的历史园区快照。 */
    @Test public void mergeFactPreservesParkSnapshotOnMatchedUpdate() throws Exception {
        Configuration config=configuration("SmtEnergyMeterDayFactMapper");
        SmtEnergyMeterDayFact fact=SmtEnergyMeterDayFact.builder().parkId(2L).build();
        BoundSql boundSql=config.getMappedStatement("com.tce.smart.platform.core.mapper.energy.SmtEnergyMeterDayFactMapper.mergeFact").getBoundSql(fact);
        String sql=boundSql.getSql().replaceAll("\\s+"," ").toUpperCase(java.util.Locale.ROOT);
        String matched=sql.substring(sql.indexOf("WHEN MATCHED"),sql.indexOf("WHEN NOT MATCHED"));
        String inserted=sql.substring(sql.indexOf("WHEN NOT MATCHED"));

        assertEquals(fact,boundSql.getParameterObject());
        assertTrue(boundSql.getParameterMappings().stream()
                .anyMatch(mapping -> "parkId".equals(mapping.getProperty())));
        assertFalse(matched.contains("PARK_ID"));
        assertTrue(inserted.contains("INSERT (ID,PARK_ID"));
        assertTrue(inserted.contains("VALUES(S.ID,S.PARK_ID"));
    }

    /** 解析真实 Mapper 文件及参数映射，XML 或属性不合法时直接让测试失败。 */
    private Configuration configuration(String mapper) throws Exception {
        Configuration configuration=new Configuration();
        String resource="mapper/"+mapper+".xml";
        try(InputStream input=getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input);
            new XMLMapperBuilder(input,configuration,resource,configuration.getSqlFragments()).parse();
        }
        return configuration;
    }
}
