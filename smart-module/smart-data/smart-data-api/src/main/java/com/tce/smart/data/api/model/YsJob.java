package com.tce.smart.data.api.model;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
public class YsJob extends Model<YsJob> {

    private static final long serialVersionUID = 1L;

    private String jobid;
    private String jobname;
}
