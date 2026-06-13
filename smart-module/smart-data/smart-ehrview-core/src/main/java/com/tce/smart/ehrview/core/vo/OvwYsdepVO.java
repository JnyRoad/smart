package com.tce.smart.ehrview.core.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
public class OvwYsdepVO extends BaseVO {

    private static final long serialVersionUID = 1L;

    private Integer depid;

    private String depname;

    private String depAbbr;

    private Integer compId;

    private String director;

    private String direcName;

    private String depGrade;

    private Integer adminId;

    private String depCost;

}
