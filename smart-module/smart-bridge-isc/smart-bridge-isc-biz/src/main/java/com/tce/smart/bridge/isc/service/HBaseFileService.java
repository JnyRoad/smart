package com.tce.smart.bridge.isc.service;

/**
 * @Description: TODO
 * @InterfaceName HBaseFileService
 * @Author jinbo
 * @Date 2019/12/17
 */
public interface HBaseFileService {

	byte[] getById(String id);

	byte[] getThumbnailById(String id);

	String save(byte[] data, Integer photoType);

	boolean save(String id, byte[] data, Integer photoType);

	boolean update(String id, byte[] data);

	boolean delete(String id);
}
