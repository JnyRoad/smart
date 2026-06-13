package com.tce.smart.platform.api.dto.req;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 添加随行人员的添加数据
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
@Data
public class AddFellowVisitorReqDTO implements Serializable {
private static final long serialVersionUID = -9038604762225240195L;


	/**
	 * 访客的id
	 */
   private Long visitId;
   /**
    * 随行人员的id
    */
   private List<SaveFellowVisitorReqDTO> followList;
}
