package com.tce.smart.platform.service.print;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.tce.smart.platform.api.dto.req.print.PrintTemplateRequest;
import com.tce.smart.platform.core.entity.print.PrintTemplateVersion;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;
import static com.tce.smart.platform.service.print.PrintAccessPolicyTest.*;

/** 合成红蓝像素贯穿Java冻结和真实Node PDF，永不读取数据库或人员照片。 */
public class PrintPersonPhotoTest {
 private PrintFeatureProperties props;private PrintAccessPolicy access;private PrintRendererClient renderer;
 @Before public void setup(){loginManager();props=properties();access=new PrintAccessPolicy(props,null);renderer=new PrintRendererClient(props,access);}
 @After public void logout(){org.springframework.security.core.context.SecurityContextHolder.clearContext();}
 private PrintTemplateRequest draft()throws Exception{
  PrintTemplateRequest value=new PrintTemplateServiceTest().draft("FRONT");ObjectNode component=((ArrayNode)value.getLayoutJson().path("schemas").get(0)).addObject().put("name","人员照片").put("type","image").put("width",20).put("height",20).put("readOnly",true);component.putObject("position").put("x",5).put("y",5);
  ((ArrayNode)value.getFieldSchemaJson().path("fields")).addObject().put("key","personPhoto").put("schemaName","人员照片").put("required",true);return value;
 }
 private PrintTemplateVersion version(PrintTemplateRequest draft){
  PrintTemplateVersion value=new PrintTemplateVersion();value.setParkId("1");value.setTemplateId(UUID.randomUUID().toString());value.setTemplateVersionId(UUID.randomUUID().toString());value.setDraftRevision(0L);value.setVersionStatus("PUBLISHED");value.setFaceRole(draft.getFaceRole());value.setSideCount(1);
  value.setLayoutJson(draft.getLayoutJson().toString());value.setFieldSchemaJson(draft.getFieldSchemaJson().toString());value.setPageSpecJson(draft.getPageSpecJson().toString());value.setResourceManifestJson(draft.getResourceManifest().toString());value.setContentHash(PrintTemplateValidator.contentHash(value));return value;
 }
 private ObjectNode subject()throws Exception{
  ObjectNode value=PrintJson.object().put("parkId","1").put("subjectId","synthetic-person-a").put("subjectType","STAFF").put("printItemType","STAFF_CARD");value.putObject("fields");byte[] bytes=picture(false,"png");value.putArray("resources").addObject().put("bindingKey","personPhoto").put("mediaType","image/png").put("sha256",PrintJson.hashBytes(bytes)).put("bytesBase64",Base64.getEncoder().encodeToString(bytes));return value;
 }
 private byte[] picture(boolean swap,String format)throws Exception{
  BufferedImage image=new BufferedImage(16,16,BufferedImage.TYPE_INT_RGB);for(int y=0;y<16;y++)for(int x=0;x<16;x++)image.setRGB(x,y,(x<8)^swap?0xff0000:0x0000ff);
  ByteArrayOutputStream out=new ByteArrayOutputStream();assertTrue(ImageIO.write(image,format,out));return out.toByteArray();
 }
 @Test public void draftAcceptsOnlyRequiredImagePhotoBinding()throws Exception{
  new PrintTemplateValidator(access,props).validate("1",draft());
  for(String type:Arrays.asList("text","qrcode","code128")){PrintTemplateRequest bad=draft();((ObjectNode)bad.getLayoutJson().path("schemas").get(0).get(0)).put("type",type);expectCode("TEMPLATE_VALIDATION_FAILED",()->new PrintTemplateValidator(access,props).validate("1",bad));}
  PrintTemplateRequest optional=draft();((ObjectNode)optional.getFieldSchemaJson().path("fields").get(0)).put("required",false);expectCode("TEMPLATE_VALIDATION_FAILED",()->new PrintTemplateValidator(access,props).validate("1",optional));
  PrintTemplateRequest text=draft();((ObjectNode)text.getFieldSchemaJson().path("fields").get(0)).put("key","staffName");expectCode("TEMPLATE_VALIDATION_FAILED",()->new PrintTemplateValidator(access,props).validate("1",text));
 }
 @Test public void draftRejectsConflictingStaticReferencesAndContent()throws Exception{
  PrintTemplateRequest ref=draft();((ObjectNode)ref.getLayoutJson().path("schemas").get(0).get(0)).putObject("resourceRef").put("objectId","foreign");expectCode("TEMPLATE_VALIDATION_FAILED",()->new PrintTemplateValidator(access,props).validate("1",ref));
  for(String content:Arrays.asList("https://invalid.example/photo","data:image/png;base64,AAAA","uncontrolled")){PrintTemplateRequest value=draft();((ObjectNode)value.getLayoutJson().path("schemas").get(0).get(0)).put("content",content);expectCode("TEMPLATE_VALIDATION_FAILED",()->new PrintTemplateValidator(access,props).validate("1",value));}
 }
 @Test public void freezeCopiesOnlyBoundPhotoWithOwnershipAndRejectsMissingRequiredPhoto()throws Exception{
  ObjectNode source=subject();PrintTemplateVersion version=version(draft());ObjectNode frozen=renderer.freezeJobRequest("job-a","MANUAL_DUPLEX",Collections.singletonList(version),source);JsonNode photo=frozen.at("/faceSources/0/resourceManifest/0");
  assertEquals("personPhoto",photo.path("bindingKey").asText());assertEquals("synthetic-person-a",photo.path("subjectId").asText());assertEquals("STAFF",photo.path("subjectType").asText());assertEquals(source.at("/resources/0/bytesBase64").asText(),photo.path("contentBase64").asText());assertEquals(0,frozen.at("/faceSources/0/resolvedInput/fields").size());
  source.putArray("resources");assertFalse(photo.path("contentBase64").asText().isEmpty());expectCode("PRINT_SUBJECT_PHOTO_REQUIRED",()->renderer.freezeJobRequest("job-a","MANUAL_DUPLEX",Collections.singletonList(version),source));
  ObjectNode withPhoto=subject();PrintTemplateRequest noPhoto=draft();((ArrayNode)noPhoto.getLayoutJson().path("schemas").get(0)).removeAll();((ArrayNode)noPhoto.getFieldSchemaJson().path("fields")).removeAll();assertEquals(0,renderer.freezeJobRequest("job-a","MANUAL_DUPLEX",Collections.singletonList(version(noPhoto)),withPhoto).at("/faceSources/0/resourceManifest").size());
 }
 @Test public void staticImageRoundTripKeepsNullableRegisteredMetadataAndIgnoresUnusedPersonPhoto()throws Exception{
  byte[] bytes=picture(false,"png");PrintResourceStore.RegisteredResource image=new PrintResourceStore.RegisteredResource();image.setObjectId(UUID.randomUUID().toString());image.setParkId("1");image.setPurpose("BACKGROUND");image.setAccessScope("TEMPLATE");image.setMediaType("image/png");image.setContentHash(PrintJson.hashBytes(bytes));image.setSizeBytes((long)bytes.length);
  PrintResourceStore store=new PrintResourceStore(){public RegisteredResource describe(String id){return image;}public boolean canAccess(String actor,RegisteredResource resource){return true;}public byte[] read(String id){return bytes;}};
  PrintAccessPolicy policy=new PrintAccessPolicy(props,store);PrintTemplateRequest fixed=draft();((ArrayNode)fixed.getFieldSchemaJson().path("fields")).removeAll();((ArrayNode)fixed.getResourceManifest()).add(PrintJson.tree(image));((ObjectNode)fixed.getLayoutJson().path("schemas").get(0).get(0)).putObject("resourceRef").put("objectId",image.getObjectId()).put("contentHash",image.getContentHash());
  new PrintTemplateValidator(policy,props).validate("1",fixed);ObjectNode frozen=new PrintRendererClient(props,policy).freezeJobRequest("job-static","MANUAL_DUPLEX",Collections.singletonList(version(fixed)),subject());assertEquals(1,frozen.at("/faceSources/0/resourceManifest").size());assertEquals(image.getObjectId(),frozen.at("/faceSources/0/resourceManifest/0/objectId").asText());assertFalse(frozen.at("/faceSources/0/resourceManifest/0").has("bindingKey"));
 }
 @Test public void freezeRejectsTamperedHashTypeExternalAddressDuplicateAndFalseImages()throws Exception{
  PrintTemplateVersion version=version(draft());for(int mutation=0;mutation<8;mutation++){
   ObjectNode source=subject(),photo=(ObjectNode)source.path("resources").get(0);
   if(mutation==0)photo.put("sha256","sha256:"+String.join("",Collections.nCopies(64,"0")));
   if(mutation==1)photo.put("mediaType","image/jpeg");if(mutation==2)photo.put("url","https://invalid.example/photo");if(mutation==3)((ArrayNode)source.path("resources")).add(photo.deepCopy());if(mutation==4)photo.put("bindingKey","unknown");if(mutation==5)photo.put("subjectId","other-person");if(mutation==6)photo.put("bytesBase64",photo.path("bytesBase64").asText()+"\n");
   if(mutation==7){byte[] fake={(byte)137,80,78,71,13,10,26,10};photo.put("bytesBase64",Base64.getEncoder().encodeToString(fake)).put("sha256",PrintJson.hashBytes(fake));}
   expectCode("PRINT_SUBJECT_PHOTO_INVALID",()->renderer.freezeJobRequest("job-a","MANUAL_DUPLEX",Collections.singletonList(version),source));
  }
 }
 @Test public void publicationProvidesExplicitSyntheticResourceWithoutTrustingBrowserPhoto()throws Exception{
  props.setRendererUrl("http://127.0.0.1:19999");props.setRendererToken("synthetic-only-test-token");final ObjectNode[] sent={null};PrintRendererClient capture=new PrintRendererClient(props,access){@Override public JsonNode renderFrozen(JsonNode request){sent[0]=(ObjectNode)request;return PrintJson.object();}};
  capture.renderPreview("preview-a","STAFF_CARD",Collections.singletonList(version(draft())),PrintJson.object().put("personPhoto","https://invalid.example/photo"));assertTrue(sent[0].at("/faceSources/0/resourceManifest/0/synthetic").asBoolean());assertEquals("SYNTHETIC",sent[0].at("/faceSources/0/resolvedInput/subjectType").asText());assertFalse(sent[0].toString().contains("invalid.example"));assertEquals(0,sent[0].at("/faceSources/0/resolvedInput/fields").size());
 }
 @Test public void freezeRejectsPngWithoutIendEvenWhenPixelsDecode()throws Exception{
  byte[] complete=picture(false,"png");byte[] truncated=Arrays.copyOf(complete,complete.length-12);
  assertNotNull("反例必须能够被ImageIO容错解码",ImageIO.read(new ByteArrayInputStream(truncated)));
  assertPhotoRejected(truncated,"image/png");
 }
 @Test public void freezeRejectsJpegWithoutEoiEvenWhenPixelsDecode()throws Exception{
  byte[] complete=picture(false,"jpeg");byte[] truncated=Arrays.copyOf(complete,complete.length-2);
  assertNotNull("反例必须能够被ImageIO容错解码",ImageIO.read(new ByteArrayInputStream(truncated)));
  assertPhotoRejected(truncated,"image/jpeg");
 }
 @Test public void freezeRejectsPngCrcChunkBoundaryAndTrailingBytes()throws Exception{
  byte[] complete=picture(false,"png"),crc=complete.clone();crc[crc.length-1]^=1;assertPhotoRejected(crc,"image/png");
  byte[] boundary=complete.clone();boundary[boundary.length-12]=0x7f;assertPhotoRejected(boundary,"image/png");
  assertPhotoRejected(Arrays.copyOf(complete,complete.length+1),"image/png");
 }
 @Test public void freezeRejectsJpegDecodeWarningAndPreservesCompleteJpeg()throws Exception{
  byte[] complete=picture(false,"jpeg");byte[] shortened=Arrays.copyOf(complete,complete.length-8);shortened[shortened.length-2]=(byte)0xff;shortened[shortened.length-1]=(byte)0xd9;
  assertPhotoRejected(shortened,"image/jpeg");
  ObjectNode frozen=freezePhoto(complete,"image/jpeg");assertEquals(Base64.getEncoder().encodeToString(complete),frozen.at("/faceSources/0/resourceManifest/0/contentBase64").asText());assertEquals("image/jpeg",frozen.at("/faceSources/0/resourceManifest/0/mediaType").asText());
 }
 private ObjectNode freezePhoto(byte[] bytes,String mediaType)throws Exception{
  ObjectNode source=subject();((ObjectNode)source.path("resources").get(0)).put("mediaType",mediaType).put("sha256",PrintJson.hashBytes(bytes)).put("bytesBase64",Base64.getEncoder().encodeToString(bytes));
  return renderer.freezeJobRequest("job-photo-integrity","MANUAL_DUPLEX",Collections.singletonList(version(draft())),source);
 }
 private void assertPhotoRejected(byte[] bytes,String mediaType)throws Exception{
  try{freezePhoto(bytes,mediaType);fail("照片必须在冻结前拒绝："+mediaType);}catch(PrintApiException error){assertEquals(422,error.getStatus());assertEquals("PRINT_SUBJECT_PHOTO_INVALID",error.getCode());}
 }
 @Test public void realNodePdfAndJobPreviewRetainFrozenRedBluePixelsAfterSourceChanges()throws Exception{
  Assume.assumeTrue(Boolean.getBoolean("print.renderer.live"));Path root=Paths.get("../../..").toAbsolutePath().normalize().resolve("smart-print-renderer");String token=UUID.randomUUID().toString()+UUID.randomUUID().toString();
  ProcessBuilder command=new ProcessBuilder("node","--input-type=module","-e","import {createRenderServer} from './src/server.mjs';const s=createRenderServer({token:process.env.PRINT_RENDERER_TOKEN,concurrency:1});s.listen(0,'127.0.0.1',()=>console.log(s.address().port));");command.directory(root.toFile());command.environment().put("PRINT_RENDERER_TOKEN",token);command.redirectError(ProcessBuilder.Redirect.INHERIT);Process process=command.start();ExecutorService reader=Executors.newSingleThreadExecutor();
  try{
   String port=reader.submit(()->new BufferedReader(new InputStreamReader(process.getInputStream())).readLine()).get(20,TimeUnit.SECONDS);props.setRendererUrl("http://127.0.0.1:"+Integer.parseInt(port));props.setRendererToken(token);props.setRendererReadTimeoutMs(60000);renderer=new PrintRendererClient(props,access);
   PrintTemplateRequest front=draft(),back=draft();back.setFaceRole("BACK");((ObjectNode)back.getLayoutJson()).put("faceRole","BACK");List<PrintTemplateVersion> versions=Arrays.asList(version(front),version(back));ObjectNode source=subject();ObjectNode frozen=renderer.freezeJobRequest("job-a","MANUAL_DUPLEX",versions,source);String serialized=frozen.toString();
   byte[] changed=picture(true,"png");((ObjectNode)source.path("resources").get(0)).put("bytesBase64",Base64.getEncoder().encodeToString(changed)).put("sha256",PrintJson.hashBytes(changed));assertPixels(renderer.renderFrozen(PrintJson.read(serialized)),false);
   ObjectNode preview=(ObjectNode)PrintJson.read(serialized);preview.put("purpose","PREVIEW").put("previewId","preview-a");preview.remove("jobId");assertPixels(renderer.renderFrozen(preview),false);assertPixels(renderer.renderFrozen(renderer.freezeJobRequest("job-b","MANUAL_DUPLEX",versions,source)),true);
   assertEquals("READY",renderer.renderPreview("synthetic-preview-a","STAFF_CARD",Collections.singletonList(versions.get(0)),PrintJson.object()).path("status").asText());
  }finally{process.destroy();if(!process.waitFor(5,TimeUnit.SECONDS))process.destroyForcibly();reader.shutdownNow();}
 }
 private void assertPixels(JsonNode result,boolean swapped)throws Exception{
  for(JsonNode artifact:result.path("artifacts"))try(PDDocument pdf=PDDocument.load(Base64.getDecoder().decode(artifact.path("contentBase64").asText()))){
   assertEquals(1,pdf.getNumberOfPages());BufferedImage image=new PDFRenderer(pdf).renderImageWithDPI(0,144);int left=image.getRGB((int)(10*144/25.4),(int)(15*144/25.4)),right=image.getRGB((int)(20*144/25.4),(int)(15*144/25.4));assertTrue(swapped?(left&255)>200&&((left>>16)&255)<40:((left>>16)&255)>200&&(left&255)<40);assertTrue(swapped?((right>>16)&255)>200&&(right&255)<40:(right&255)>200&&((right>>16)&255)<40);
  }
 }
}
