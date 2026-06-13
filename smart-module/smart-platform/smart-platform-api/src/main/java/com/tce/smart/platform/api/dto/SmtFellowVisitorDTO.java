package com.tce.smart.platform.api.dto;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 随行人员表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:44
 */
@Data
public class SmtFellowVisitorDTO implements Serializable {
private static final long serialVersionUID = 3777685279310868111L;

    /**
   *
   */
    private Long id;
    /**
   *
   */
    private String fellowName;
    /**
   *
   */
    private String fellowPhotoId;
    /**
   *
   */
    private Long visitorId;

}
