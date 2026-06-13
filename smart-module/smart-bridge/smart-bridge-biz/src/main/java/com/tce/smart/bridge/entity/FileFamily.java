package com.tce.smart.bridge.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @Description: TODO
 * @ProjectName smart-image
 * @ClassName: ImageFamily
 * @Author jinbo
 * @Date 2019/11/16
 */
@Data
@Component
public class FileFamily extends Family {

	private String data;

	private Integer fileType;
}
