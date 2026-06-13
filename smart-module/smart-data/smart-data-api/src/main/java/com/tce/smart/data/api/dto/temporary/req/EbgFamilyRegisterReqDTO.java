package com.tce.smart.data.api.dto.temporary.req;

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
public class EbgFamilyRegisterReqDTO implements Serializable {

    private static final long serialVersionUID = -7297298025802907603L;

    private Integer id;
    private Integer seqid;
    private String fname;
    private Integer relation;
    private Integer gender;
    private Date Birthday;
    private String Company;
    private String Job;
    private Integer status;
    private String remark;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSeqid() {
        return seqid;
    }

    public void setSeqid(Integer seqid) {
        this.seqid = seqid;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public Integer getRelation() {
        return relation;
    }

    public void setRelation(Integer relation) {
        this.relation = relation;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Date getBirthday() {
        return Birthday;
    }

    public void setBirthday(Date Birthday) {
        this.Birthday = Birthday;
    }

    public String getCompany() {
        return Company;
    }

    public void setCompany(String Company) {
        this.Company = Company;
    }

    public String getJob() {
        return Job;
    }

    public void setJob(String Job) {
        this.Job = Job;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "EbgFamilyRegisterReqDTO{" +
        ", id=" + id +
        ", seqid=" + seqid +
        ", fname=" + fname +
        ", relation=" + relation +
        ", gender=" + gender +
        ", Birthday=" + Birthday +
        ", Company=" + Company +
        ", Job=" + Job +
        ", status=" + status +
        ", remark=" + remark +
        "}";
    }
}
