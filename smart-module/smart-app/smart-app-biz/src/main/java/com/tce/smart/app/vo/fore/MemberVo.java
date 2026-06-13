package com.tce.smart.app.vo.fore;

import java.util.List;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访客随行详情信息VO
 *
 * @author ly
 * @date 2019-05-10 16:11:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 5362758608679031031L;


    /**
     * 访客的跟随人员信息
     */
    private List<MemberDetailVo> records;

}
