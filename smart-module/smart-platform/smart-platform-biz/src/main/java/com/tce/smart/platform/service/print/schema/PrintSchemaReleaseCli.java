package com.tce.smart.platform.service.print.schema;

import java.io.PrintStream;
import java.sql.*;
import java.util.*;

/** 发布流水线显式调用的 Java main；凭据仅从环境获取，不启动 Spring 或应用服务。 */
public final class PrintSchemaReleaseCli {
    private PrintSchemaReleaseCli() { }

    public static void main(String[] args) { System.exit(run(args,System.getenv(),System.out,System.err)); }

    static int run(String[] args,Map<String,String> environment,PrintStream out,PrintStream err) {
        try {
            Options options=Options.parse(args);
            if (options.help) {
                out.println("PrintSchemaReleaseCli [--validate|--plan|--apply] --expected-schema SCHEMA");
                out.println("--apply 另需 --release-id ID --confirm-version "+PrintSchemaRelease.VERSION+" --confirm-checksum SHA256");
                out.println("凭据环境变量: PRINT_SCHEMA_JDBC_URL / PRINT_SCHEMA_JDBC_USER / PRINT_SCHEMA_JDBC_PASSWORD");
                return 0;
            }
            out.println("version="+PrintSchemaRelease.VERSION+" checksum="+PrintSchemaRelease.checksum());
            String url=required(environment,"PRINT_SCHEMA_JDBC_URL");
            String user=required(environment,"PRINT_SCHEMA_JDBC_USER");
            String password=required(environment,"PRINT_SCHEMA_JDBC_PASSWORD");
            // 禁止 URL 内携带凭据和非 Oracle 端点；H2 仅供包内发布器测试。
            if (!url.startsWith("jdbc:oracle:thin:@") || !user.equals(options.schema)) throw new IllegalArgumentException("Oracle URL 或 schema 用户不匹配");
            Class.forName("oracle.jdbc.OracleDriver");
            try (Connection connection=DriverManager.getConnection(url,user,password)) {
                if (!PrintSchemaInspector.oracle(connection)) throw new IllegalArgumentException("CLI 仅支持 Oracle");
                PrintSchemaRelease release=new PrintSchemaRelease();
                String state;
                if ("apply".equals(options.mode)) state=release.apply(connection,options.schema,options.releaseId);
                else state=release.inspect(connection,options.schema);
                out.println("schema="+options.schema+" state="+state);
                if ("plan".equals(options.mode)) {
                    if ("ABSENT".equals(state)) for(String statement:release.plan()) out.println(statement+";");
                    else out.println("本版本已完成，无待执行语句。");
                    return 0;
                }
                return "APPLIED".equals(state)?0:2;
            }
        } catch (Exception e) {
            // JDBC 异常可能携带 URL、账号或密码；只输出固定状态和数字错误码。
            err.println("PRINT_SCHEMA_RELEASE_REJECTED; 请核对授权参数、目标 schema、发布账本和独立验收记录。"
                +(e instanceof SQLException?" jdbcErrorCode="+((SQLException)e).getErrorCode():""));
            return 1;
        }
    }

    private static String required(Map<String,String> environment,String name) {
        String value=environment.get(name); if(value==null || value.isEmpty()) throw new IllegalArgumentException("缺少发布环境变量"); return value;
    }

    static final class Options {
        String mode="validate";
        String schema;
        String releaseId;
        boolean help;
        static Options parse(String[] args) {
            Options result=new Options(); Set<String> seen=new HashSet<>(); Map<String,String> values=new HashMap<>(); boolean selected=false;
            for(int i=0;i<args.length;i++) {
                String key=args[i]; if(!seen.add(key)) throw new IllegalArgumentException("参数重复");
                if("--help".equals(key)) { result.help=true; continue; }
                if(Arrays.asList("--validate","--plan","--apply").contains(key)) {
                    if(selected) throw new IllegalArgumentException("模式互斥"); selected=true; result.mode=key.substring(2); continue;
                }
                if(!Arrays.asList("--expected-schema","--release-id","--confirm-version","--confirm-checksum").contains(key) || i+1>=args.length) throw new IllegalArgumentException("未知或缺少参数");
                values.put(key,args[++i]);
            }
            if(result.help) { if(args.length!=1) throw new IllegalArgumentException("help 不接受执行参数"); return result; }
            result.schema=values.get("--expected-schema");
            if(result.schema==null || !result.schema.matches("[A-Z][A-Z0-9_]{0,127}")) throw new IllegalArgumentException("必须确认目标 schema");
            result.releaseId=values.get("--release-id");
            if("apply".equals(result.mode)) {
                if(result.releaseId==null || !result.releaseId.matches("[A-Za-z0-9][A-Za-z0-9._:-]{0,127}") || !PrintSchemaRelease.VERSION.equals(values.get("--confirm-version")) || !PrintSchemaRelease.checksum().equals(values.get("--confirm-checksum"))) throw new IllegalArgumentException("必须明确确认 release-id、版本及校验和");
            } else if(values.size()!=1) throw new IllegalArgumentException("只读模式不接受写入授权参数");
            return result;
        }
    }
}
