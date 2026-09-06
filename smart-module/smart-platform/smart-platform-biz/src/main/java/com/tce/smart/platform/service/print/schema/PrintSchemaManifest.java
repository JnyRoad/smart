package com.tce.smart.platform.service.print.schema;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** 已发布版本只追加、不改写；声明仅生成本版本新增表和索引，不接受外部 SQL。 */
final class PrintSchemaManifest {
    static final String VERSION = "009-print-v1";
    static final Table HISTORY = table("SCHEMA_RELEASE", "VERSION:V32!,CHECKSUM:V64!,RELEASE_ID:V128!,STATUS:V16!,STARTED_AT:T!,FINISHED_AT:T,APPLIED_BY:V128!,COMPLETED_STEPS:N10!,FAILED_STEP:V128,ERROR_CODE:V32")
            .unique("UK_PRT_RELEASE_ID", "RELEASE_ID");
    static final List<Table> TABLES = Collections.unmodifiableList(Arrays.asList(
        table("TEMPLATE", "TEMPLATE_ID:V36,PARK_ID:V64,TEMPLATE_KEY:V64,NAME:V100,PRINT_ITEM_TYPE:V64,PERSON_TYPE:V64,CLASSIFICATION_CODE:V64,FACE_ROLE:V10,LIFECYCLE_STATUS:V16,CURRENT_DRAFT_VERSION_ID:V36,CURRENT_PUBLISHED_VERSION_ID:V36,DRAFT_REVISION:N19,CREATED_BY:V128,CREATED_AT:T,UPDATED_BY:V128,UPDATED_AT:T,ARCHIVED_AT:T")
            .unique("UK_PRT_TEMPLATE_KEY", "PARK_ID", "TEMPLATE_KEY").index("IX_PRT_TEMPLATE_PARK", "PARK_ID", "CREATED_AT", "TEMPLATE_ID"),
        table("TEMPLATE_VER", "TEMPLATE_VERSION_ID:V36,TEMPLATE_ID:V36,PARK_ID:V64,VERSION_NO:N19,VERSION_STATUS:V16,FACE_ROLE:V10,SIDE_COUNT:N10,LAYOUT_JSON:C,FIELD_SCHEMA_JSON:C,RESOURCE_MANIFEST_JSON:C,PAGE_SPEC_JSON:C,VALIDATION_REPORT_JSON:C,CONTENT_HASH:V71,DRAFT_REVISION:N19,PUBLISHED_AT:T,PUBLISHED_BY:V128,CREATED_AT:T,CREATED_BY:V128")
            .unique("UK_PRT_TEMPLATE_VER", "TEMPLATE_ID", "VERSION_NO"),
        table("TEMPLATE_PAIR", "PAIR_ID:V36,PARK_ID:V64,NAME:V100,PRINT_ITEM_TYPE:V64,PERSON_TYPE:V64,CLASSIFICATION_CODE:V64,FRONT_TEMPLATE_VERSION_ID:V36,BACK_TEMPLATE_VERSION_ID:V36,REVISION:N19,STATUS:V16,CREATED_BY:V128,CREATED_AT:T,UPDATED_BY:V128,UPDATED_AT:T,ARCHIVED_AT:T")
            .index("IX_PRT_PAIR_PARK", "PARK_ID", "CREATED_AT", "PAIR_ID"),
        // 兼容设备原始身份前缀；不能沿用模板测试夹具的 64 字符操作主体列。
        table("OPERATION", "OPERATION_ID:V36,PRINCIPAL_ID:V135,IDEMPOTENCY_KEY:V128,BODY_HASH:V71,RESPONSE_JSON:C,CREATED_AT:T")
            .unique("UK_PRT_OPERATION_KEY", "PRINCIPAL_ID", "IDEMPOTENCY_KEY"),
        table("AUDIT", "AUDIT_ID:V36,PARK_ID:V64,ACTOR_ID:V135,ACTION:V64,OBJECT_ID:V36,DETAILS_JSON:C,CREATED_AT:T")
            .index("IX_PRT_AUDIT_OBJECT", "OBJECT_ID", "CREATED_AT", "AUDIT_ID"),
        table("PREVIEW", "PREVIEW_ID:V36,PARK_ID:V64,CREATED_BY:V128,CREATED_AT:T,STATUS:V32,DETAILS_JSON:C"),
        table("OBJECT", "OBJECT_ID:V36,PARK_ID:V64,CREATED_BY:V128,PURPOSE:V32,ACCESS_SCOPE:V32,OWNER_ID:V36,CONTENT_HASH:V71,MEDIA_TYPE:V80,SIZE_BYTES:N19,CREATED_AT:T,CONTENT_BYTES:B"),
        table("BIND_RULE", "BINDING_RULE_ID:V36,PARK_ID:V64,PRINT_ITEM_TYPE:V64,PERSON_TYPE:V64,CLASSIFICATION_CODE:V64,SCOPE_TYPE:V32,SCOPE_ID:V64,TEMPLATE_ID:V36,PAIR_ID:V36,EMPLOYEE_GRADE_CODES_CLOB:C,PRIORITY:N10,VALID_FROM:T,VALID_TO:T,STATUS:V16,REVISION:N19,CREATED_BY:V128,CREATED_AT:T,UPDATED_BY:V128,UPDATED_AT:T")
            .index("IX_PRT_BIND_CANDIDATE", "PARK_ID", "PRINT_ITEM_TYPE", "PERSON_TYPE", "CLASSIFICATION_CODE", "STATUS"),
        table("PRINTER_PROFILE", "PRINTER_PROFILE_ID:V36,PARK_ID:V128,DEVICE_IDENTITY:V128,STATUS:V128,ACTIVE_JOB_ID:V36,CONFIG_JSON:C,CONFIG_REVISION:N19")
            .index("IX_PRT_PRINTER_PARK", "PARK_ID", "PRINTER_PROFILE_ID"),
        table("JOB", "JOB_ID:V36,PARK_ID:V128,CREATED_BY:V128,PRINTER_PROFILE_ID:V36,DEVICE_IDENTITY:V128,SUBJECT_TYPE:V128,SUBJECT_ID:V128,ACTIVE_SUBJECT_KEY:V512,PRINT_ITEM_TYPE:V128,PRINT_MODE:V128,STATUS:V128,SNAPSHOT_JSON:C,TEMPLATE_SNAPSHOT_HASH:V71,PRINTER_SNAPSHOT_HASH:V71,CURRENT_ATTEMPT_ID:V36,CLAIM_ID:V36,CLIENT_INSTANCE_ID:V36,ARTIFACTS_JSON:C,OPERATOR_CHECK_ID:V36,ERROR_CODE:V128,STATE_JSON:C,CREATED_AT:T,UPDATED_AT:T,LEASE_EXPIRES_AT:T")
            .unique("UK_PRT_JOB_CLAIM", "CLAIM_ID").unique("UK_PRT_JOB_ACTIVE_SUBJECT", "ACTIVE_SUBJECT_KEY")
            .index("IX_PRT_JOB_OWNER", "PARK_ID", "CREATED_BY", "CREATED_AT", "JOB_ID")
            .index("IX_PRT_JOB_QUEUE", "STATUS", "CREATED_AT", "JOB_ID"),
        table("ATTEMPT", "ATTEMPT_ID:V36,JOB_ID:V36,COMMAND_ID:V36,FACE:V128,STATE_JSON:C,ATTEMPT_NO:N19")
            .unique("UK_PRT_ATTEMPT_COMMAND", "COMMAND_ID").unique("UK_PRT_ATTEMPT_NO", "JOB_ID", "ATTEMPT_NO"),
        table("EVENT", "EVENT_ID:V36,JOB_ID:V36,ATTEMPT_ID:V36,COMMAND_ID:V36,EVENT_TYPE:V128,BODY_HASH:V71,RESPONSE_JSON:C,DETAILS_JSON:C,CREATED_AT:T")
            .index("IX_PRT_EVENT_JOB", "JOB_ID", "CREATED_AT", "EVENT_ID"),
        table("JOB_ARTIFACT", "ARTIFACT_ID:V36,OWNER_ID:V36,PARK_ID:V128,FACE:V128,CONTENT_HASH:V71,CONTENT_BYTES:B"),
        table("JOB_PREVIEW", "PREVIEW_ID:V36,PARK_ID:V128,CREATED_BY:V128,DETAILS_JSON:C")
    ));

    static List<Table> allTables() { List<Table> all=new ArrayList<>(); all.add(HISTORY); all.addAll(TABLES); return all; }
    static List<String> businessStatements() { List<String> all=new ArrayList<>(); for (Table table:TABLES) all.addAll(table.statements()); return all; }
    static String checksum() {
        try {
            StringBuilder canonical=new StringBuilder(VERSION).append('\n');
            for (Table table:allTables()) for (String sql:table.statements()) canonical.append(sql).append(';').append('\n');
            StringBuilder hex=new StringBuilder();
            for (byte value:MessageDigest.getInstance("SHA-256").digest(canonical.toString().getBytes(StandardCharsets.UTF_8))) hex.append(String.format(Locale.ROOT,"%02x",value & 255));
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 不可用",e); }
    }

    private static Table table(String suffix, String columns) { return new Table(suffix,columns); }

    static final class Column {
        final String name;
        final char kind;
        final int size;
        final boolean nullable;
        Column(String definition, boolean primary) {
            String[] parts=definition.split(":"); name=parts[0]; String type=parts[1];
            nullable=!primary && !type.endsWith("!"); type=type.replace("!",""); kind=type.charAt(0); size=type.length()>1?Integer.parseInt(type.substring(1)):0;
        }
        String ddl() {
            String type;
            switch (kind) {
                case 'V': type="VARCHAR2("+size+" CHAR)"; break;
                case 'N': type="NUMBER("+size+",0)"; break;
                case 'T': type="TIMESTAMP(6)"; break;
                case 'C': type="CLOB"; break;
                case 'B': type="BLOB"; break;
                default: throw new IllegalStateException("未知列类型");
            }
            return name+" "+type+(nullable?"":" NOT NULL");
        }
    }
    static final class Key {
        final String name;
        final String kind;
        final List<String> columns;
        Key(String name,String kind,String... columns) { this.name=name; this.kind=kind; this.columns=Arrays.asList(columns); }
    }
    static final class Table {
        final String name;
        final List<Column> columns=new ArrayList<>();
        final List<Key> keys=new ArrayList<>();
        Table(String suffix,String definition) {
            name="SMT_PRINT_"+suffix;
            for (String c:definition.split(",")) columns.add(new Column(c,columns.isEmpty()));
            keys.add(new Key("PK_PRT_"+suffix,"PRIMARY KEY",columns.get(0).name));
        }
        Table unique(String name,String... columns) { keys.add(new Key(name,"UNIQUE",columns)); return this; }
        Table index(String name,String... columns) { keys.add(new Key(name,"INDEX",columns)); return this; }
        List<String> statements() {
            List<String> parts=new ArrayList<>(); for (Column column:columns) parts.add(column.ddl());
            for (Key key:keys) if (!"INDEX".equals(key.kind)) parts.add("CONSTRAINT "+key.name+" "+key.kind+" ("+String.join(",",key.columns)+")");
            List<String> statements=new ArrayList<>(); statements.add("CREATE TABLE "+name+" ("+String.join(",",parts)+")");
            for (Key key:keys) if ("INDEX".equals(key.kind)) statements.add("CREATE INDEX "+key.name+" ON "+name+" ("+String.join(",",key.columns)+")");
            return statements;
        }
    }
}
