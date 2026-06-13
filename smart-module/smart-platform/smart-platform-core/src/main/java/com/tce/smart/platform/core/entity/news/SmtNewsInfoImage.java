package com.tce.smart.platform.core.entity.news;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 *
 * @author fushiping
 * @date 2022-02-16 18:00:09
 */
@Data
@TableName("smt_news_info_image")
@EqualsAndHashCode(callSuper = true)
public class SmtNewsInfoImage extends Model<SmtNewsInfoImage> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * 信息ID
   */
    private Long infoId;
    /**
   * 图片ID
   */
    private String imageId;
    /**
   * 图片顺序
   */
    private Integer sort;

}
