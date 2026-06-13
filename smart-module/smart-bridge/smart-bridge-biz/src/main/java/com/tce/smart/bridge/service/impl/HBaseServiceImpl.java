package com.tce.smart.bridge.service.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.json.JSONUtil;
import com.tce.smart.bridge.util.BeanUtils;
import com.tce.smart.bridge.util.ReflectUtils;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.bridge.service.HBaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: TODO
 * @ProjectName smart-image
 * @ClassName: HBaseUtils
 * @Author jinbo
 * @Date 2019/12/10
 */
@Slf4j
@Service
public class HBaseServiceImpl implements HBaseService, InitializingBean, ApplicationContextAware {

	@Value("${smart.image.expire:-1}")
	private float expire;

	/**
	 * 声明静态配置
	 */
	@Autowired
	private Connection connection;

	@Autowired
	private HBaseAdmin hbaseAdmin;

	@Override
	public boolean createTable(Class<? extends com.tce.smart.bridge.entity.Table> table) {
		return createTable(table(table), family(table));
	}

	/**
	 * 创建表
	 *
	 * @param tableName      表名
	 * @param columnFamilies 列族名
	 * @return boolean 是否创建成功
	 */
	@Override
	public boolean createTable(String tableName, List<String> columnFamilies) {
		try {
			if (hbaseAdmin.tableExists(TableName.valueOf(tableName))) {
				log.warn("数据表已存在，tableName：{}", tableName);
				return false;
			}
			HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
			final int ttl = expire > 0 ? 2147483647 : (int) (expire * 24 * 60 * 60);
			columnFamilies.forEach(columnFamily -> tableDescriptor.addFamily(new HColumnDescriptor(columnFamily).setTimeToLive(ttl)));
			hbaseAdmin.createTable(tableDescriptor);
			log.info("创建数据表成功，tableName：{}", tableName);
			return true;
		} catch (IOException e) {
			log.error("创建数据表失败，tableName：{}，异常：{}", tableName, e.getMessage(), e);
		}
		return false;
	}

	/**
	 * 获取table
	 *
	 * @param tableName 表名
	 * @return Table
	 * @throws IOException IOException
	 */
	private Table getTable(String tableName) throws IOException {
		return connection.getTable(TableName.valueOf(tableName));
	}

	/**
	 * 查询库中所有表的表名
	 *
	 * @return 所有表的表名
	 */
	@Override
	public List<String> getTables() {
		try {
			TableName[] tableNames = hbaseAdmin.listTableNames();
			return Arrays.stream(tableNames).map(TableName::getNameAsString)
					.collect(Collectors.toList());
		} catch (IOException e) {
			log.error("获取所有表的表名失败", e);
		}
		return null;
	}

	/**
	 * 判断表名是否存在
	 *
	 * @param tableName 表名 String ,注意这里区分大小写
	 * @return
	 */
	@Override
	public boolean tableExists(String tableName) {
		try {
			return hbaseAdmin.tableExists(tableName);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * 通过表名和rowKey获取数据,获取一条数据
	 *
	 * @param tableName 表名
	 * @param rowKey    rowKey
	 * @return Result 类型
	 */
	@Override
	public Result get(String tableName, String rowKey) {
		Table table = null;
		try {
			if (!hbaseAdmin.tableExists(tableName)) {
				log.warn("数据表不存在，tableName：{}", tableName);
				return null;
			}
			table = connection.getTable(TableName.valueOf(tableName));
			Get get = new Get(Bytes.toBytes(rowKey));
			return table.get(get);
		} catch (IOException e) {
			log.error("查询数据失败，tableName={}，rowKey={}，异常：{}", tableName, rowKey, e.getMessage(), e);
		} finally {
			close(table);
		}
		return null;
	}

	/**
	 * 通过对象(只传rowKey)获取数据,获取一条数据
	 *
	 * @param model
	 * @return Result 类型
	 */
	@Override
	public <T extends com.tce.smart.bridge.entity.Table> Result get(T model) {
		String tableName = table(model.getClass());
		Table table = null;
		try {
			if (!hbaseAdmin.tableExists(tableName)) {
				log.warn("数据表不存在，tableName：{}", tableName);
				return null;
			}
			table = connection.getTable(TableName.valueOf(tableName));
			Get get = new Get(Bytes.toBytes(model.getKey()));
			return table.get(get);
		} catch (IOException e) {
			log.error("查询数据失败，tableName={}，rowKey={}，异常：{}", tableName, model.getKey(), e.getMessage(), e);
		} finally {
			close(table);
		}
		return null;
	}

	/**
	 * 通过对象(只传rowKey)获取数据,获取一条数据
	 *
	 * @param model
	 * @return Result 类型
	 */
	@Override
	public <T extends com.tce.smart.bridge.entity.Table> T query(T model) {
		String tableName = table(model.getClass());
		Table table = null;
		try {
			if (!hbaseAdmin.tableExists(tableName)) {
				log.warn("数据表不存在，tableName：{}", tableName);
				return null;
			}
			table = connection.getTable(TableName.valueOf(tableName));
			Get get = new Get(Bytes.toBytes(model.getKey()));
			Class<T> target = (Class<T>) model.getClass();
			return toBean(table.get(get), target);
		} catch (IOException e) {
			log.error("查询数据失败，tableName={}，rowKey={}，异常：{}", tableName, model.getKey(), e.getMessage(), e);
		} finally {
			close(table);
		}
		return null;
	}

	/**
	 * 自定义查询
	 *
	 * @param tableName 表名
	 * @param getList   请求体
	 * @return Result类型
	 */
	@Override
	public Result[] get(String tableName, List<Get> getList) {
		Table table = null;
		Result[] result = null;
		try {
			if (!hbaseAdmin.tableExists(tableName)) {
				log.warn("数据表不存在，tableName：{}", tableName);
				return null;
			}
			table = connection.getTable(TableName.valueOf(tableName));
			result = table.get(getList);
		} catch (IOException e) {
			log.error("GET查询数据失败，tableName={}，异常：{}", tableName, e.getMessage(), e);
		} finally {
			close(table);
		}
		return result;
	}

	/**
	 * 关闭连接
	 *
	 * @param table 表名
	 */
	private void close(Table table) {
		try {
			if (table != null) {
				table.close();
			}
		} catch (Exception e) {
			log.error("close table {} error {}", table.getName(), e.getMessage());
		}
	}

	/**
	 * 新增一条数据
	 *
	 * @param model 目标数据
	 * @return
	 */
	@Override
	public <T extends com.tce.smart.bridge.entity.Table> boolean put(T model) {
		String tableName = table(model.getClass());
		String rowKey = model.getKey();
		Map<String, ? extends com.tce.smart.bridge.entity.Family> columnFamilies = family(model);
		Table table = null;
		try {
			if (!hbaseAdmin.tableExists(tableName)) {
				log.warn("数据表不存在，tableName：{}", tableName);
				return false;
			}
			if (CollectionUtils.isEmpty(columnFamilies)) {
				log.error("未定义列族，请添加注解：Family，tableName：{}", tableName);
				return false;
			}
			table = connection.getTable(TableName.valueOf(tableName));
			Put put = new Put(Bytes.toBytes(rowKey));
			columnFamilies.forEach((columnFamily, data) -> {
				Map<String, Object> qualifiers = qualifier(data);
				qualifiers.forEach((qualifier, value) ->
						put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(qualifier), Bytes.toBytes(String.valueOf(value))));
			});
			table.put(put);
			log.info("写入数据成功：tableName={}，rowKey={}", tableName, rowKey);
			return true;
		} catch (IOException e) {
			log.error("PUT写入数据失败，tableName={}，异常：{}", tableName, e.getMessage(), e);
		} finally {
			close(table);
		}
		return false;
	}

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
	@Override
	public boolean put(String tableName, String rowKey, String columnFamily, String qualifier, byte[] data) {

		Table table = null;
		try {
			if (!hbaseAdmin.tableExists(tableName)) {
				log.warn("数据表不存在，tableName：{}", tableName);
				return false;
			}
			table = connection.getTable(TableName.valueOf(tableName));
			Put put = new Put(Bytes.toBytes(rowKey));
			put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(qualifier), data);
			table.put(put);
			log.info("写入数据成功：tableName={}，rowKey={}", tableName, rowKey);
			return true;
		} catch (IOException e) {
			log.error("PUT写入数据失败，tableName={}，异常：{}", tableName, e.getMessage(), e);
		} finally {
			close(table);
		}
		return false;
	}

	/**
	 * 批量插入数据
	 *
	 * @param tableName 表名
	 * @param putList   put集合
	 * @return
	 */
	@Override
	public boolean putBatch(String tableName, List<Put> putList) {

		Table table = null;
		try {
			if (!hbaseAdmin.tableExists(tableName)) {
				log.warn("数据表不存在，tableName：{}", tableName);
				return false;
			}
			table = connection.getTable(TableName.valueOf(tableName));
			table.put(putList);
			log.info("批量写入数据成功：tableName={}", tableName);
			return true;
		} catch (IOException e) {
			log.error("PUT批量写入数据失败，tableName={}，异常：{}", tableName, e.getMessage(), e);
		} finally {
			close(table);
		}

		return false;
	}

	@Override
	public <T extends com.tce.smart.bridge.entity.Table> boolean delete(T model) {
		String tableName = table(model.getClass());
		String rowKey = model.getKey();
		return delete(tableName, rowKey, null, null);
	}

	/**
	 * 删除一个列族下的数据
	 *
	 * @param tableName    目标数据表
	 * @param rowKey       rowKey
	 * @param columnFamily 列族名
	 */
	@Override
	public void delete(String tableName, String rowKey, String columnFamily) {
		delete(tableName, rowKey, columnFamily, null);
	}

	/**
	 * 删除某个列下的数据
	 *
	 * @param tableName    目标数据表
	 * @param rowKey       rowKey
	 * @param columnFamily 列族名
	 * @param qualifier    列名
	 * @return
	 */
	@Override
	public boolean delete(String tableName, String rowKey, String columnFamily, String qualifier) {

		Table table = null;
		try {
			if (!hbaseAdmin.tableExists(tableName)) {
				log.warn("数据表不存在，tableName：{}", tableName);
				return false;
			}
			table = connection.getTable(TableName.valueOf(tableName));
			Delete delete = new Delete(rowKey.getBytes());
			if(columnFamily != null && !columnFamily.isEmpty()){
				if (qualifier != null && !qualifier.isEmpty()) {
					delete.addColumn(columnFamily.getBytes(), qualifier.getBytes());
				}else {
					delete.addFamily(columnFamily.getBytes());
				}
			}
			table.delete(delete);
			return true;
		} catch (IOException e) {
			log.error("删除数据失败，tableName={}，异常：{}", tableName, e.getMessage(), e);
		} finally {
			close(table);
		}
		return false;
	}

	/**
	 * 批量删除数据
	 *
	 * @param tableName  表名
	 * @param deleteList 需要删除的数据
	 * @return
	 */
	@Override
	public boolean deleteBatch(String tableName, List<Delete> deleteList) {

		Table table = null;
		try {
			if (!hbaseAdmin.tableExists(tableName)) {
				log.warn("数据表不存在，tableName：{}", tableName);
				return false;
			}
			table = connection.getTable(TableName.valueOf(tableName));
			table.delete(deleteList);
			return true;
		} catch (IOException e) {
			log.error("批量删除数据失败，tableName={}，异常：{}", tableName, e.getMessage(), e);
		} finally {
			close(table);
		}
		return false;
	}

	/**
	 * 通过scan查询数据
	 *
	 * @param tableName 表名
	 * @param scan      scan
	 * @return 返回 ResultScanner
	 */
	@Override
	public ResultScanner scan(String tableName, Scan scan) {

		Table table = null;
		try {
			if (!hbaseAdmin.tableExists(tableName)) {
				log.warn("数据表不存在，tableName：{}", tableName);
				return null;
			}
			table = connection.getTable(TableName.valueOf(tableName));
			return table.getScanner(scan);
		} catch (IOException e) {
			log.error("Scan查询数据失败，tableName={}，异常：{}", tableName, e.getMessage(), e);
		} finally {
			close(table);
		}
		return null;
	}

	/**
	 * 删除表
	 *
	 * @param table
	 * @return
	 */
	@Override
	public boolean dropTable(Class<? extends com.tce.smart.bridge.entity.Table> table) {
		return dropTable(table(table));
	}

	/**
	 * 删除表
	 *
	 * @param tableName 表名称
	 * @return
	 */
	@Override
	public boolean dropTable(String tableName) {
		try {
			if (!hbaseAdmin.tableExists(tableName)) {
				log.warn("数据表不存在，tableName：{}", tableName);
				return false;
			}
			hbaseAdmin.disableTable(tableName);
			hbaseAdmin.deleteTable(tableName);
			log.info("Table删除成功，tableName：{}", tableName);
			return true;
		} catch (IOException e) {
			log.info("Table删除失败，tableName：{}，异常：{}", tableName, e.getMessage(), e);
		}
		return false;
	}

	private ApplicationContext applicationContext;

	@Override
	public void afterPropertiesSet() {
		final Map<String, com.tce.smart.bridge.entity.Table> handlers = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, com.tce.smart.bridge.entity.Table.class, true, true);
		handlers.values().forEach(model -> {
			try {
				createTable(model.getClass());
			} catch (Exception e) {
				log.error("创建 HBase Table 失败，异常：{}", e.getMessage(), e);
			}
		});
	}

	/**
	 * 获取列族名
	 *
	 * @param model
	 * @return
	 */
	private String table(Class<? extends com.tce.smart.bridge.entity.Table> model) {
		com.tce.smart.bridge.annotation.Table table = AnnotationUtils.findAnnotation(model, com.tce.smart.bridge.annotation.Table.class);
		return Objects.requireNonNull(table, "未定义表名，请添加注解：Table").name();
	}

	/**
	 * 获取列族
	 *
	 * @param tClass
	 * @return
	 */
	private List<String> family(Class<? extends com.tce.smart.bridge.entity.Table> tClass) {
		return Arrays.stream(tClass.getDeclaredFields())
				.filter(field -> Objects.nonNull(field.getAnnotation(com.tce.smart.bridge.annotation.Family.class)))
				.map(Field::getName)
				.collect(Collectors.toList());
	}

	/**
	 * 获取列族
	 *
	 * @param table
	 * @return
	 */
	private <T extends com.tce.smart.bridge.entity.Table> Map<String, ? extends com.tce.smart.bridge.entity.Family> family(T table) {
		Map<String, com.tce.smart.bridge.entity.Family> family = new HashMap<>();
		Arrays.stream(table.getClass().getDeclaredFields())
				.filter(field -> Objects.nonNull(field.getAnnotation(com.tce.smart.bridge.annotation.Family.class)))
				.forEach(field -> family.put(field.getName(), (com.tce.smart.bridge.entity.Family) ReflectUtils.getFieldValue(table, field)));
		return family;
	}

	/**
	 * 获取列
	 * 可使用 Jackson 的 @JsonProperty 注解设置别名
	 *
	 * @param tClass
	 * @return
	 */
	private List<String> qualifier(Class<? extends com.tce.smart.bridge.entity.Family> tClass) {
		return Arrays.stream(tClass.getDeclaredFields())
				.map(Field::getName)
				.collect(Collectors.toList());
	}

	/**
	 * 获取列
	 *
	 * @param familyDTO
	 * @return
	 */
	private <T extends com.tce.smart.bridge.entity.Family> Map<String, Object> qualifier(T familyDTO) {
		Map<String, Object> qualifier = new HashMap<>();
		Arrays.stream(ReflectUtils.getFields(familyDTO.getClass()))
				.forEach(field -> qualifier.put(field.getName(), ReflectUtils.getFieldValue(familyDTO, field)));
		return qualifier;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	private <T extends com.tce.smart.bridge.entity.Table> T toBean(Result result, Class<T> tClass) {
		Map<String, Object> stringObjectMap = toMap(result);
		return BeanUtils.mapToBean(stringObjectMap, tClass, CopyOptions.create());
	}

	private Map<String, Object> toMap(Result result) {
		Map<String, Object> data = new HashMap<>();
		Optional.ofNullable(result.listCells()).ifPresent(cells -> cells.forEach(cell -> {
			String rowKey = Bytes.toString(CellUtil.cloneRow(cell));
			if(!data.containsKey(key())){
				data.put(key(), rowKey);
			}
			String columnFamily = Bytes.toString(CellUtil.cloneFamily(cell));
			String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));
			String value = Bytes.toString(CellUtil.cloneValue(cell));
			Map<String, String> row = new HashMap<>();
			row.put(qualifier, value);
			if(data.containsKey(columnFamily)){
				((Map<String, String>)data.get(columnFamily)).putAll(row);
			}else {
				data.put(columnFamily, row);
			}
		}));
		return data;
	}

	private String key(){
		Field field = Arrays.stream(ReflectUtils.getFields(com.tce.smart.bridge.entity.Table.class))
				.filter(f -> Objects.nonNull(f.getAnnotation(com.tce.smart.bridge.annotation.Key.class)))
				.findFirst().orElse(null);
		return Objects.isNull(field) ? null : field.getName();
	}
}
