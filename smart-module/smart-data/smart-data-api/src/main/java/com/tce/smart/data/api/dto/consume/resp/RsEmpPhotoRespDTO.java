package com.tce.smart.data.api.dto.consume.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人员照片表
 *
 * @author mkwu
 * @date 2019-07-29
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class RsEmpPhotoRespDTO extends BaseVO {

    /**
	 * 序列号
	 */
	private static final long serialVersionUID = 292538130837443786L;

	private String GUID;

    private String EmpSysID;

    private byte[] EmpPhotoImage;

    private String EmpPhotoPath;

    private String EmpPhotoType;

    private String EmpBarcodePath;
}
