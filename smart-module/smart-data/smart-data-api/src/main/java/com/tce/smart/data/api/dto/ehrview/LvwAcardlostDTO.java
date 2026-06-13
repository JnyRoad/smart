package com.tce.smart.data.api.dto.ehrview;

import lombok.Data;

import java.io.Serializable;
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
public class LvwAcardlostDTO implements Serializable {

    private static final long serialVersionUID = -4413462276218063767L;

    private String badge;
    private String name;
    private Integer compid;
    private String compname;
    private Integer depId;
    private String depname;
    private String jobid;
    private String jobname;
    private Date kqStartDate;
    private String kqintime2;
    private String kqouttime2;
    private String kqintime4;
    private String kqouttime4;
    private String kqintime5;
    private String kqouttime5;
    private String reason;
    private String remarks;

}
