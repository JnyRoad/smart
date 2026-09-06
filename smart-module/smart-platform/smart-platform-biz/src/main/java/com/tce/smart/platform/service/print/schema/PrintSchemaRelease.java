package com.tce.smart.platform.service.print.schema;

import java.sql.*;
import java.util.*;

/** 显式执行的版本发布器；不是 Spring Bean，服务启动不会调用或创建表。 */
public final class PrintSchemaRelease {
    public static final String VERSION = PrintSchemaManifest.VERSION;
    public static String checksum() { return PrintSchemaManifest.checksum(); }

    /** 只读检查；空库返回 ABSENT，完整且有匹配发布记录的库返回 APPLIED，其余拒绝。 */
    public String inspect(Connection connection,String expectedSchema) throws SQLException {
        PrintSchemaInspector.requireTarget(connection,expectedSchema);
        Set<String> objects=PrintSchemaInspector.objects(connection,expectedSchema);
        if (!objects.contains(PrintSchemaManifest.HISTORY.name)) {
            PrintSchemaInspector.requireFresh(objects); return "ABSENT";
        }
        PrintSchemaInspector.validateTable(connection,expectedSchema,PrintSchemaManifest.HISTORY);
        try (Statement s=connection.createStatement(); ResultSet r=s.executeQuery("SELECT VERSION,CHECKSUM,RELEASE_ID,STATUS,STARTED_AT,FINISHED_AT,APPLIED_BY,COMPLETED_STEPS,FAILED_STEP,ERROR_CODE FROM SMT_PRINT_SCHEMA_RELEASE")) {
            if (!r.next()) throw new SQLException("已有发布账本无版本记录，禁止自动认领");
            if (!VERSION.equals(r.getString(1)) || !checksum().equals(r.getString(2))) throw new SQLException("发布版本或校验和不匹配");
            requireReleaseId(r.getString(3));
            if (!"APPLIED".equals(r.getString(4)) || r.getTimestamp(5)==null || r.getTimestamp(6)==null || r.getString(7)==null || r.getInt(8)!=PrintSchemaManifest.businessStatements().size() || r.getString(9)!=null || r.getString(10)!=null) throw new SQLException("部分或失败发布，必须人工核对并经新版本恢复，禁止自动续跑");
            if (r.next()) throw new SQLException("发现未知发布版本，禁止使用旧执行器");
        }
        for (PrintSchemaManifest.Table table:PrintSchemaManifest.TABLES) PrintSchemaInspector.validateTable(connection,expectedSchema,table);
        return "APPLIED";
    }

    /** 只有显式发布调用才写入；独占自动提交连接，不向外部提供任意 SQL 或删除能力。 */
    public String apply(Connection connection,String expectedSchema,String releaseId) throws SQLException {
        requireReleaseId(releaseId);
        if ("APPLIED".equals(inspect(connection,expectedSchema))) {
            try (Statement s=connection.createStatement(); ResultSet r=s.executeQuery("SELECT RELEASE_ID FROM SMT_PRINT_SCHEMA_RELEASE")) {
                r.next(); if(!releaseId.equals(r.getString(1))) throw new SQLException("已安装版本属于另一发布记录，不能改写 release-id");
            }
            return "APPLIED";
        }
        // 创建账本是首次安装的争用门禁。失败时不得更新另一发布进程的账本。
        for (String sql:PrintSchemaManifest.HISTORY.statements()) execute(connection,sql);
        try (PreparedStatement s=connection.prepareStatement("INSERT INTO SMT_PRINT_SCHEMA_RELEASE (VERSION,CHECKSUM,RELEASE_ID,STATUS,STARTED_AT,APPLIED_BY,COMPLETED_STEPS) VALUES (?,?,?,'STARTED',CURRENT_TIMESTAMP,?,0)")) {
            s.setString(1,VERSION); s.setString(2,checksum()); s.setString(3,releaseId); s.setString(4,connection.getMetaData().getUserName()); s.executeUpdate();
        }
        int completed=0; String step="PRECHECK";
        try {
            List<String> statements=PrintSchemaManifest.businessStatements();
            for (String sql:statements) {
                step=sql.substring(0,sql.indexOf(" ("));
                execute(connection,sql); completed++;
                progress(connection,releaseId,completed,"STARTED",null,null);
            }
            for (PrintSchemaManifest.Table table:PrintSchemaManifest.TABLES) PrintSchemaInspector.validateTable(connection,expectedSchema,table);
            progress(connection,releaseId,completed,"APPLIED",null,null);
            return inspect(connection,expectedSchema);
        } catch (SQLException | RuntimeException e) {
            try { progress(connection,releaseId,completed,"FAILED",step,e instanceof SQLException?Integer.toString(((SQLException)e).getErrorCode()):"RUNTIME"); }
            catch (SQLException recordFailure) { e.addSuppressed(recordFailure); }
            // Oracle DDL 会隐式提交，不能通过 rollback 撤销已建表；绝不删除结构或证据。
            throw e;
        }
    }

    /** 发布计划是固定版本声明，不读取外部脚本。 */
    public List<String> plan() {
        List<String> plan=new ArrayList<>();
        for(PrintSchemaManifest.Table table:PrintSchemaManifest.allTables()) plan.addAll(table.statements());
        return Collections.unmodifiableList(plan);
    }

    private static void progress(Connection connection,String releaseId,int completed,String status,String failedStep,String errorCode) throws SQLException {
        try (PreparedStatement s=connection.prepareStatement("UPDATE SMT_PRINT_SCHEMA_RELEASE SET STATUS=?,COMPLETED_STEPS=?,FINISHED_AT=CASE WHEN ?='STARTED' THEN NULL ELSE CURRENT_TIMESTAMP END,FAILED_STEP=?,ERROR_CODE=? WHERE VERSION=? AND CHECKSUM=? AND RELEASE_ID=?")) {
            s.setString(1,status); s.setInt(2,completed); s.setString(3,status); s.setString(4,failedStep); s.setString(5,errorCode); s.setString(6,VERSION); s.setString(7,checksum()); s.setString(8,releaseId);
            if(s.executeUpdate()!=1) throw new SQLException("发布记录已变化，停止执行");
        }
    }
    private static void execute(Connection connection,String sql) throws SQLException { try (Statement s=connection.createStatement()) { s.execute(sql); } }
    private static void requireReleaseId(String releaseId) {
        if (releaseId==null || !releaseId.matches("[A-Za-z0-9][A-Za-z0-9._:-]{0,127}")) throw new IllegalArgumentException("release-id 必须为 1 至 128 位发布记录标识");
    }
}
