package com.tce.smart.platform.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 人脸比对参数
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/5/10 .
 * @Modified By:
 */
@Data
@NoArgsConstructor
public class FaceCompareDTO {

    private String  base64Face1;

    private String  base64Face2;
}
