package com.tce.smart.platform.api.dto.req;

import com.tce.smart.platform.api.dto.SmtFellowVisitorDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 添加随行人员的添加数据
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SaveFellowWechatVisitorReqDTO extends SmtFellowVisitorDTO {
private static final long serialVersionUID = -4349140709422171453L;

   private String fellowPhotoId;
}
