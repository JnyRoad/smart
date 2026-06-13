package com.tce.smart.transfer.config;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.beans.PropertyDescriptor;
import java.util.*;

/**
 * @description: HbaseFindBuilder
 * @date: 2020/11/13 11:52
 * @author: wuling
 * @version: 1.0
 */
public class HbaseFindBuilder<T> {

	private String family;

	private Result result;

	private String qualifier;

	private final Map<String, PropertyDescriptor> fieldsMap;

	private final Set<String> propertiesSet;

	private Set<String> qualifierSet;

	private BeanWrapper beanWrapper;

	private Map<String,String[]> familyQualifiers;

	private T tBean;

	/**
	 * 按family查询
	 * @param family
	 * @param result
	 * @param tclazz
	 */
	public HbaseFindBuilder(String family, Result result, Class<T> tclazz) {

		this.family = family;
		this.result = result;
		fieldsMap = new HashMap();
		propertiesSet = new HashSet<>();

		reflectBean(tclazz);

	}

	/**
	 * 多个CF
	 * @param familyQualifiers
	 * @param tclazz
	 */
	public HbaseFindBuilder(Map<String,String[]> familyQualifiers, Class<T> tclazz) {
		fieldsMap = new HashMap();
		propertiesSet = new HashSet<>();
		this.familyQualifiers=familyQualifiers;
		reflectBean(tclazz);
	}

	public HbaseFindBuilder build(Result result) {
		this.result = result;
		return buildWithOwnOneMap(familyQualifiers);
	}

	/**
	 * return the result by qulifier
	 * @param qualifier
	 * @return
	 */
	public HbaseFindBuilder build(String qualifier) {

		return this.build(qualifier,"");
	}

	/**
	 * by multiple qualifier
	 * @param qualifiers
	 * @return
	 */
	public HbaseFindBuilder build(String... qualifiers) {

		if (qualifiers == null || qualifiers.length == 0) {
			return this;
		}
		PropertyDescriptor p = null;
		byte[] qualifierByte = null;

		for (String qualifier : qualifiers) {
			if (StringUtils.isEmpty(qualifier)) {
				continue;
			}
			p = fieldsMap.get(qualifier.trim());
			qualifierByte = result.getValue(family.getBytes(), HumpNameUtils.humpEntityForVar(qualifier).getBytes());
			if (qualifierByte != null && qualifierByte.length > 0) {
				beanWrapper.setPropertyValue(p.getName(), Bytes.toString(qualifierByte));
				propertiesSet.add(p.getName());
			}
		}

		return this;
	}

	/**
	 * by map
	 * @param map
	 * @return
	 */
	public HbaseFindBuilder build(Map<String,String> map) {

		if (map == null || map.size() <= 0) {
			return this;
		}

		PropertyDescriptor p = null;
		byte[] qualifierByte = null;

		for (String value : map.values()) {
			if (StringUtils.isEmpty(value)) {
				continue;
			}

			p = fieldsMap.get(value.trim());
			qualifierByte = result.getValue(family.getBytes(), HumpNameUtils.humpEntityForVar(value).getBytes());

			if (qualifierByte != null && qualifierByte.length > 0) {
				beanWrapper.setPropertyValue(p.getName(), Bytes.toString(qualifierByte));
				propertiesSet.add(p.getName());
			}
		}

		return this;
	}

	private void reflectBean(Class<T> tclazz) {

		tBean = BeanUtils.instantiate(tclazz);

		PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(tclazz);
		for (PropertyDescriptor p : propertyDescriptors) {
			if (p.getWriteMethod() != null) {
				this.fieldsMap.put(p.getName(), p);
			}
		}

		beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(tBean);
	}

	public T fetch() {
		if (null != propertiesSet && propertiesSet.size() > 0) {
			return this.tBean;
		}
		return null;
	}

	public HbaseFindBuilder buildWithOwnOneMap(Map<String,String[]> familyQualifiers) {
		if(familyQualifiers==null||familyQualifiers.isEmpty())
			return this;
		familyQualifiers.entrySet().stream().filter(es -> !StringUtils.isBlank(es.getKey())
				&& (es.getValue() != null) && (es.getValue().length > 0))
				.forEach(es ->
						Arrays.stream(es.getValue()).forEach(q -> {
							PropertyDescriptor p  = fieldsMap.get(q.trim());
							String qualifier = HumpNameUtils.humpEntityForVar(q.trim());
							byte[] qualifierByte=result.getValue(es.getKey().getBytes(), qualifier.getBytes());
							if (qualifierByte != null && qualifierByte.length > 0) {
                               /* if(!p.getName().contains("b_")){
                                    beanWrapper.setPropertyValue(p.getName(), Bytes.toString(qualifierByte));
                                }else {*/
								beanWrapper.setPropertyValue(p.getName(), Bytes.toString(qualifierByte));
								//}

								propertiesSet.add(p.getName());
							}

						})
				);
		return this;

	}
}
