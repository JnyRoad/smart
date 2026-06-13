package com.tce.smart.ehrview.core.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@TableName("lvw_LCD_OTTimeWage")
public class LvwLcdOttimewage extends Model<LvwLcdOttimewage> {

    private static final long serialVersionUID = 1L;

    @TableField("ID")
    private Integer id;
    private String Title;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "LvwLcdOttimewage{" +
        ", id=" + id +
        ", Title=" + Title +
        "}";
    }
}
