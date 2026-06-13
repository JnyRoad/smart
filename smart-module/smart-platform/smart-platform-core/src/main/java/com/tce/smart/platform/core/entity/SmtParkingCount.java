package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 车位统计表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:30:56
 */
@Data
@TableName("smt_parking_count")
@EqualsAndHashCode(callSuper = true)
public class SmtParkingCount extends Model<SmtParkingCount> {
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
    private Integer totalCount;
    /**
   * 车位使用数量
   */
    private Integer useCount;
    /**
   * 空闲数量
   */
    private Integer freeCount;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

    private String parkingId;

}
