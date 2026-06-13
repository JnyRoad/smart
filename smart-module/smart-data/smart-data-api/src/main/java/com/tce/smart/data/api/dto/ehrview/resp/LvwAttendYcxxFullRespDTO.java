package com.tce.smart.data.api.dto.ehrview.resp;

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
public class LvwAttendYcxxFullRespDTO extends BaseVO {

    private static final long serialVersionUID = 8990183352531478531L;

    private Integer id;
    private String badge;
    private String Name;
    private Integer compid;
    private Integer DepID;
    private String DepName;
    private String jobid;
    private Date attenddate;
    private String week;
    private String type;
    private String typeRemark;
    private String shiftid;
    private String in1;
    private String out1;
    private String in2;
    private String out2;
    private String in3;
    private String out3;
    private String remark;
    private String shift;
    private String stdIn2;
    private String stdOt2;
    private String stdIn4;
    private String stdOt4;
    private String stdIn5;
    private String stdOt5;
    private Boolean IsRight;
    private String KqDateStr;

}
