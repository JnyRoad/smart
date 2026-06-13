package com.tce.smart.common.core.model;

import lombok.Data;

/**
 * @Description: TODO
 * @ProjectName smart
 * @ClassName: AuthorizeMenu
 * @Author jinbo
 * @Date 2019/5/14
 */
@Data
public class AuthorizeMenu {
    private String ModuleId;

    private String ParentId;

    private String EnCode;

    private String FullName;

    private String Icon;

    private String UrlAddress;

    private String Target;

    private int IsMenu;

    private int AllowExpand;

    private int IsPublic;

    private String AllowEdit;

    private String AllowDelete;

    private int SortCode;

    private int DeleteMark;

    private int EnabledMark;

    private String Description;

    private String CreateDate;

    private String CreateUserId;

    private String CreateUserName;

    private String ModifyDate;

    private String ModifyUserId;

    private String ModifyUserName;
}
