package com.tce.smart.platform.service;

/**
 * oa撤销接口
 * @author fushiping
 * @date 2020/8/3 10:20
 **/
public interface IOARevokeService {

	Boolean revokeProcess(Integer processId, String badege, String status);

}
