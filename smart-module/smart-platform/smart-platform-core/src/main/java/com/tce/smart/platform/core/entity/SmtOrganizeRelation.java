package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 外部组织关系关联
 *
 * @author
 * @date 2019-04-15 11:34:58
 */
@Data
@TableName("smt_organize_relation")
@EqualsAndHashCode(callSuper = true)
public class SmtOrganizeRelation extends Model<SmtOrganizeRelation> {

	@TableId(type = IdType.ID_WORKER)
    private Long id;

    private Integer userId ;

    private String compName;

    private Integer parkId;

    private String userName;

    private Integer source;

    private String compId;

/*    @TableLogic
    private Integer isDelete;*/

	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

	/**
	 * 企业类型
	 */
	private Integer compType;

}
