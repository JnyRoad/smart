package com.tce.smart.platform.api.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/5/17 .
 * @Modified By:
 */
@Data
@NoArgsConstructor
public class FaceStorageResultDTO implements Serializable {
    private static final long serialVersionUID = 8432659986689740036L;

    private String esId; //ES id
    private String blobId; //图片数据存储Blob id

    @Builder
    public FaceStorageResultDTO(String esId,String blobId) {
        this.esId = esId;
        this.blobId = blobId;
    }
}
