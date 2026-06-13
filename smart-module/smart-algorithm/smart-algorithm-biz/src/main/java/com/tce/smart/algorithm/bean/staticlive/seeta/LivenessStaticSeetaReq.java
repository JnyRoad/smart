package com.tce.smart.algorithm.bean.staticlive.seeta;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: LivenessStaticSeetaReq
 * @Package com.tce.smart.algorithm.bean.staticlive.seeta
 * @Description:
 * @Author wuxinjian
 * @Date 2020/2/3 11:05
 * @Version V1.0
 */
@Data
public class LivenessStaticSeetaReq implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	private String type;

	private List<Image> images;
}
