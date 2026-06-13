package com.tce.smart.platform.core.vo;
import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

/**
 * 查询访客分析数据
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:20
 */
@Data
public class SearchVisitorAnalysisVO extends BaseVO{

	private static final long serialVersionUID = 1L;


    private Integer causeCount;
    private String causeDesc;

}
