package com.tce.smart.algorithm.bean.facedetect.seeta;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName: Image
 * @Package com.tce.smart.algorithm.bean.staticlive.seeta
 * @Description:
 * @Author wuxinjian
 * @Date 2020/2/3 11:04
 * @Version V1.0
 */
@Data
@Builder
public class FaceDetectReq implements Serializable {

	private static final long serialVersionUID = 1L;

	private String serialNo;

	private String imageData;
}
