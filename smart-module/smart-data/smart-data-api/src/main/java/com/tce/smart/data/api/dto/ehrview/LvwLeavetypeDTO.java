package com.tce.smart.data.api.dto.ehrview;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
public class LvwLeavetypeDTO implements Serializable {

    private static final long serialVersionUID = -1123836792959090265L;

    private Integer id;
    private String title;
    private String remark;


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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "LvwLeavetypeDTO{" +
        ", id=" + id +
        ", title=" + title +
        ", remark=" + remark +
        "}";
    }
}
