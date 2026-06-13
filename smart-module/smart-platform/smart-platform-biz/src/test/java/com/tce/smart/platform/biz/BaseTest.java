package com.tce.smart.platform.biz;

import cn.hutool.core.map.MapUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Base64Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes={SmartPlatformApplication.class})
public class BaseTest {

//	/*** @Author zmy * @Description hutool导出excel * @Date 10:28 2020/12/16 * @param dataList 数据集合 * @param fileName excel文件名 * @param headAliasMap 表头别名 * @param mergeNameColumnIndexMap 合并单元格的字段名和列角标，角标从0开始 * @param isMerge 是否需要合并单元格 * @return void */
//	public static void hutoolExportExcel(List<?> dataList, String fileName, LinkedHashMap<String, String> headAliasMap, LinkedHashMap<String, Integer> mergeNameColumnIndexMap, Boolean isMerge) throws Exception
//	{
//		// 通过工具类创建writer
//		ExcelWriter writer = ExcelUtil.getWriter(fileName);
//		//自定义标题别名
//		 if (MapUtil.isNotEmpty(headAliasMap))
//		 { writer.setHeaderAlias(headAliasMap);
//		 }
//
//		 if (isMerge) {
//		 	//获取需要合并的单元格所对应的行集合
//			  Map<Integer, List<RowRangeDto>> stringListMap = addMerStrategy(dataList, mergeNameColumnIndexMap);
//			  //调用merge合并单元格
//			 layout(writer, stringListMap);
//		 }
//		 // 一次性写出内容，使用默认样式，强制输出标题
//		writer.write(dataList, true);
//		 // flush或者close方法后才会真正写出文件;关闭writer，才会释放Workbook对象资源
//		writer.close();
//	}
//
//	/*** @Author zmy * @Description 获取合并单元格对应的行集合 * @Date 11:16 2020/12/16 * @param dataList 数据集合 * @param mergeNameColumnIndexMap 有序map，合并单元格字段和列角标map * @return 合并单元格对应的行集合映射 */
//	public static Map<Integer, List<RowRangeDto>> addMerStrategy(List<?> dataList, LinkedHashMap<String, Integer> mergeNameColumnIndexMap) throws Exception
//	{
//		Map<Integer, List<RowRangeDto>> strategyMap = new HashMap<>();
//		Object preObj = null;
//		int i = 0;
//		for (Object currObj : dataList) {
//			if (preObj != null) {
//				Boolean mergeFlag = false;
//				int j = 0;
//				for (Map.Entry<String, Integer> entry : mergeNameColumnIndexMap.entrySet()) {
//					//在第一个列合并的情况下，后面的列才需要合并
//					if (mergeFlag || j == 0) {
//						String name = entry.getKey();
//						if (getGetMethod(currObj, name).equals(getGetMethod(preObj, name))) {
//							fillStrategyMap(strategyMap, mergeNameColumnIndexMap.get(name), i); mergeFlag = true;
//						}
//					} else {
//						break;
//					} j++;
//				}
//			} i++; preObj = currObj;
//		} return strategyMap;
//	}
//
//	public void layout(ExcelWriter writer,Map<Integer,List<RowRangeDto>> columnAndRowRangMap){
//		if(MapUtil.isNotEmpty(columnAndRowRangMap)){
//			columnAndRowRangMap.entrySet().stream().forEach(entry -> {
//				Integer column = entry.getKey();
//				List<RowRangeDto> rowRangeList = entry.getValue();
//				merge(writer,column,rowRangeList);
//			});
//		}
//	}

	class RowRangeDto {
		private int start;
		private int end;
	}
}
