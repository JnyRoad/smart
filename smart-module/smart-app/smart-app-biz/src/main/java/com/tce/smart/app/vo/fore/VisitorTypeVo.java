package com.tce.smart.app.vo.fore;

import java.util.List;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访客来访事由
 *
 * @author ly
 * @date 2019-05-10 16:11:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VisitorTypeVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 5362758608679031031L;


    /**
     * 访客的来访事由信息
     */
    private List<VisitorTypeDetailVo> records;
    /**
     * 条数
     */
    private Integer total;

}
