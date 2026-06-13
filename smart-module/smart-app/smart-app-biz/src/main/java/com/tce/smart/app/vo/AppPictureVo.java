package com.tce.smart.app.vo;

import com.tce.smart.app.dto.AppPictureDto;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.util.List;
@Data
public class AppPictureVo extends BaseVO {
	private static final long serialVersionUID = 1L;

	private List<AppPictureDto>  picture;
}
