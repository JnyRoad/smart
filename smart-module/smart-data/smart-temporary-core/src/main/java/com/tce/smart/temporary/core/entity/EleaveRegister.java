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
@TableName("eLeave_Register")
public class EleaveRegister extends Model<EleaveRegister> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer type;
    @TableField("EZID")
    private Integer ezid;
    private Integer eid;
    private String badge;
    private String name;
    private Integer compid;
    private Integer depid;
    private String jobid;
    private Date joindate;
    private Date lastleavedate;
    private Date ApplyDate;
    private Date leavedate;
    private Integer leavetype;
    private Integer leavereason;
    private Boolean isEndCon;
    private Boolean isPay;
    private Double PayFee;
    private Boolean IsExpenses;
    private Double ExpenseFee;
    private Boolean Iscompete;
    private Double competeFee;
    private String NewCompany;
    private String NewJob;
    private String NewSalary;
    private Boolean isBlackList;
    private Integer regBy;
    private Date regdate;
    private Boolean initialized;
    private Integer initializedby;
    private Date initializedtime;
    private Boolean submit;
    private Integer submitby;
    private Date submittime;
    private Boolean closed;
    private Integer closedby;
    private Date closedtime;
    private String remark;
    private Integer Seqid;
    @TableField("ISRCK")
    private Boolean isrck;
    private Integer ConCount;
    private Integer contract;
    private Integer ConType;
    private Integer ConProperty;
    private String ConNo;
    private Date ConBeginDate;
    private Integer ConTerm;
    private Date ConEndDate;
    private Integer status;
    private Integer wfinstanceID;
    private Boolean IsRetain;
    private Integer leaintent;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getEzid() {
        return ezid;
    }

    public void setEzid(Integer ezid) {
        this.ezid = ezid;
    }

    public Integer getEid() {
        return eid;
    }

    public void setEid(Integer eid) {
        this.eid = eid;
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

    public Integer getCompid() {
        return compid;
    }

    public void setCompid(Integer compid) {
        this.compid = compid;
    }

    public Integer getDepid() {
        return depid;
    }

    public void setDepid(Integer depid) {
        this.depid = depid;
    }

    public String getJobid() {
        return jobid;
    }

    public void setJobid(String jobid) {
        this.jobid = jobid;
    }

    public Date getJoindate() {
        return joindate;
    }

    public void setJoindate(Date joindate) {
        this.joindate = joindate;
    }

    public Date getLastleavedate() {
        return lastleavedate;
    }

    public void setLastleavedate(Date lastleavedate) {
        this.lastleavedate = lastleavedate;
    }

    public Date getApplyDate() {
        return ApplyDate;
    }

    public void setApplyDate(Date ApplyDate) {
        this.ApplyDate = ApplyDate;
    }

    public Date getLeavedate() {
        return leavedate;
    }

    public void setLeavedate(Date leavedate) {
        this.leavedate = leavedate;
    }

    public Integer getLeavetype() {
        return leavetype;
    }

    public void setLeavetype(Integer leavetype) {
        this.leavetype = leavetype;
    }

    public Integer getLeavereason() {
        return leavereason;
    }

    public void setLeavereason(Integer leavereason) {
        this.leavereason = leavereason;
    }

    public Boolean getEndCon() {
        return isEndCon;
    }

    public void setEndCon(Boolean isEndCon) {
        this.isEndCon = isEndCon;
    }

    public Boolean getPay() {
        return isPay;
    }

    public void setPay(Boolean isPay) {
        this.isPay = isPay;
    }

    public Double getPayFee() {
        return PayFee;
    }

    public void setPayFee(Double PayFee) {
        this.PayFee = PayFee;
    }

    public Boolean getIsExpenses() {
        return IsExpenses;
    }

    public void setIsExpenses(Boolean IsExpenses) {
        this.IsExpenses = IsExpenses;
    }

    public Double getExpenseFee() {
        return ExpenseFee;
    }

    public void setExpenseFee(Double ExpenseFee) {
        this.ExpenseFee = ExpenseFee;
    }

    public Boolean getIscompete() {
        return Iscompete;
    }

    public void setIscompete(Boolean Iscompete) {
        this.Iscompete = Iscompete;
    }

    public Double getCompeteFee() {
        return competeFee;
    }

    public void setCompeteFee(Double competeFee) {
        this.competeFee = competeFee;
    }

    public String getNewCompany() {
        return NewCompany;
    }

    public void setNewCompany(String NewCompany) {
        this.NewCompany = NewCompany;
    }

    public String getNewJob() {
        return NewJob;
    }

    public void setNewJob(String NewJob) {
        this.NewJob = NewJob;
    }

    public String getNewSalary() {
        return NewSalary;
    }

    public void setNewSalary(String NewSalary) {
        this.NewSalary = NewSalary;
    }

    public Boolean getBlackList() {
        return isBlackList;
    }

    public void setBlackList(Boolean isBlackList) {
        this.isBlackList = isBlackList;
    }

    public Integer getRegBy() {
        return regBy;
    }

    public void setRegBy(Integer regBy) {
        this.regBy = regBy;
    }

    public Date getRegdate() {
        return regdate;
    }

    public void setRegdate(Date regdate) {
        this.regdate = regdate;
    }

    public Boolean getInitialized() {
        return initialized;
    }

    public void setInitialized(Boolean initialized) {
        this.initialized = initialized;
    }

    public Integer getInitializedby() {
        return initializedby;
    }

    public void setInitializedby(Integer initializedby) {
        this.initializedby = initializedby;
    }

    public Date getInitializedtime() {
        return initializedtime;
    }

    public void setInitializedtime(Date initializedtime) {
        this.initializedtime = initializedtime;
    }

    public Boolean getSubmit() {
        return submit;
    }

    public void setSubmit(Boolean submit) {
        this.submit = submit;
    }

    public Integer getSubmitby() {
        return submitby;
    }

    public void setSubmitby(Integer submitby) {
        this.submitby = submitby;
    }

    public Date getSubmittime() {
        return submittime;
    }

    public void setSubmittime(Date submittime) {
        this.submittime = submittime;
    }

    public Boolean getClosed() {
        return closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    public Integer getClosedby() {
        return closedby;
    }

    public void setClosedby(Integer closedby) {
        this.closedby = closedby;
    }

    public Date getClosedtime() {
        return closedtime;
    }

    public void setClosedtime(Date closedtime) {
        this.closedtime = closedtime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getSeqid() {
        return Seqid;
    }

    public void setSeqid(Integer Seqid) {
        this.Seqid = Seqid;
    }

    public Boolean getIsrck() {
        return isrck;
    }

    public void setIsrck(Boolean isrck) {
        this.isrck = isrck;
    }

    public Integer getConCount() {
        return ConCount;
    }

    public void setConCount(Integer ConCount) {
        this.ConCount = ConCount;
    }

    public Integer getContract() {
        return contract;
    }

    public void setContract(Integer contract) {
        this.contract = contract;
    }

    public Integer getConType() {
        return ConType;
    }

    public void setConType(Integer ConType) {
        this.ConType = ConType;
    }

    public Integer getConProperty() {
        return ConProperty;
    }

    public void setConProperty(Integer ConProperty) {
        this.ConProperty = ConProperty;
    }

    public String getConNo() {
        return ConNo;
    }

    public void setConNo(String ConNo) {
        this.ConNo = ConNo;
    }

    public Date getConBeginDate() {
        return ConBeginDate;
    }

    public void setConBeginDate(Date ConBeginDate) {
        this.ConBeginDate = ConBeginDate;
    }

    public Integer getConTerm() {
        return ConTerm;
    }

    public void setConTerm(Integer ConTerm) {
        this.ConTerm = ConTerm;
    }

    public Date getConEndDate() {
        return ConEndDate;
    }

    public void setConEndDate(Date ConEndDate) {
        this.ConEndDate = ConEndDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getWfinstanceID() {
        return wfinstanceID;
    }

    public void setWfinstanceID(Integer wfinstanceID) {
        this.wfinstanceID = wfinstanceID;
    }

    public Boolean getIsRetain() {
        return IsRetain;
    }

    public void setIsRetain(Boolean IsRetain) {
        this.IsRetain = IsRetain;
    }

    public Integer getLeaintent() {
        return leaintent;
    }

    public void setLeaintent(Integer leaintent) {
        this.leaintent = leaintent;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "EleaveRegister{" +
        ", id=" + id +
        ", type=" + type +
        ", ezid=" + ezid +
        ", eid=" + eid +
        ", badge=" + badge +
        ", name=" + name +
        ", compid=" + compid +
        ", depid=" + depid +
        ", jobid=" + jobid +
        ", joindate=" + joindate +
        ", lastleavedate=" + lastleavedate +
        ", ApplyDate=" + ApplyDate +
        ", leavedate=" + leavedate +
        ", leavetype=" + leavetype +
        ", leavereason=" + leavereason +
        ", isEndCon=" + isEndCon +
        ", isPay=" + isPay +
        ", PayFee=" + PayFee +
        ", IsExpenses=" + IsExpenses +
        ", ExpenseFee=" + ExpenseFee +
        ", Iscompete=" + Iscompete +
        ", competeFee=" + competeFee +
        ", NewCompany=" + NewCompany +
        ", NewJob=" + NewJob +
        ", NewSalary=" + NewSalary +
        ", isBlackList=" + isBlackList +
        ", regBy=" + regBy +
        ", regdate=" + regdate +
        ", initialized=" + initialized +
        ", initializedby=" + initializedby +
        ", initializedtime=" + initializedtime +
        ", submit=" + submit +
        ", submitby=" + submitby +
        ", submittime=" + submittime +
        ", closed=" + closed +
        ", closedby=" + closedby +
        ", closedtime=" + closedtime +
        ", remark=" + remark +
        ", Seqid=" + Seqid +
        ", isrck=" + isrck +
        ", ConCount=" + ConCount +
        ", contract=" + contract +
        ", ConType=" + ConType +
        ", ConProperty=" + ConProperty +
        ", ConNo=" + ConNo +
        ", ConBeginDate=" + ConBeginDate +
        ", ConTerm=" + ConTerm +
        ", ConEndDate=" + ConEndDate +
        ", status=" + status +
        ", wfinstanceID=" + wfinstanceID +
        ", IsRetain=" + IsRetain +
        ", leaintent=" + leaintent +
        "}";
    }
}
