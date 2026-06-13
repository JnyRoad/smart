package com.tce.smart.platform.biz;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.platform.SmartPlatformApplication;
import com.tce.smart.platform.core.entity.SmtOrganizeRelation;
import com.tce.smart.platform.core.entity.SmtParkBu;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.service.SmtOrganizeRelationService;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtStaffService;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @date:
 * @author: fushiping
 * @version: 1.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = SmartPlatformApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestTest {

	@Resource
	private SmtStaffService smtStaffService;
	@Resource
	private SmtParkBuService smtParkBuService;
	@Resource
	private SmtOrganizeRelationService smtOrganizeRelationService;

	//@Test
	public void test1() throws Exception {
//
//		List<SmtParkBu> buList = smtParkBuService.listByParkId(5000021);
//		List<String> bus = buList.stream().map(SmtParkBu::getCompId).collect(Collectors.toList());
//		List<SmtOrganizeRelation> relations = smtOrganizeRelationService.getByParkId(new ArrayList<Integer>() {{
//			add(5000021);
//		}});
//		if (CollUtil.isNotEmpty(relations)) {
//			List<String> relationBus = relations.stream().map(org -> {
//				return org.getId().toString();
//			}).collect(Collectors.toList());
//			bus.addAll(relationBus);
//		}
//
//		List<SmtStaff> staffs = smtStaffService.list(Wrappers.<SmtStaff>query().lambda()
//				.in(SmtStaff::getCompId, bus)
//				.isNull(SmtStaff::getFacePicId));
//		List<String> badge = staffs.stream().map(SmtStaff::getBadge).collect(Collectors.toList());
//
//		File[] files = new File("F:/fu/myWork/yutong3.0/C6照片(1)").listFiles();
//		int len = files.length;
//		for (int i = 0; i < len; i++) {
//			String tmp = files[i].getName();
//			if (StringUtils.isNotBlank(tmp)) {
//				String str = tmp.split(".jpg")[0];
//				if(badge.contains(str)) {
//					File oldFile = new File("F:/fu/myWork/yutong3.0/C6照片(1)/" + tmp);
//					File newfile = new File("F:/fu/myWork/yutong3.0/img/" + tmp);
//					FileInputStream fileInputStream = new FileInputStream(oldFile);
//					FileOutputStream fileOutputStream = new FileOutputStream (newfile);
//					byte[] data = new byte[fileInputStream.available()];
//					fileInputStream.read(data);
//					fileOutputStream.write(data);
//					fileOutputStream.close();
//					fileInputStream.close();
//				}
//			}
//		}


//		List<String> fileName = new ArrayList<>();
//		File[] files = new File("F:/fu/myWork/yutong3.0/C6照片(1)").listFiles();
//		int len = files.length;
//		for (int i = 0; i < len; i++) {
//			String tmp = files[i].getName();
//			if (StringUtils.isNotBlank(tmp)) {
//				String str = tmp.split(".jpg")[0];
//				fileName.add(str);
//			}
//		}
//		List<SmtParkBu> buList = smtParkBuService.listByParkId(5000021);
//		List<String> bus = buList.stream().map(SmtParkBu::getCompId).collect(Collectors.toList());
//		List<SmtOrganizeRelation> relations = smtOrganizeRelationService.getByParkId(new ArrayList<Integer>() {{
//			add(5000021);
//		}});
//		if (CollUtil.isNotEmpty(relations)) {
//			List<String> relationBus = relations.stream().map(org -> {
//				return org.getId().toString();
//			}).collect(Collectors.toList());
//			bus.addAll(relationBus);
//		}
//
//		List<SmtStaff> staffs = smtStaffService.list(Wrappers.<SmtStaff>query().lambda()
//				.in(SmtStaff::getCompId, bus)
//				.isNull(SmtStaff::getFacePicId));
//		List<String> badge = staffs.stream().map(SmtStaff::getBadge).collect(Collectors.toList());
//		badge.retainAll(fileName);
//		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
//				new FileOutputStream("F:/fu/myWork/yutong3.0/1.txt", true)));
//		out.write(badge.toString());
//		out.close();


	}
}
