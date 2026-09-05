package com.tce.smart.platform.service.print.schema;

import java.sql.*;
import java.util.*;

/** 只读核对目标身份、实际列、主键、唯一性和索引；不修改用户已有结构。 */
final class PrintSchemaInspector {
    private PrintSchemaInspector() { }

    static boolean oracle(Connection db) throws SQLException { return "Oracle".equals(db.getMetaData().getDatabaseProductName()); }

    static void requireTarget(Connection db,String schema) throws SQLException {
        if (schema==null || !schema.matches("[A-Z][A-Z0-9_]{0,127}")) throw new IllegalArgumentException("必须显式指定大写 expected-schema");
        if (!db.getAutoCommit()) throw new SQLException("必须使用独占的自动提交连接，禁止提交调用方事务");
        String current;
        if (oracle(db)) {
            try (Statement s=db.createStatement(); ResultSet r=s.executeQuery("SELECT SYS_CONTEXT('USERENV','CURRENT_SCHEMA'), SYS_CONTEXT('USERENV','SESSION_USER') FROM DUAL")) {
                if (!r.next()) throw new SQLException("无法核实 Oracle 会话身份");
                current=r.getString(1);
                if (!schema.equals(r.getString(2))) throw new SQLException("schema 必须等于登录用户，禁止跨 schema 发布");
            }
        } else if ("H2".equals(db.getMetaData().getDatabaseProductName())) {
            current=db.getSchema();
        } else throw new SQLException("不支持的数据库类型");
        if (!schema.equals(current)) throw new SQLException("expected-schema 与当前 schema 不一致");
    }

    static Set<String> objects(Connection db,String schema) throws SQLException {
        Set<String> objects=new HashSet<>();
        if (oracle(db)) {
            try (Statement s=db.createStatement(); ResultSet r=s.executeQuery("SELECT OBJECT_NAME FROM USER_OBJECTS UNION SELECT CONSTRAINT_NAME FROM USER_CONSTRAINTS UNION SELECT SYNONYM_NAME FROM ALL_SYNONYMS WHERE OWNER='PUBLIC'")) {
                while (r.next()) objects.add(r.getString(1));
            }
        } else {
            try (ResultSet r=db.getMetaData().getTables(null,schema,null,null)) { while (r.next()) if (schema.equals(r.getString("TABLE_SCHEM"))) objects.add(r.getString("TABLE_NAME")); }
            try (PreparedStatement s=db.prepareStatement("SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_SCHEMA=? UNION SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.CONSTRAINTS WHERE CONSTRAINT_SCHEMA=?")) {
                s.setString(1,schema); s.setString(2,schema); try (ResultSet r=s.executeQuery()) { while (r.next()) objects.add(r.getString(1)); }
            }
        }
        return objects;
    }

    static void requireFresh(Set<String> objects) throws SQLException {
        for (PrintSchemaManifest.Table table:PrintSchemaManifest.allTables()) {
            if (objects.contains(table.name)) throw new SQLException("存在未经本版本登记的对象: "+table.name);
            for (PrintSchemaManifest.Key key:table.keys) if (objects.contains(key.name)) throw new SQLException("存在冲突索引或约束: "+key.name);
        }
    }

    static void validateTable(Connection db,String schema,PrintSchemaManifest.Table table) throws SQLException {
        DatabaseMetaData metadata=db.getMetaData();
        boolean found=false;
        // JDBC 的下划线是模式通配符，必须再用完整名称比对。
        try (ResultSet r=metadata.getTables(null,schema,table.name,null)) {
            while (r.next()) if (schema.equals(r.getString("TABLE_SCHEM")) && table.name.equals(r.getString("TABLE_NAME")) && ("TABLE".equals(r.getString("TABLE_TYPE")) || "BASE TABLE".equals(r.getString("TABLE_TYPE")))) found=true;
        }
        if (!found) throw drift(table.name,"缺少实际表");
        Map<String,PrintSchemaManifest.Column> columns=new HashMap<>();
        for (PrintSchemaManifest.Column column:table.columns) columns.put(column.name,column);
        int seen=0;
        try (ResultSet r=metadata.getColumns(null,schema,table.name,null)) {
            while (r.next()) {
                if (!schema.equals(r.getString("TABLE_SCHEM")) || !table.name.equals(r.getString("TABLE_NAME"))) continue;
                String name=r.getString("COLUMN_NAME"); PrintSchemaManifest.Column column=columns.get(name);
                if (column==null) throw drift(table.name,"未知列 "+name);
                seen++; int type=r.getInt("DATA_TYPE"); int size=r.getInt("COLUMN_SIZE");
                boolean compatible;
                switch (column.kind) {
                    case 'V': compatible=type==Types.VARCHAR && size==column.size; break;
                    case 'N': compatible=(type==Types.NUMERIC || type==Types.DECIMAL) && size==column.size && r.getInt("DECIMAL_DIGITS")==0; break;
                    case 'T': compatible=type==Types.TIMESTAMP; break;
                    case 'C': compatible=type==Types.CLOB; break;
                    case 'B': compatible=type==Types.BLOB; break;
                    default: compatible=false;
                }
                int nullable=r.getInt("NULLABLE");
                if (!compatible || nullable!=(column.nullable?DatabaseMetaData.columnNullable:DatabaseMetaData.columnNoNulls) || r.getString("COLUMN_DEF")!=null) throw drift(table.name,"列类型、长度、默认值或可空性不符: "+name);
            }
        }
        if (seen!=columns.size()) throw drift(table.name,"缺少列");
        SortedMap<Integer,String> primary=new TreeMap<>();
        try (ResultSet r=metadata.getPrimaryKeys(null,schema,table.name)) { while(r.next()) primary.put(r.getInt("KEY_SEQ"),r.getString("COLUMN_NAME")); }
        if (!new ArrayList<>(primary.values()).equals(table.keys.get(0).columns)) throw drift(table.name,"主键不符");
        Map<String,SortedMap<Integer,String>> indexColumns=new HashMap<>(); Set<String> uniqueNames=new HashSet<>();
        // 允许使用现有统计信息，校验不要求数据库刷新统计。
        try (ResultSet r=metadata.getIndexInfo(null,schema,table.name,false,true)) {
            while (r.next()) {
                if (r.getShort("TYPE")==DatabaseMetaData.tableIndexStatistic) continue;
                String name=r.getString("INDEX_NAME");
                if (name==null || r.getString("COLUMN_NAME")==null) throw drift(table.name,"存在无法核对的索引");
                indexColumns.computeIfAbsent(name,ignored -> new TreeMap<>()).put(r.getInt("ORDINAL_POSITION"),r.getString("COLUMN_NAME"));
                if (!r.getBoolean("NON_UNIQUE")) uniqueNames.add(name);
            }
        }
        Set<List<String>> actualUnique=new HashSet<>();
        for (String name:uniqueNames) actualUnique.add(new ArrayList<>(indexColumns.get(name).values()));
        Set<List<String>> expectedUnique=new HashSet<>();
        for (PrintSchemaManifest.Key key:table.keys) {
            if ("INDEX".equals(key.kind)) {
                SortedMap<Integer,String> actual=indexColumns.get(key.name);
                if (actual==null || !new ArrayList<>(actual.values()).equals(key.columns) || uniqueNames.contains(key.name)) throw drift(table.name,"缺少或错误索引: "+key.name);
            } else expectedUnique.add(key.columns);
        }
        if (!actualUnique.equals(expectedUnique)) throw drift(table.name,"唯一约束不符");
        try (ResultSet r=metadata.getImportedKeys(null,schema,table.name)) { if (r.next()) throw drift(table.name,"出现未经版本登记的外键"); }
        if (oracle(db)) validateOracleDetails(db,table);
    }

    private static void validateOracleDetails(Connection db,PrintSchemaManifest.Table table) throws SQLException {
        try (PreparedStatement s=db.prepareStatement("SELECT COLUMN_NAME,CHAR_USED,CHAR_LENGTH,DATA_SCALE FROM USER_TAB_COLUMNS WHERE TABLE_NAME=?")) {
            s.setString(1,table.name); try (ResultSet r=s.executeQuery()) {
                while(r.next()) for (PrintSchemaManifest.Column column:table.columns) if (column.name.equals(r.getString(1))) {
                    if (column.kind=='V' && (!"C".equals(r.getString(2)) || column.size!=r.getInt(3))) throw drift(table.name,"必须使用 CHAR 长度语义: "+column.name);
                    if (column.kind=='T' && r.getInt(4)!=6) throw drift(table.name,"时间戳精度不符: "+column.name);
                }
            }
        }
        Set<String> expected=new HashSet<>(); for(PrintSchemaManifest.Key key:table.keys) if(!"INDEX".equals(key.kind)) expected.add(key.name);
        try (PreparedStatement s=db.prepareStatement("SELECT CONSTRAINT_NAME,CONSTRAINT_TYPE,STATUS,VALIDATED,DEFERRABLE,GENERATED,SEARCH_CONDITION FROM USER_CONSTRAINTS WHERE TABLE_NAME=?")) {
            s.setString(1,table.name); try (ResultSet r=s.executeQuery()) {
                while(r.next()) {
                    // SEARCH_CONDITION 是 LONG，按列顺序读取并最后取完整表达式，不使用可能截断的替代字段。
                    String name=r.getString(1),type=r.getString(2),status=r.getString(3);
                    String validated=r.getString(4),deferrable=r.getString(5),generated=r.getString(6);
                    String condition=r.getString(7);
                    boolean known=expected.remove(name);
                    // GENERATED NAME 只证明名称由数据库生成；普通未命名 CHECK 也有这个标记。
                    boolean implicitNotNull="C".equals(type) && "GENERATED NAME".equals(generated) && manifestNotNull(table,condition);
                    if ((!known && !implicitNotNull) || !"ENABLED".equals(status) || !"VALIDATED".equals(validated) || !"NOT DEFERRABLE".equals(deferrable)) throw drift(table.name,"约束未知或未启用/验证");
                }
            }
        }
        if (!expected.isEmpty()) throw drift(table.name,"缺少具名约束");
        try (PreparedStatement s=db.prepareStatement("SELECT COUNT(*) FROM USER_INDEXES WHERE TABLE_NAME=? AND STATUS <> 'VALID'")) {
            s.setString(1,table.name); try (ResultSet r=s.executeQuery()) { r.next(); if(r.getInt(1)!=0) throw drift(table.name,"索引无效"); }
        }
        try (PreparedStatement s=db.prepareStatement("SELECT COUNT(*) FROM USER_TRIGGERS WHERE TABLE_NAME=?")) {
            s.setString(1,table.name); try (ResultSet r=s.executeQuery()) { r.next(); if(r.getInt(1)!=0) throw drift(table.name,"存在未经版本登记的触发器"); }
        }
    }
    private static boolean manifestNotNull(PrintSchemaManifest.Table table,String condition) {
        if (condition==null) return false;
        for (PrintSchemaManifest.Column column:table.columns) {
            if (!column.nullable && condition.trim().matches("\""+column.name+"\"\\s+(?i:IS)\\s+(?i:NOT)\\s+(?i:NULL)")) return true;
        }
        return false;
    }
    private static SQLException drift(String table,String reason) { return new SQLException("打印 schema 不兼容: "+table+" / "+reason); }
}
