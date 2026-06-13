package com.tce.smart.tool.util;

import com.obs.services.ObsClient;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObsObject;
import com.obs.services.model.ObjectListing;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class HuaweiOBSUtil {

    private static String obsAk() {
        return requiredConfig("smart.obs.ak", "SMART_OBS_AK");
    }

    private static String obsSk() {
        return requiredConfig("smart.obs.sk", "SMART_OBS_SK");
    }

    private static String obsEndpoint() {
        return config("smart.obs.endpoint", "SMART_OBS_ENDPOINT", "https://obs.cn-east-3.myhuaweicloud.com");
    }

    private static String obsBucketName() {
        return config("smart.obs.bucket-name", "SMART_OBS_BUCKET_NAME", "obs-yuto14");
    }

    private static String obsDirPath() {
        return config("smart.obs.dir-path", "SMART_OBS_DIR_PATH", "Photo/App/");
    }

    private static String smbDomain() {
        return config("smart.smb.photo.domain", "SMART_SMB_PHOTO_DOMAIN", "");
    }

    private static String smbUsername() {
        return requiredConfig("smart.smb.photo.username", "SMART_SMB_PHOTO_USERNAME");
    }

    private static String smbPassword() {
        return requiredConfig("smart.smb.photo.password", "SMART_SMB_PHOTO_PASSWORD");
    }

    private static String smbRemotePhotoUrl() {
        return config("smart.smb.photo.url", "SMART_SMB_PHOTO_URL", "smb://10.0.20.99/photo$/App");
    }

    /**
     * 判断对象是否存在
     * @param objectName
     * @return boolean 是否存在
     */
    public static boolean isObjectExists(String objectName) {
        boolean result = false;
        String ak = obsAk();
        String sk = obsSk();
        String endpoint = obsEndpoint();
        String bucketName = obsBucketName();
        try {
            // 创建ObsClient实例
            // 使用永久AK/SK初始化客户端
            ObsClient obsClient = new ObsClient(ak, sk, endpoint);

            result = obsClient.doesObjectExist(bucketName, objectName);
            log.info("dhr照片同步-检查图片是否存在结果：{}", result);
        } catch (Exception e) {
            log.error("dhr照片同步-检查图片是否存在异常:", e);
        }

        return result;
    }

    /**
     * 获取指定桶下的所有对象
     * @return List<String> 对象名列表
     */
    public static List<String> readLastRemoteImgNameList(LocalDateTime lastTime) {
        List<String> fileNameList = new ArrayList<>();
        String ak = obsAk();
        String sk = obsSk();
        String endpoint = obsEndpoint();
        String bucketName = obsBucketName();
        String dirPath = obsDirPath();

        try {
            // 创建ObsClient实例
            // 使用永久AK/SK初始化客户端
            ObsClient obsClient = new ObsClient(ak, sk, endpoint);

            log.info("dhr照片库获取对象列表-开始");
            // 列举文件夹中的所有对象
            ListObjectsRequest request = new ListObjectsRequest(bucketName);
            // 设置文件夹对象名前缀
            request.setPrefix(dirPath);
            // 设置每页列举对象数量
            request.setMaxKeys(1000);
            ObjectListing result;
            do {
                result = obsClient.listObjects(request);
                for (ObsObject obsObject : result.getObjects()) {
                    // System.out.println("ObjectKey:" + obsObject.getObjectKey());
                    Date lastModified = obsObject.getMetadata().getLastModified();
                    // 将 Date 转换为 LocalDateTime
                    LocalDateTime dateTime = LocalDateTime.ofInstant(lastModified.toInstant(), ZoneId.systemDefault());
                    if (lastTime.isBefore(dateTime)) {
                        String idCard = obsObject.getObjectKey().replace(".jpg", "").replace(dirPath, "");
                        log.info("dhr照片库获取到更新对象，修改时间：{}", lastModified);
                        fileNameList.add(idCard);
                    }
                }
                request.setMarker(result.getNextMarker());
            } while (result.isTruncated());
            log.info("dhr照片库获取对象列表-结束");
            return fileNameList;
        } catch (Exception e) {
            log.error("dhr照片库获取对象列表-获取指定桶下的所有对象异常:", e);
        }

        return null;
    }

    public static List<String> readLastRemoteImgNameList1(LocalDateTime lastTime) {
        List<String> fileNameList = new ArrayList<>();
        String smbDomain = smbDomain();
        String smbUsername = smbUsername();
        String smbPassword = smbPassword();
        String remotePhotoUrl = smbRemotePhotoUrl();
        try {
            //获取图片
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(smbDomain, smbUsername, smbPassword);
            SmbFile remoteFile = new SmbFile(remotePhotoUrl + "/", auth);
            remoteFile.connect(); //尝试连接
            log.info("开始获取最后更新文件1");
            if (remoteFile != null) {
                log.info("开始获取最后更新文件2");
                if (remoteFile.isDirectory()) {
                    SmbFile[] smbFiles = remoteFile.listFiles();
                    log.info("开始获取最后更新文件3={}", smbFiles.length);
                    for (int i = 0; i < smbFiles.length; i++) {
                        SmbFile file = smbFiles[i];
                        long lastModified = file.getLastModified();
                        LocalDateTime longToLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified), ZoneId.systemDefault());
                        if (lastTime.isBefore(longToLocalDateTime)) {
                            fileNameList.add(file.getName().replace(".jpg", ""));
                        }
                    }
                }
            }
            return fileNameList;
        } catch (Exception e) {
            log.error("连接dhr照片库异常:", e);
        }
        return null;
    }

    /**
     * 读取OBS图片转为base64
     * @param idCard
     * @return 图片Base64编码
     */
    public static String readRemoteImgToBase64(String idCard) {

        String imgPath = obsDirPath() + idCard + ".jpg";
        InputStream input = null;
        ByteArrayOutputStream bos = null;
        if (!isObjectExists(imgPath)) {
            log.info("dhr照片同步图片不存在");
            return "";
        }
        String ak = obsAk();
        String sk = obsSk();
        String endpoint = obsEndpoint();
        String bucketName = obsBucketName();
        try {
            log.info("dhr照片同步图片存在");

            ObsClient obsClient = new ObsClient(ak, sk, endpoint);

            // 流式下载
            ObsObject obsObject = obsClient.getObject(bucketName, imgPath);
            // 读取对象内容
            input = obsObject.getObjectContent();
            byte[] b = new byte[1024];
            bos = new ByteArrayOutputStream();
            int len;
            while ((len = input.read(b)) != -1) {
                bos.write(b, 0, len);
            }

            String base64 = ImageUtils.encodeImage(bos.toByteArray());
            log.info("dhr照片同步结束");

            bos.close();
            input.close();

            return base64;
        } catch (Exception e) {
            log.info("dhr-urlFile-error");
            log.error("dhr照片同步获取异常:", e);
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
            }
        }
        return "";
    }

    private static String config(String propertyName, String envName) {
        return config(propertyName, envName, "");
    }

    private static String config(String propertyName, String envName, String defaultValue) {
        return SmartToolConfigUtils.get(propertyName, envName, defaultValue);
    }

    private static String requiredConfig(String propertyName, String envName) {
        return SmartToolConfigUtils.getRequired(propertyName, envName);
    }
}
