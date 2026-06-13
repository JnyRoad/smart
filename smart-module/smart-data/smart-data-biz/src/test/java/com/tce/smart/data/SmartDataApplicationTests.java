/*
//package com.tce.smart.data;
//
//import com.tce.smart.data.api.dto.msg.req.*;
//import com.tce.smart.data.api.feign.ehrview.RemoteLvwAcardlostService;
//import com.tce.smart.data.service.msg.IEmailManagerService;
//import com.tce.smart.platform.core.entity.SmtMsgRecord;
//import com.tce.smart.platform.core.service.SmtMsgRecordService;
//import org.apache.commons.io.FileUtils;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.File;
//import java.util.*;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class SmartDataApplicationTests {
//	@Autowired
//	private RemoteLvwAcardlostService remoteLvwAcardlostService;
//
//	@Test
//	public void contextLoads() {
////		System.out.println(JSONUtil.toJsonStr(remoteLvwAcardlostService.getByBadge("101077", DateUtils.parse("2013-08-08 00:00:00"), SecurityConstants.FROM_IN)));
//	}
//
//	@Autowired
//	private SmtMsgRecordService smtMsgRecordService;
//
//	@Autowired
//	private IEmailManagerService emailManagerService;
//
//	@Test
//	public void testRecord() {
//		SmtMsgRecord smt = new SmtMsgRecord();
//		String str = "{姓名} 先生/小姐：         \n" +
//				"您好！我是裕同集团人力资源管理中心的HR 。目前我司招聘{岗位}岗位，以下为公司概况及岗位职责介绍，请查阅。根据简历挑选，通知您到我司进行面试，具体安排如下：\n" +
//				"一、岗位名称：{岗位}\n" +
//				"二、岗位职责：\n" +
//				"{岗位职责}\n" +
//				"三、公司概况：\n" +
//				"裕同集团（股票代码002831）创立于一九九六年，国内包装印刷行业龙头企业，一直致力服务世界知名的高端客户，提供专业的全领域印刷包装解决方案。业务覆盖IT 及电子产品、烟酒、化妆品、模切、MIM、互联网+包装等诸多领域。服务包括产品设计、工程服务、材料研发、印刷包装、物流配送及品牌管理等一体化方案。通过快速的响应能力及精细化管理，为客户提供高品质的包装产品和服务。\n" +
//				"集团现有20000余名员工，在深圳、东莞、苏州、烟台、三河、许昌、武汉、重庆、成都、九江、合肥等重点工业城市建立完善的生产基地。在北京，上海也相继设有分公司，在美国、香港、台湾亦设立服务及支援中心。2010开始在东南亚布局，相继在越南、印度建立生产基地。我们的客户群主要是IT高端客户（如A客户、华为、小米、联想、索尼等）、国内知名白酒（如茅台、五粮液、泸州老窖、洋河等）以及化妆品和保健品高端客户。\n" +
//				"2017年销售收入70多亿，预计2018年突破100亿。裕同先后被评为“国家高新技术企业”、“中国纸包装印刷材料研发中心（国家级）”、“中国包装优秀品牌”、“广东省十大最具竞争力印刷企业”、“广东省十佳优秀设计企业”、“2017年中国印刷业最佳雇主二十强企业”等，并于2016、2017和2018连续三年荣获“中国印刷包装百强排行榜”第一名。\n" +
//				"集团已成立裕同大学，聚焦人才培训、人才发展以及文化传承。集团秉承“人才是资本而非成本”的用人理念，诚邀全球优秀人才加盟。裕您一同，与众不同！\n" +
//				"四、福利待遇：\n" +
//				"（1）社保：入职购买五险一金；\n" +
//				"（2）吃住：公司提供住宿有餐补；\n" +
//				"（3）假期：公司有法定的带薪假期，带薪年假以及节假日福利；\n" +
//				"（4）休闲：公司设有图书馆，健身房，篮球场和足球场等休闲娱乐场所；\n" +
//				"（5）其他：年底双薪，年度体检，弹性工作制，5天8小时（每月最后一周周六上班），班车接送。\n" +
//				" \n" +
//				"    面试时间：{面试时间} {周几}\n" +
//				"   面试地址：{面试地址}\n" +
//				"   联系人：{联系人}\n" +
//				"   联系电话：{联系电话}\n" +
//				"   邮箱：{邮箱}\n" +
//				"   公司地址：{园区地址}\n" +
//				"   乘车线路：{乘车路线}\n" +
//				"   如有疑问，烦请电话联系确认！\n";
//
//		smt.setMsgContent(str);
//		Integer id = smtMsgRecordService.addRecord(smt);
//		System.out.println(id);
//	}
//
//	@Test
//	@Transactional
//	public void sendHtmlEmail() {
//
////		SendEmailsAo sendEmailsAo=new SendEmailsAo();
//
//		SendEmailAo sendEmailAo = new SendEmailAo();
//		sendEmailAo.setInbox("pu.hao@bjtce.com");
//		sendEmailAo.setTempCode("2201");
//		Map<String, String> param = new HashMap<>();
//		param.put("姓名", "puao");
//		param.put("岗位", "Java");
//		param.put("岗位职责", "耍就行了");
//		param.put("面试时间", "19-10-10");
//		param.put("周几", "周三");
//		param.put("面试地址", "there");
//		param.put("联系人", "puao");
//		param.put("联系电话", "188818188");
//		param.put("邮箱", "151545@qq.com");
//		param.put("园区地址", "随便");
//		param.put("乘车路线", "随便坐");
//		sendEmailAo.setParam(param);
//		try{
//			File file2 = new File("D:/smart.jar");
//			EmailFileReqDTO emailFile = new EmailFileReqDTO();
//			emailFile.setFileName("smart.jar");
//			emailFile.setFileBytes(FileUtils.readFileToByteArray(file2));
//			List<EmailFileReqDTO> files =new ArrayList<>();
//			files.add(emailFile);
//			sendEmailAo.setFileData(files);
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//		Boolean flag = emailManagerService.sendHtmlEmail(sendEmailAo);
//
//		System.out.println(flag);
//		throw new RuntimeException();
//	}
//
//
//	@Test
//	public void sendHtmlEmailWithContent() {
//		try {
//			File file = new File("D:/身份证.jpg");
//			if (!file.exists()) {
//				file.createNewFile();
//			}
//
//			List<File> list = new ArrayList();
//			list.add(file);
//			EmailReqDTO email = new EmailReqDTO();
//			email.setContent("<div style=\"margin:100px;\">this is a test file post</div><p>这是p标签</p>");
//			List<String> account = Arrays.asList("pu.hao@bjtce.com");
//			email.setInboxs(account);
//			email.setTitle("测试邮件");
//
//			EmailFileReqDTO emailFileAo = new EmailFileReqDTO();
//			emailFileAo.setFileName(file.getName());
//			emailFileAo.setFileBytes(FileUtils.readFileToByteArray(file));
//			List<EmailFileReqDTO> files = new ArrayList<>();
//			files.add(emailFileAo);
//			email.setFileData(files);
//			boolean flag = emailManagerService.sendEmailWithContent(email);
//			System.out.println("==============================" + flag);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//
//	@Test
//	public void sendEmails() {
//
//		SendEmailsAo sendEmailsAo = new SendEmailsAo();
//		sendEmailsAo.setTempCode("5001");
//		AddresseeReqDTO ad = new AddresseeReqDTO();
//		ad.setInbox("pu.hao@bjtce.com");
//		ad.setUsername("wer");
//		AddresseeReqDTO ad1 = new AddresseeReqDTO();
//		ad1.setInbox("impuhao@163.com");
//		ad1.setUsername("pppr");
//
//		List<AddresseeReqDTO> list = new ArrayList<>();
//		list.add(ad);
//		list.add(ad1);
//
//		sendEmailsAo.setAddressee(list);
//		Map<String, String> param = new HashMap<>();
//		param.put("姓名", "puao");
//		param.put("BU", "研究中心");
//		param.put("部门", "技术部	");
//		param.put("岗位", "JAVA程序员");
//		param.put("入职时间", "19-10-10");
//		param.put("电话", "18841881");
//		sendEmailsAo.setParam(param);
//		emailManagerService.sendEmails(sendEmailsAo);
//
//		System.out.println("====");
//	}
//}
*/
