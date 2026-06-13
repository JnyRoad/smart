package com.tce.smart.bridge.isc.service;

import org.apache.hadoop.hbase.client.*;
import com.tce.smart.bridge.isc.entity.Table;

import java.util.List;

public interface HBaseService {

	/**
	 * 创建表
	 *
	 * @param table 表
	 * @return boolean 是否创建成功
	 */
	boolean createTable(Class<? extends Table> table);

	/**
	 * 创建表
	 *
	 * @param tableName 表名
	 * @param families  列族名
	 * @return boolean 是否创建成功
	 */
	boolean createTable(String tableName, List<String> families);

	/**
	 * 查询库中所有表的表名
	 *
	 * @return 所有表的表名
	 */
	List<String> getTables();

	/**
	 * 判断表名是否存在
	 *
	 * @param tableName 表名 String ,注意这里区分大小写
	 * @return
	 */
	boolean tableExists(String tableName);

	/**
	 * 通过表名和rowKey获取数据,获取一条数据
	 *
	 * @param tableName 表名
	 * @param rowKey    rowKey
	 * @return Result 类型
	 */
	Result get(String tableName, String rowKey);

	/**
	 * 通过对象(只传rowKey)获取数据,获取一条数据
	 *
	 * @param model
	 * @return Result 类型
	 */
	<T extends Table> Result get(T model);

	/**
	 * 通过对象(只传rowKey)获取数据,获取一条数据
	 *
	 * @param model
	 * @return Result 类型
	 */
	<T extends Table> T query(T model);

	/**
	 * Get查询
	 *
	 * @param tableName 表名
	 * @param getList   请求体
	 * @return Result类型
	 */
	Result[] get(String tableName, List<Get> getList);

	/**
	 * 新增一条数据
	 *
	 * @param model 目标数据
	 * @return
	 */
	<T extends Table> boolean put(T model);

	/**
	 * 新增一条数据
	 *
	 * @param tableName    目标数据表
	 * @param rowKey       rowKey
	 * @param columnFamily 列族名
	 * @param qualifier    列名
	 * @param data         字节数组类型的数据
	 * @return
	 */
	boolean put(String tableName, String rowKey, String columnFamily, String qualifier, byte[] data);

	/**
	 * 批量插入数据
	 *
	 * @param tableName 表名
	 * @param putList   put集合
	 * @return
	 */
	boolean putBatch(String tableName, List<Put> putList);

	/**
	 * 删除一个列族下的数据
	 *
	 * @param model    目标数据表
	 */
	<T extends Table> boolean delete(T model);
	/**
	 * 删除一个列族下的数据
	 *
	 * @param tableName    目标数据表
	 * @param rowKey       rowKey
	 * @param columnFamily 列族名
	 */
	void delete(String tableName, String rowKey, String columnFamily);

	/**
	 * 删除列下的某个数据
	 *
	 * @param tableName    目标数据表
	 * @param rowKey       rowKey
	 * @param columnFamily 列族名
	 * @param qualifier    列名
	 * @return
	 */
	boolean delete(String tableName, String rowKey, String columnFamily, String qualifier);

	/**
	 * 批量删除数据
	 *
	 * @param tableName  表名
	 * @param deleteList 需要删除的数据
	 * @return
	 */
	boolean deleteBatch(String tableName, List<Delete> deleteList);

	/**
	 * 通过scan查询数据
	 *
	 * @param tableName 表名
	 * @param scan      scan
	 * @return 返回 ResultScanner
	 */
	ResultScanner scan(String tableName, Scan scan);

	/**
	 * 删除表
	 *
	 * @param table
	 * @return
	 */
	boolean dropTable(Class<? extends Table> table);

	/**
	 * 删除表
	 *
	 * @param tableName 表名称
	 * @return
	 */
	boolean dropTable(String tableName);
}
