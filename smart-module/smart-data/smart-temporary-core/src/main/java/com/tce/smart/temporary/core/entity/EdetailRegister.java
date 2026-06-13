package com.tce.smart.temporary.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("edetail_register")
public class EdetailRegister extends Model<EdetailRegister> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;
    private String Mobile;
    private String email;
    @TableField("office_phone")
    private String officePhone;
    private String EmergencyName;
    private Integer Relation;
    private String Telephone;
    private String badge;
    private String name;
    private Integer azstatus;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String Mobile) {
        this.Mobile = Mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public void setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
    }

    public String getEmergencyName() {
        return EmergencyName;
    }

    public void setEmergencyName(String EmergencyName) {
        this.EmergencyName = EmergencyName;
    }

    public Integer getRelation() {
        return Relation;
    }

    public void setRelation(Integer Relation) {
        this.Relation = Relation;
    }

    public String getTelephone() {
        return Telephone;
    }

    public void setTelephone(String Telephone) {
        this.Telephone = Telephone;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAzstatus() {
        return azstatus;
    }

    public void setAzstatus(Integer azstatus) {
        this.azstatus = azstatus;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "EdetailRegister{" +
        ", id=" + id +
        ", Mobile=" + Mobile +
        ", email=" + email +
        ", officePhone=" + officePhone +
        ", EmergencyName=" + EmergencyName +
        ", Relation=" + Relation +
        ", Telephone=" + Telephone +
        ", badge=" + badge +
        ", name=" + name +
        ", azstatus=" + azstatus +
        "}";
    }
}
