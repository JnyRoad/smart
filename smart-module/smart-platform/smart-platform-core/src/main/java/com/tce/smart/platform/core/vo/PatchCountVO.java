package com.tce.smart.platform.core.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 补卡次数
 * @author liangyuan
 *
 */
@Data
public class PatchCountVO implements Serializable {

	private static final long serialVersionUID = 1L;

    /**
   * 补卡次数
   */
    private String patchCount;
}
