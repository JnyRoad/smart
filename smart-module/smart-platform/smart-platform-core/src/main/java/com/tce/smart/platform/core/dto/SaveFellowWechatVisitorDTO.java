package com.tce.smart.platform.core.dto;

import com.tce.smart.platform.core.entity.SmtFellowVisitor;

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
public class SaveFellowWechatVisitorDTO extends SmtFellowVisitor {
private static final long serialVersionUID = 1L;

   private String fellowPhotoId;
}
