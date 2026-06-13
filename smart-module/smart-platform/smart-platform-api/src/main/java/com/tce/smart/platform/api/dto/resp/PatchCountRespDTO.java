package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * 补卡次数
 * @author liangyuan
 *
 */
@Data
public class PatchCountRespDTO implements Serializable {

	private static final long serialVersionUID = 1237109146455062003L;

    /**
   * 补卡次数
   */
    private String patchCount;
}
