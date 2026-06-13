package com.tce.smart.platform.api.dto.req.news;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author fushiping
 * @date 2022-02-16 18:00:09
 */
@Data
public class NewsInfoFileReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;


	/**
	 * 文件名
	 */
	@ApiModelProperty(value = "文件名")
	@NotBlank(message = "文件名不能为空")
	private String fileName;

	/**
	 * 文件类型
	 */
	@ApiModelProperty(value = "文件后缀")
	@NotBlank(message = "文件后缀不能为空")
	private String fileSuffix;

	/**
	 * 文件大小
	 */
	@ApiModelProperty(value = "文件大小")
	private Float fileSize;


}
