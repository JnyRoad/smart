package com.tce.smart.app.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.tce.smart.app.dto.AppPictureDto;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author fushiping
 * @date 2019/6/19 08:39
 * APP首页导航菜单vo
 **/
@Data
public class AppNavigationVo extends BaseVO {

	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	private Integer id;
	/**
	 * 模块名称
	 */
	private String moduleName;
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
	 * 模块链接
	 */
	private String moduleUrl;
}
