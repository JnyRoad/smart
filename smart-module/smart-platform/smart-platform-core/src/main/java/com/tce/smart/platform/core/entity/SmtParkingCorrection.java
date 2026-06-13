package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;
import java.util.Date;

import javax.validation.constraints.Min;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.date.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 停车场车位校正表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:31:55
 */
@Data
@TableName("smt_parking_correction")
@EqualsAndHashCode(callSuper = true)
public class SmtParkingCorrection extends Model<SmtParkingCorrection> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
   * 园区主键
   */
    private Integer parkId;
    /**
   * 车位总数量
   */
    @Min(value=0,message="车位总数量应该大于等于0")
    private Integer totalCount;
    /**
   * 车位使用数量
   */
    @Min(value=0,message="车位使用数量应该大于等于0")
    private Integer useCount;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;


    private String parkingId;

}
