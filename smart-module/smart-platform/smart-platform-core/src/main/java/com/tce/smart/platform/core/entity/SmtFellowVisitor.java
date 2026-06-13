package com.tce.smart.platform.core.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 随行人员表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:44
 */
@Data
@TableName("smt_fellow_visitor")
@EqualsAndHashCode(callSuper = true)
public class SmtFellowVisitor extends Model<SmtFellowVisitor> {
private static final long serialVersionUID = 1L;

    /**
   *
   */
    @TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   *
   */
    private String fellowName;
    /**
   *
   */
    private String fellowPhotoId;
    /**
   *
   */
    private Long visitorId;

	/**
	 * 证件类型
	 */
    private Integer certType;

	/**
	 * 证件号码
	 */
    private String certNo;

	/**
	 * 证件图片
	 */
    private String certPic;

}
