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
@TableName("evw_ECD_LEAINTENT")
public class EvwEcdLeaintent extends Model<EvwEcdLeaintent> {

    private static final long serialVersionUID = 1L;

    @TableField("ID")
    private Integer id;
    private String title;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "EvwEcdLeaintent{" +
        ", id=" + id +
        ", title=" + title +
        "}";
    }
}
