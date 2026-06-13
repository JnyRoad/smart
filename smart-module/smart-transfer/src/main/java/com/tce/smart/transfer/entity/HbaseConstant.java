package com.tce.smart.transfer.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: HbaseConstant
 * @date: 2020/11/13 11:50
 * @author: wuling
 * @version: 1.0
 */
public class HbaseConstant {
	public static Map<String,Map<String,String[]>>  tableMap = new HashMap<>();
	static {
		Map<String,String[]> imageFamilyQualifiers = new HashMap<>();
		imageFamilyQualifiers.put("a",new String[]{"name","size","time","type","info","code"});
		imageFamilyQualifiers.put("b",new String[]{"base64"});
		imageFamilyQualifiers.put("c",new String[]{"small"});
		tableMap.put("blob",imageFamilyQualifiers);
	}

	public enum Table{
		T_BLOB(1,tableMap.get("blob"));
		private final int type;

		private final Map<String,String[]> familyQualifiers;

		Table(int type,Map<String,String[]> familyQualifiers){
			this.type=type;
			this.familyQualifiers = familyQualifiers;
		}

		public int getType() {
			return type;
		}

		public Map<String, String[]> getFamilyQualifiers() {
			return familyQualifiers;
		}
	}
}
