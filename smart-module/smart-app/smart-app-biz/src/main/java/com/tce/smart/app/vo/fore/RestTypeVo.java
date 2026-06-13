package com.tce.smart.app.vo.fore;

import com.tce.smart.common.core.vo.BaseVO;
import com.tce.smart.platform.api.dto.resp.SearchBreakOffTypeRespDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 调休类型
 * @author ly
 * @date 2019-05-20 16:11:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RestTypeVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 5362758608679031031L;


    /**
     * 请假类型
     */
    private List<SearchBreakOffTypeRespDTO> records;

    private int total;
}
