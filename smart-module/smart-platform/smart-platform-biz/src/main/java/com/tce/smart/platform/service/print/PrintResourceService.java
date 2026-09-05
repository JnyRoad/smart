package com.tce.smart.platform.service.print;

import lombok.Value;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.*;
import java.util.*;

/** 模板图片上传只授予模板资源用途，不能借此登记或读取员工照片。 */
@Service
public class PrintResourceService {
    private static final int MAX_BYTES=20*1024*1024;
    private final PrintAccessPolicy access;
    private final SqlPrintObjectStore store;
    public PrintResourceService(PrintAccessPolicy access,SqlPrintObjectStore store) { this.access=access; this.store=store; }
    public PrintResourceStore.RegisteredResource upload(String parkId,String mediaType,String purpose,InputStream input) throws IOException {
        String park=access.resolvePark(parkId); access.require("write",park);
        if(!Arrays.asList("LOGO","BACKGROUND").contains(purpose)) throw new PrintApiException(403,"PRINT_SCOPE_DENIED","模板上传不允许人员照片或其他资源用途");
        if(!Arrays.asList("image/png","image/jpeg").contains(mediaType)) invalidImage();
        ByteArrayOutputStream out=new ByteArrayOutputStream(); byte[] buffer=new byte[8192]; int count;
        while((count=input.read(buffer))!=-1) { if(out.size()+count>MAX_BYTES) throw new PrintApiException(413,"PAYLOAD_LIMIT_EXCEEDED","模板图片最大为20MiB"); out.write(buffer,0,count); }
        byte[] bytes=out.toByteArray(); validateImage(bytes,mediaType);
        return store.saveTemplateImage(park,access.actor(),purpose,mediaType,bytes);
    }
    public Download download(String id,String parkId) {
        PrintAccessPolicy.uuid(id); String park=access.resolvePark(parkId); access.require("resource",park);
        PrintResourceStore.RegisteredResource resource=store.describe(id);
        if(resource==null) throw new PrintApiException(404,"PRINT_RESOURCE_NOT_FOUND","模板资源不存在");
        if(!"TEMPLATE".equals(resource.getAccessScope())) throw new PrintApiException(403,"PRINT_SCOPE_DENIED","该对象不是模板图片");
        return new Download(resource,access.readResource(park,id,resource.getContentHash()));
    }
    private static void validateImage(byte[] bytes,String mediaType) {
        ImageReader reader=null;
        try(ImageInputStream image=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers=ImageIO.getImageReaders(image); if(!readers.hasNext()) { invalidImage(); return; }
            reader=readers.next(); reader.setInput(image,true,true);
            String format=reader.getFormatName(); if("image/png".equals(mediaType) ? !"png".equalsIgnoreCase(format) : !("jpeg".equalsIgnoreCase(format) || "jpg".equalsIgnoreCase(format))) invalidImage();
            int width=reader.getWidth(0),height=reader.getHeight(0);
            if(width<=0 || height<=0 || width>4096 || height>4096 || (long)width*height>16000000) throw new PrintApiException(422,"TEMPLATE_VALIDATION_FAILED","模板图片尺寸过大，请缩小到4096像素以内");
            if(reader.read(0)==null) invalidImage();
        } catch(IOException | IllegalArgumentException error) { invalidImage(); }
        finally { if(reader!=null) reader.dispose(); }
    }
    private static void invalidImage() { throw new PrintApiException(422,"TEMPLATE_VALIDATION_FAILED","图片内容不完整或与声明格式不一致，仅支持PNG和JPEG"); }
    @Value public static class Download { PrintResourceStore.RegisteredResource resource; byte[] bytes; }
}
