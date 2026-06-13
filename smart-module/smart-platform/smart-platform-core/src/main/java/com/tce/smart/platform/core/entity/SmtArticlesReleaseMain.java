package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * (SmtArticlesReleaseMain)实体类
 *
 * @author sunfujian
 * @date 2021-08-17 14:23:40
 */
@Data
@TableName("SMT_ARTICLES_RELEASE_MAIN")
@EqualsAndHashCode(callSuper = true)
public class SmtArticlesReleaseMain extends Model<SmtArticlesReleaseMain> {
	private static final long serialVersionUID = 1L;
	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
	/**
	 * 物品放行ID
	 */
	private Long releaseId;
	/**
	 * 申请人级别
	 */
	private String sqrjb;
	/**
	 * 附件上传
	 */
	private String fjsc;
	/**
	 * 出发地点
	 */
	private String fxdd;
	/**
	 * 到达地点
	 */
	private String dddd;
	/**
	 * 出发地点详情
	 */
	private String fxddxq;
	/**
	 * 到达地点详情
	 */
	private String ddddxq;
	/**
	 * 物品放行类别
	 */
	private String wpfxlb;
	/**
	 * 是否返厂
	 */
	private String sffc;
	/**
	 * 流程编号
	 */
	private String lcbh;
	/**
	 * 申请人(OA人员编号)
	 */
	private String sqr;
	/**
	 * 申请人(OA部门编号)
	 */
	private String sqbm;
	/**
	 * 放行事项
	 */
	private String fxsx;
	/**
	 * 人员放行(与物品放行二选一)
	 */
	private String ryfx;
	/**
	 * 人员放行详情
	 */
	private String ryfxxq;
	/**
	 * 物品放行
	 */
	private String wpfx;
	/**
	 * 物品放行详情
	 */
	private String wpfxxq;
	/**
	 * 放行去处
	 */
	private String fxqc;
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

}
