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
@TableName("LdxRegLeave_register")
public class LdxregleaveRegister extends Model<LdxregleaveRegister> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;
    private Integer ezid;
    private Date Term;
    private String Badge;
    @TableField("EID")
    private Integer eid;
    private String Name;
    private Integer compid;
    private Integer DepID;
    private String jobid;
    @TableField("TWID")
    private Integer twid;
    private Date oldBeginTime;
    private Date oldEndTime;
    private Double oldAmount;
    private Date BeginTime;
    private Date EndTime;
    private Double Amount;
    private Integer Unit;
    private Integer regBy;
    private Date regdate;
    private Boolean Initialized;
    private Integer InitializedBy;
    private Date InitializedTime;
    private Boolean Submit;
    private Integer SubmitBy;
    private Date SubmitTime;
    private Boolean Closed;
    private Integer ClosedBy;
    private Date ClosedTime;
    private String Remark;
    private Integer SeqID;
    private Date marrydate;
    private String DayoffReason;
    private Boolean IsDispose;
    private String ErrMsg;
    private Date DataCreateDay;
    private Integer sqlid;
    private Double Period;
    private Boolean IsTiaoXiu;
    private Integer TermID;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEzid() {
        return ezid;
    }

    public void setEzid(Integer ezid) {
        this.ezid = ezid;
    }

    public Date getTerm() {
        return Term;
    }

    public void setTerm(Date Term) {
        this.Term = Term;
    }

    public String getBadge() {
        return Badge;
    }

    public void setBadge(String Badge) {
        this.Badge = Badge;
    }

    public Integer getEid() {
        return eid;
    }

    public void setEid(Integer eid) {
        this.eid = eid;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public Integer getCompid() {
        return compid;
    }

    public void setCompid(Integer compid) {
        this.compid = compid;
    }

    public Integer getDepID() {
        return DepID;
    }

    public void setDepID(Integer DepID) {
        this.DepID = DepID;
    }

    public String getJobid() {
        return jobid;
    }

    public void setJobid(String jobid) {
        this.jobid = jobid;
    }

    public Integer getTwid() {
        return twid;
    }

    public void setTwid(Integer twid) {
        this.twid = twid;
    }

    public Date getOldBeginTime() {
        return oldBeginTime;
    }

    public void setOldBeginTime(Date oldBeginTime) {
        this.oldBeginTime = oldBeginTime;
    }

    public Date getOldEndTime() {
        return oldEndTime;
    }

    public void setOldEndTime(Date oldEndTime) {
        this.oldEndTime = oldEndTime;
    }

    public Double getOldAmount() {
        return oldAmount;
    }

    public void setOldAmount(Double oldAmount) {
        this.oldAmount = oldAmount;
    }

    public Date getBeginTime() {
        return BeginTime;
    }

    public void setBeginTime(Date BeginTime) {
        this.BeginTime = BeginTime;
    }

    public Date getEndTime() {
        return EndTime;
    }

    public void setEndTime(Date EndTime) {
        this.EndTime = EndTime;
    }

    public Double getAmount() {
        return Amount;
    }

    public void setAmount(Double Amount) {
        this.Amount = Amount;
    }

    public Integer getUnit() {
        return Unit;
    }

    public void setUnit(Integer Unit) {
        this.Unit = Unit;
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
        return Initialized;
    }

    public void setInitialized(Boolean Initialized) {
        this.Initialized = Initialized;
    }

    public Integer getInitializedBy() {
        return InitializedBy;
    }

    public void setInitializedBy(Integer InitializedBy) {
        this.InitializedBy = InitializedBy;
    }

    public Date getInitializedTime() {
        return InitializedTime;
    }

    public void setInitializedTime(Date InitializedTime) {
        this.InitializedTime = InitializedTime;
    }

    public Boolean getSubmit() {
        return Submit;
    }

    public void setSubmit(Boolean Submit) {
        this.Submit = Submit;
    }

    public Integer getSubmitBy() {
        return SubmitBy;
    }

    public void setSubmitBy(Integer SubmitBy) {
        this.SubmitBy = SubmitBy;
    }

    public Date getSubmitTime() {
        return SubmitTime;
    }

    public void setSubmitTime(Date SubmitTime) {
        this.SubmitTime = SubmitTime;
    }

    public Boolean getClosed() {
        return Closed;
    }

    public void setClosed(Boolean Closed) {
        this.Closed = Closed;
    }

    public Integer getClosedBy() {
        return ClosedBy;
    }

    public void setClosedBy(Integer ClosedBy) {
        this.ClosedBy = ClosedBy;
    }

    public Date getClosedTime() {
        return ClosedTime;
    }

    public void setClosedTime(Date ClosedTime) {
        this.ClosedTime = ClosedTime;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String Remark) {
        this.Remark = Remark;
    }

    public Integer getSeqID() {
        return SeqID;
    }

    public void setSeqID(Integer SeqID) {
        this.SeqID = SeqID;
    }

    public Date getMarrydate() {
        return marrydate;
    }

    public void setMarrydate(Date marrydate) {
        this.marrydate = marrydate;
    }

    public String getDayoffReason() {
        return DayoffReason;
    }

    public void setDayoffReason(String DayoffReason) {
        this.DayoffReason = DayoffReason;
    }

    public Boolean getIsDispose() {
        return IsDispose;
    }

    public void setIsDispose(Boolean IsDispose) {
        this.IsDispose = IsDispose;
    }

    public String getErrMsg() {
        return ErrMsg;
    }

    public void setErrMsg(String ErrMsg) {
        this.ErrMsg = ErrMsg;
    }

    public Date getDataCreateDay() {
        return DataCreateDay;
    }

    public void setDataCreateDay(Date DataCreateDay) {
        this.DataCreateDay = DataCreateDay;
    }

    public Integer getSqlid() {
        return sqlid;
    }

    public void setSqlid(Integer sqlid) {
        this.sqlid = sqlid;
    }

    public Double getPeriod() {
        return Period;
    }

    public void setPeriod(Double Period) {
        this.Period = Period;
    }

    public Boolean getIsTiaoXiu() {
        return IsTiaoXiu;
    }

    public void setIsTiaoXiu(Boolean IsTiaoXiu) {
        this.IsTiaoXiu = IsTiaoXiu;
    }

    public Integer getTermID() {
        return TermID;
    }

    public void setTermID(Integer TermID) {
        this.TermID = TermID;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "LdxregleaveRegister{" +
        ", id=" + id +
        ", ezid=" + ezid +
        ", Term=" + Term +
        ", Badge=" + Badge +
        ", eid=" + eid +
        ", Name=" + Name +
        ", compid=" + compid +
        ", DepID=" + DepID +
        ", jobid=" + jobid +
        ", twid=" + twid +
        ", oldBeginTime=" + oldBeginTime +
        ", oldEndTime=" + oldEndTime +
        ", oldAmount=" + oldAmount +
        ", BeginTime=" + BeginTime +
        ", EndTime=" + EndTime +
        ", Amount=" + Amount +
        ", Unit=" + Unit +
        ", regBy=" + regBy +
        ", regdate=" + regdate +
        ", Initialized=" + Initialized +
        ", InitializedBy=" + InitializedBy +
        ", InitializedTime=" + InitializedTime +
        ", Submit=" + Submit +
        ", SubmitBy=" + SubmitBy +
        ", SubmitTime=" + SubmitTime +
        ", Closed=" + Closed +
        ", ClosedBy=" + ClosedBy +
        ", ClosedTime=" + ClosedTime +
        ", Remark=" + Remark +
        ", SeqID=" + SeqID +
        ", marrydate=" + marrydate +
        ", DayoffReason=" + DayoffReason +
        ", IsDispose=" + IsDispose +
        ", ErrMsg=" + ErrMsg +
        ", DataCreateDay=" + DataCreateDay +
        ", sqlid=" + sqlid +
        ", Period=" + Period +
        ", IsTiaoXiu=" + IsTiaoXiu +
        ", TermID=" + TermID +
        "}";
    }
}
