package com.tce.smart.temporary.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

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
@TableName("eBG_Working_register")
public class EbgWorkingRegister extends Model<EbgWorkingRegister> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("seqID")
    private Integer seqID;
    private Date begindate;
    private Date enddate;
    private String company;
    private String job;
    private String workplace;
    private String Reference;
    private String Tel;
    private Boolean isout;
    private String remark;
    private Integer Type;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSeqID() {
        return seqID;
    }

    public void setSeqID(Integer seqID) {
        this.seqID = seqID;
    }

    public Date getBegindate() {
        return begindate;
    }

    public void setBegindate(Date begindate) {
        this.begindate = begindate;
    }

    public Date getEnddate() {
        return enddate;
    }

    public void setEnddate(Date enddate) {
        this.enddate = enddate;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getWorkplace() {
        return workplace;
    }

    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }

    public String getReference() {
        return Reference;
    }

    public void setReference(String Reference) {
        this.Reference = Reference;
    }

    public String getTel() {
        return Tel;
    }

    public void setTel(String Tel) {
        this.Tel = Tel;
    }

    public Boolean getIsout() {
        return isout;
    }

    public void setIsout(Boolean isout) {
        this.isout = isout;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getType() {
        return Type;
    }

    public void setType(Integer Type) {
        this.Type = Type;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "EbgWorkingRegister{" +
        ", id=" + id +
        ", seqID=" + seqID +
        ", begindate=" + begindate +
        ", enddate=" + enddate +
        ", company=" + company +
        ", job=" + job +
        ", workplace=" + workplace +
        ", Reference=" + Reference +
        ", Tel=" + Tel +
        ", isout=" + isout +
        ", remark=" + remark +
        ", Type=" + Type +
        "}";
    }
}
