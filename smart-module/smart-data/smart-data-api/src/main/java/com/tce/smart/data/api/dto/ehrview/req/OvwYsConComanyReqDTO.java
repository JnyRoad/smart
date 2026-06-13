package com.tce.smart.data.api.dto.ehrview.req;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/***
 * description: 合同签约单位 <br>
 * date: 2019/11/27 11:36 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Data
public class OvwYsConComanyReqDTO implements Serializable {

    private static final long serialVersionUID = -5176519397769281861L;

    private Integer compId;

    private String compAbbr;

    private String title;

}
