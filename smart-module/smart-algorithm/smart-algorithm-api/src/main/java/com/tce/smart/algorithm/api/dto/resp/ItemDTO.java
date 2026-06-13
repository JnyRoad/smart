package com.tce.smart.algorithm.api.dto.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName: Item
 * @Package com.tce.smart.yunxun.algorithm.bean.ocr.wentong
 * @Description:
 * @Author wuxinjian
 * @Date 2019-10-11 11:25
 * @Version V1.0
 */
@Data
public class ItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 描述
     */
	@ApiModelProperty("描述")
    private String desc;

    /**
     * 内容
     */
	@ApiModelProperty("内容")
    private String content;

    public static ItemDTO createItem(String desc, String content) {
		ItemDTO itemDTO = new ItemDTO();
		itemDTO.setDesc(desc);
		itemDTO.setContent(content);
		return itemDTO;
	}
}
