package com.tce.smart.dispatcher.service;

import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;

/**
 * @Description: TODO
 * @InterfaceName DispatcherService
 * @Author jinbo
 * @Date 2019/11/6
 */
public interface DispatcherService {

	<T> String dispatch(DispatcherDTO<T> dispatcherDTO);

	String getImage(Integer parkId, String id);

	String getThumbnail(Integer parkId, String id);

//	String saveImage(Integer parkId, String base64Image);
//
//	String saveThumbnail(Integer parkId, String base64Image);
}
