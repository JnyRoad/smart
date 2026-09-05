package com.tce.smart.platform.service.print.schema;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** H2 提供实际表元数据，仅替换 Oracle 字典查询；不连接真实 Oracle。 */
public class PrintSchemaOracleConstraintTest {
    private Connection local;
    private PrintSchemaManifest.Table table;

    @Before public void open() throws Exception {
        local=DriverManager.getConnection("jdbc:h2:mem:oracle_constraint_"+UUID.randomUUID()+";MODE=Oracle","sa","");
        for(PrintSchemaManifest.Table candidate:PrintSchemaManifest.TABLES) if("SMT_PRINT_PREVIEW".equals(candidate.name)) table=candidate;
        assertNotNull(table);
        try(Statement statement=local.createStatement()) { statement.execute(table.statements().get(0)); }
    }

    @After public void close() throws Exception { local.close(); }

    @Test public void actualManifestNotNullWithGeneratedNameRemainsValid() throws Exception {
        PrintSchemaInspector.validateTable(oracle(null),"PUBLIC",table);
    }

    @Test public void generatedOrdinaryCheckMustNotBeMistakenForNotNull() throws Exception {
        rejected("\"STATUS\"='BLOCKED'");
    }

    @Test public void notNullOnManifestNullableColumnIsStillAnUnknownConstraint() throws Exception {
        rejected("\"STATUS\" IS NOT NULL");
    }

    @Test public void checkWithExtraPredicateOrUnknownColumnMustBeRejected() throws Exception {
        rejected("\"PREVIEW_ID\" IS NOT NULL AND \"STATUS\"='BLOCKED'");
        rejected("\"UNKNOWN_ID\" IS NOT NULL");
    }

    @Test public void unreadableGeneratedCheckExpressionMustFailClosed() throws Exception {
        PrintSchemaInspector.validateTable(oracle(null),"PUBLIC",table);
        rejected("");
        rejected(null);
    }

    private void rejected(String extraCondition) throws Exception {
        try {
            PrintSchemaInspector.validateTable(oracle(extraCondition,true),"PUBLIC",table);
            fail("未登记的 Oracle CHECK 必须拒绝，不能仅凭 GENERATED NAME 放行");
        } catch(SQLException expected) { assertTrue(expected.getMessage().contains("约束")); }
    }

    private Connection oracle(String extraCondition) throws Exception { return oracle(extraCondition,extraCondition!=null); }

    private Connection oracle(String extraCondition,boolean includeExtra) throws Exception {
        DatabaseMetaData original=local.getMetaData();
        DatabaseMetaData metadata=mock(DatabaseMetaData.class,invocation -> {
            if("getDatabaseProductName".equals(invocation.getMethod().getName())) return "Oracle";
            try { return invocation.getMethod().invoke(original,invocation.getArguments()); }
            catch(InvocationTargetException e) { throw e.getCause(); }
        });
        Connection connection=mock(Connection.class); when(connection.getMetaData()).thenReturn(metadata);
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql=invocation.getArgument(0);
            List<Object[]> rows=new ArrayList<>();
            if(sql.contains("FROM USER_TAB_COLUMNS")) {
                rows.add(new Object[]{"PREVIEW_ID","C",36,0});
                rows.add(new Object[]{"PARK_ID","C",64,0});
                rows.add(new Object[]{"CREATED_BY","C",128,0});
                rows.add(new Object[]{"CREATED_AT",null,0,6});
                rows.add(new Object[]{"STATUS","C",32,0});
                rows.add(new Object[]{"DETAILS_JSON",null,0,0});
            } else if(sql.contains("FROM USER_CONSTRAINTS")) {
                rows.add(new Object[]{"PK_PRT_PREVIEW","P","ENABLED","VALIDATED","NOT DEFERRABLE","USER NAME",null});
                rows.add(new Object[]{"SYS_C001","C","ENABLED","VALIDATED","NOT DEFERRABLE","GENERATED NAME","\"PREVIEW_ID\" IS NOT NULL"});
                if(includeExtra) rows.add(new Object[]{"SYS_C002","C","ENABLED","VALIDATED","NOT DEFERRABLE","GENERATED NAME",extraCondition});
            } else if(sql.contains("FROM USER_INDEXES") || sql.contains("FROM USER_TRIGGERS")) rows.add(new Object[]{0});
            else throw new SQLException("测试未允许此 SQL");
            PreparedStatement statement=mock(PreparedStatement.class);
            when(statement.executeQuery()).thenAnswer(ignored -> result(rows));
            return statement;
        });
        return connection;
    }

    private ResultSet result(List<Object[]> rows) throws Exception {
        AtomicInteger position=new AtomicInteger(-1);
        ResultSet result=mock(ResultSet.class);
        when(result.next()).thenAnswer(ignored -> position.incrementAndGet()<rows.size());
        when(result.getString(anyInt())).thenAnswer(invocation -> {
            Object value=rows.get(position.get())[(Integer)invocation.getArgument(0)-1];
            return value==null?null:String.valueOf(value);
        });
        when(result.getInt(anyInt())).thenAnswer(invocation -> ((Number)rows.get(position.get())[(Integer)invocation.getArgument(0)-1]).intValue());
        return result;
    }
}
