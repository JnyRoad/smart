package com.tce.smart.app.dto;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AppModuleInfoDto extends Model<AppModuleInfoDto> {
	/**
	 * ID
	 */
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
	private String catalogCode;
	/**
	 * 模块图标
	 */
	private String moduleIcon;
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
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;
	/**
	 * 修改时间
	 */
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updateTime;
	/**
	 * 模块链接
	 */
	private String moduleUrl;
	/**
	 *编辑用判断
	 */
	private boolean inEdit;
	/**
	 *改名称用判断
	 */
	private boolean nameEditAble;

}
