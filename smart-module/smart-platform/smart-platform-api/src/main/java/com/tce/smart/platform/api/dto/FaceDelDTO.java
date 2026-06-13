package com.tce.smart.platform.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 车辆卡片删除信息
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/4/18 .
 * @Modified By:
 */
@Data
@NoArgsConstructor
public class FaceDelDTO implements Serializable {

    private static final long serialVersionUID = -7702777798697317761L;
    /**
     * 园区id
     */
    private Integer parkId;

    /**
     * 人员id
     */
    private Long personId;

    /**
     * 人员类型(员工、访客)
     */
    private Integer type;


}
