package com.tce.smart.algorithm.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
@EqualsAndHashCode(callSuper = true)
public class FaceImgCutReq extends BaseDTO {

	private String serialNo;

	private String imageData;
}
