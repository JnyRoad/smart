package com.tce.smart.algorithm.bean.staticlive.seeta;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName: Response
 * @Package com.tce.smart.algorithm.bean.staticlive.seeta
 * @Description:
 * @Author wuxinjian
 * @Date 2020/2/3 11:24
 * @Version V1.0
 */
@Data
public class Response implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer result;
	private Integer alive;
	private Double reality;
	private String image;
	private Double clarity;

}