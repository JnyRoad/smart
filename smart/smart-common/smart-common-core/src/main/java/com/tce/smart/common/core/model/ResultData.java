package com.tce.smart.common.core.model;

import lombok.Data;

import java.util.List;

/**
 * @Description: TODO
 * @ProjectName smart
 * @ClassName: ResultData
 * @Author jinbo
 * @Date 2019/5/14
 */
@Data
public class ResultData {
    private NowUser NowUser;

    private List<AuthorizeMenu> authorizeMenu;
}
