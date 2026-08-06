package com.tce.smart.transfer.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.tce.smart.tool.util.ImageUtils;
import com.tce.smart.transfer.config.HbaseFindBuilder;
import com.tce.smart.transfer.dao1.Platform1Dao;
import com.tce.smart.transfer.dao2.Platform2Dao;
import com.tce.smart.transfer.entity.BlobInfo;
import com.tce.smart.transfer.entity.HbaseConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class StaffImageTransferService {

	@Resource
	private Platform1Dao platform1Dao;

	@Resource
	private Platform2Dao platform2Dao;

//	@Autowired
//	private Connection connection;
//
//	@Autowired
//	private HBaseAdmin hbaseAdmin;

	@Resource
	private HbaseTemplate hbaseTemplate;

	@Resource
	private FaceCropInternalClient faceCropInternalClient;

	private static final HbaseConstant.Table tableBlob = HbaseConstant.Table.T_BLOB;

	public void transferStaffImg() throws Exception{
		//查询一期的员工数据
		int currentPage = 1;
		int size = 20;

		List<Map<String, Object>> staffList = null;
		do{
			staffList = platform1Dao.getStaffList(currentPage, size);
			if(CollectionUtil.isEmpty(staffList)){
				break;
			}
			log.info("员工数量：{}", staffList.size());
			for(Map<String,Object> map : staffList){

				String createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				if(null != map.get("CREATE_TIME")){
					createTime = map.get("CREATE_TIME").toString();
				}
				String centNoPicId = "";
				String facePicId = "";
//				if(null != map.get("CERTNO_PIC_ID")){
//					centNoPicId = map.get("CERTNO_PIC_ID").toString();
//					if(StringUtils.isNotEmpty(centNoPicId)){
//						//判断员工身份证正面照图片是否存在二期的图片表中
//						int count = platform2Dao.imgCount(centNoPicId);
////						if(count < 1){
////							handleImg(centNoPicId,createTime,211,"员工身份证正面照");
////						}
//						if(count > 0){
//							platform2Dao.deleteImg(centNoPicId);
//						}
//						handleImg(centNoPicId,createTime,211,"员工身份证正面照");
//					}
//				}

				if(null != map.get("FACE_PIC_ID")){
					facePicId = map.get("FACE_PIC_ID").toString();
					if(StringUtils.isNotEmpty(facePicId)){
						//判断员工人脸图片是否存在二期的图片表中
						//int count = platform2Dao.imgCount(facePicId);
//						if(count < 1){
//							handleImg(facePicId,createTime,11,"员工人脸照片");
//						}
//						if(count > 0){
//							platform2Dao.deleteImg(facePicId);
//						}
						String badge = map.get("BADGE").toString();
						handleImg(facePicId,createTime,11,"员工人脸照片",badge);
					}
				}
//				if (null != map.get("BADGE")) {
//					String badge = map.get("BADGE").toString();
//					log.info("员工工号：{}", badge);
//					platform2Dao.updateStaffImg(centNoPicId, facePicId, badge);
//				}
			}
			currentPage++;
		} while (staffList.size() == size);
	}

	private void handleImg(String imgCode,String createTime,int type,String remark,String badge) throws Exception{
		log.info("开始处理图片{}", imgCode);
		//从一期的hbase中获取图片信息

		HttpResponse execute = HttpUtil.createGet("http://10.0.20.136:6030/image/view/" + imgCode).execute();

		byte[] bigImg = execute.bodyBytes();
		if (bigImg == null || bigImg.length == 0) {
			log.info("{}图片数据为空", imgCode);
			return;
		}
//		String bigImgBase64 = Base64.encode(bigImg);
//		byte[] smallImgByte = this.image2Small(bigImgBase64, ImageUtils.SMALL_FIXED_SIZE_W, ImageUtils.SMALL_FIXED_SIZE_H);
//		platform2Dao.saveImg(imgCode, bigImg, smallImgByte,type,remark,createTime,26);

		//调用裁剪接口
		byte[] imageBinaryByCode = null;
		try {

			String cutImg = faceCropInternalClient.crop(Base64.encode(bigImg));
			imageBinaryByCode = ImageUtils.base64StrToByte(cutImg);

			String filePath = "E:\\裕同大岭山人脸图片\\" + badge+".jpg";
			File file = new File(filePath);
			if(file.exists()){
				return;
			}

			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(imageBinaryByCode);
			fileOutputStream.flush();
			fileOutputStream.close();
		}catch (Exception e){
			log.error("员工人脸图片迁移时裁剪失败，图片编号={}", imgCode, e);
		}

//		BlobInfo blobInfo = getBlobInfo(imgCode);
//		if(null != blobInfo){
//			//写入二期的图片表
//			byte[] image = null;
//			if(StringUtils.isNotEmpty(blobInfo.getBase64())){
//				image = blobInfo.getBase64().getBytes();
//			}
//			byte[] imageSmall = null;
//			if(StringUtils.isNotEmpty(blobInfo.getSmall())){
//				imageSmall = blobInfo.getSmall().getBytes();
//			} else {
//				//制作缩略图
//				imageSmall = this.image2Small(blobInfo.getBase64(), ImageUtils.SMALL_FIXED_SIZE_W, ImageUtils.SMALL_FIXED_SIZE_H);
//			}
//			platform2Dao.saveImg(imgCode,image,imageSmall,type,remark,createTime,26);
//		}
	}

	public String getBlob(String blobId) throws Exception{
		String base64 = null;
		BlobInfo blobInfo = getBlobInfo(blobId);
		if(null!=blobInfo)
			base64 = blobInfo.getBase64();

		return base64;
	}

	public String getImageSmall(String blobId) throws Exception {
		String smallBase64 = null;
		BlobInfo blobInfo = getBlobInfo(blobId);
		if(null!=blobInfo)
			smallBase64 = blobInfo.getSmall();

		return smallBase64;
	}

	private BlobInfo getBlobInfo(String blobId)throws Exception{
		Validate.isTrue(StringUtils.isNotEmpty(blobId), "blobId 不合法");

		String tableName = "blob_store";

		String rowkey = blobId;
		Map<String,String[]> familyQualifiers = tableBlob.getFamilyQualifiers();
		BlobInfo blobInfo = hbaseTemplate.get(tableName, rowkey, new RowMapper<BlobInfo>(){
			@Override
			public BlobInfo mapRow(Result result, int rowNum) throws Exception {
				return (BlobInfo)new HbaseFindBuilder(familyQualifiers,BlobInfo.class).
						build(result).fetch();
			}
		});
		return blobInfo;
	}

	private byte[] image2Small(String imgBase64, int width, int height) {
		byte[] b_small = null;
		byte[] imageByte = ImageUtils.base64StrToByte(imgBase64);
		try {
			b_small = ImageUtils.fixedSizeByte(imageByte, width, height);
		} catch (Exception e) {
		}

		return b_small;
	}
}
