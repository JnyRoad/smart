package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@TableName("evw_eCD_Country")
public class EvwEcdCountry extends Model<EvwEcdCountry> {

    private static final long serialVersionUID = 1L;

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
        return "EvwEcdCountry{" +
        ", id=" + id +
        ", title=" + title +
        "}";
    }
}
