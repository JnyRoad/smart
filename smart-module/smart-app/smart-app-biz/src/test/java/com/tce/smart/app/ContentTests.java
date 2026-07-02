package com.tce.smart.app;

import cn.hutool.core.io.FileUtil;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.service.AppContentTextService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;

@org.junit.Ignore("手工调试脚本：从开发者本机路径读文件并写真实数据库，禁止自动构建执行")
public class ContentTests extends BaseTests {

    @Autowired
    private AppContentTextService contentTextService;
    @Test
    public void text() {
        AppContentText text = new AppContentText();
        text.setTextName("test");
        text.setTextDesc("test1");
        text.setPicBinary(FileUtil.readBytes("C:\\Users\\jinbo\\Desktop\\WEB前端编码规范.pdf"));
        boolean b = contentTextService.save(text);
        System.out.println(b);
    }
//
//	@RequestMapping("/getVideo")
//	public void getVideo(HttpServletRequest request, HttpServletResponse response, String fileName) {
//		//视频资源存储信息
//		response.reset();
//		//获取从那个字节开始读取文件
//		String rangeString = request.getHeader("Range");
//		log.info("getVideo获取视频资源:{},读取文件字节:{}",fileName,rangeString);
//		try {
//			//获取响应的输出流
//			OutputStream outputStream = response.getOutputStream();
//			File file = new File(fileName);
//			if(file.exists()){
//				RandomAccessFile targetFile = new RandomAccessFile(file, "r");
//				long fileLength = targetFile.length();
//				//播放
//				if(rangeString != null){
//
//					long range = Long.parseLong(rangeString.substring(rangeString.indexOf("=") + 1, rangeString.indexOf("-")));
//					//设置内容类型
//					response.setHeader("Content-Type", "video/mov");
//					//设置此次相应返回的数据长度
//					response.setHeader("Content-Length", String.valueOf(fileLength - range));
//					//设置此次相应返回的数据范围
//					response.setHeader("Content-Range", "bytes "+range+"-"+(fileLength-1)+"/"+fileLength);
//					//返回码需要为206，而不是200
//					response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
//					//设定文件读取开始位置（以字节为单位）
//					targetFile.seek(range);
//				}else {//下载
//
//					//设置响应头，把文件名字设置好
//					response.setHeader("Content-Disposition", "attachment; filename="+fileName );
//					//设置文件长度
//					response.setHeader("Content-Length", String.valueOf(fileLength));
//					//解决编码问题
//					response.setHeader("Content-Type","application/octet-stream");
//				}
//
//
//				byte[] cache = new byte[1024 * 300];
//				int flag;
//				while ((flag = targetFile.read(cache))!=-1){
//					outputStream.write(cache, 0, flag);
//				}
//			}else {
//				String message = "file:"+fileName+" not exists";
//				//解决编码问题
//				response.setHeader("Content-Type","application/json");
//				outputStream.write(message.getBytes(StandardCharsets.UTF_8));
//			}
//
//			outputStream.flush();
//			outputStream.close();
//
//		} catch (FileNotFoundException e) {
//
//		} catch (IOException e) {
//
//		}
//	}
//
//	@RequestMapping("/getViewImg1")
//	public void execute1(HttpServletResponse response,@RequestParam(value="imgPath") String imgPath){
//		//由于数据库存的是绝对路径,之前的老数据只能这样转换了
//		imgPath=imgPath.replace("http://oss-cn-a-internal.aliyuncs.com/", "http://oss-cn-yczw-d01-a.yc-ops.com.cn/");
//		System.out.println("路径-"+imgPath);
//		try {
//
//			if(imgPath.indexOf("http")>-1) {
//				URL url = null;
//				InputStream input = null;
//				try{
//					url = new URL(imgPath);
//					HttpURLConnection httpUrl = (HttpURLConnection) url.openConnection();
//					httpUrl.connect();
//					httpUrl.getInputStream();
//					input = httpUrl.getInputStream();
//				}catch (Exception e) {
//					e.printStackTrace();
//					return;
//				}
//				response.setContentType(url.openConnection().getContentType());
//				ServletOutputStream out=response.getOutputStream();
//				try {
//					byte[] buf = new byte[2048];
//					while(input.read(buf)>=0){
//						out.write(buf);
//					}
//					out.flush();
//					out.close();
//
//				} catch (Exception e) {
//					e.printStackTrace();
//				}finally{
//					if(input!=null){
//						try {
//							input.close();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//
//// 如果边返回的流会导致图片不清晰或者马赛克, 请使用下边注释的代码返回流
//				/*
//				   URL urlimg = new URL(imgPath);
//	              //创建链接对象
//	              URLConnection urlConnection = urlimg.openConnection();
//	              //设置超时
//	              urlConnection.setConnectTimeout(1000);
//	              urlConnection.setReadTimeout(5000);
//	              urlConnection.connect();
//	              //获取流
//	              InputStream inputStream = urlConnection.getInputStream();
//
//
//	              // 判读是mp4格式还是jpg格式
//
//	              String format=imgPath.substring(imgPath.lastIndexOf(".")+1);
//	              if(".mp4".equals(format)) {
//	            	  response.setContentType("video/mp4"); // 设置返回的文件类型
//	            	  response.addHeader("Content-Type", "audio/mp4;charset=UTF-8");
//	            	  IOUtils.copy(inputStream, response.getOutputStream());
//	            	  response.flushBuffer();
//	              }else if(".mp3".equals(format)) {
//	            	  response.addHeader("Content-Type", "audio/mpeg;charset=UTF-8");
//	            	  IOUtils.copy(inputStream, response.getOutputStream());
//	            	  response.flushBuffer();
//	              }else if (".jpg".equals(format)) {
//	            	  //读取图片
//		              BufferedImage bufferedImage = ImageIO.read(inputStream);
//		              if (bufferedImage!=null){
//		                  //打印图片
//		                  ImageIO.write(bufferedImage,format,response.getOutputStream());// 将文件流放入response中
//
//		              }
//		              if(inputStream!=null){
//							try {
//								inputStream.close();
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
//						}
//					}
//	              }*/
//
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//	}
}
