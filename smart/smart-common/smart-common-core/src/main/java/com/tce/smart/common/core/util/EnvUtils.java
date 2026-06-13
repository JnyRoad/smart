package com.tce.smart.common.core.util;

/**
 * @Description: TODO
 * @ProjectName smart
 * @ClassName: EnvUtils
 * @Author jinbo
 * @Date 2019/4/25
 */
public class EnvUtils {
    private static final String DEBUG = "DEBUG";

    private static final String TEST = "TEST";

    private static final String DEV = "DEV";

    private static final String PROD = "PROD";

    public static boolean isDebug(String profile) {
        return profile.equalsIgnoreCase(DEBUG);
    }

    public static boolean isDev(String profile) {
        return profile.equalsIgnoreCase(DEV);
    }

    public static boolean isTest(String profile) {
        return profile.equalsIgnoreCase(TEST);
    }

    public static boolean isProd(String profile) {
        return profile.equalsIgnoreCase(PROD);
    }
}
