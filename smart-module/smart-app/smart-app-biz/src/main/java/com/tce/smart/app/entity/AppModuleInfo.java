package com.tce.smart.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 园区模块信息表
 *
 * @author fushiping
 * @date 2019/5/21 10:30
 **/

@Data
@TableName("app_module_info")
@EqualsAndHashCode(callSuper = true)
public class AppModuleInfo extends Model<AppModuleInfo> {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId
	private Integer id;
	/**
	 * 模块名称
	 */
	private String moduleName;
	/**
	 * 上级模块
	 */
	private Integer parentModule;
	/**
	 * 分类编码
	 */
	private Integer catalogCode;
	/**
	 * 模块图标
	 */
	private byte[] moduleIcon;
	/**
	 * 模块顺序
	 */
	private Integer moduleOrder;
	/**
	 * 发布状态（0:待发布；1:已发布；2:已下线））
	 */
	private String publishFlag;
	/**
	 * 删除状态（0:删除；1:正常）
	 */
	private String delFlag;
	/**
	 * 创建时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;
	/**
	 * 修改时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updateTime;
	/**
	 * 模块链接
	 */
	private String moduleUrl;

}
