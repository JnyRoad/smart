package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.dto.CheckFacePicDTO;
import com.tce.smart.platform.core.entity.SmtFaceImgTask;
import com.tce.smart.platform.core.entity.SmtFaceImgTaskDetails;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.mapper.SmtFaceImgTaskMapper;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.vo.CheckFacePicVO;
import com.tce.smart.platform.service.SmtFaceImgTaskDetailsService;
import com.tce.smart.platform.service.SmtStaffService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Collections;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SmtFaceImgTaskServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtFaceImgTask.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtFaceImgTaskDetails.class);
	}

	@Test
	public void checkFacePicUsesDeviceTasksInsteadOfDirectFixedParkIscSync() throws Exception {
		SmtFaceImgTaskMapper taskMapper = Mockito.mock(SmtFaceImgTaskMapper.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtFaceImgTaskDetailsService detailsService = Mockito.mock(SmtFaceImgTaskDetailsService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		SmtFaceImgTaskServiceImpl service = new SmtFaceImgTaskServiceImpl();
		setField(service, "baseMapper", taskMapper);
		setField(service, "smtStaffService", staffService);
		setField(service, "smtFaceImgTaskDetailsService", detailsService);
		setField(service, "smtImageService", imageService);
		SmtStaff staff = new SmtStaff();
		staff.setId(1001L);
		staff.setBadge("JA26086");
		staff.setName("张珂");
		staff.setCompId("1001");
		Mockito.when(taskMapper.insert(Mockito.any(SmtFaceImgTask.class))).thenAnswer(invocation -> {
			SmtFaceImgTask task = invocation.getArgument(0);
			task.setId(9001L);
			return 1;
		});
		Mockito.when(staffService.getOne(Mockito.any())).thenReturn(staff);
		Mockito.when(imageService.saveImage(Mockito.anyInt(), Mockito.anyString(), Mockito.anyInt())).thenReturn("face-new");
		Mockito.when(staffService.updateById(Mockito.eq(staff))).thenReturn(true);
		Mockito.when(staffService.createStaffPhotoUploadRecord(Mockito.eq(staff))).thenReturn(77);
		Mockito.when(staffService.updatePersonCard(Mockito.eq(staff), Mockito.eq("AQID"), Mockito.eq("face-new"),
				Mockito.isNull(), Mockito.isNull(), Mockito.isNull())).thenReturn(null);
		Mockito.when(detailsService.save(Mockito.any(SmtFaceImgTaskDetails.class))).thenReturn(true);

		service.checkFacePic(checkFacePic());

		Mockito.verify(staffService).updatePersonCard(Mockito.eq(staff), Mockito.eq("AQID"), Mockito.eq("face-new"),
				Mockito.isNull(), Mockito.isNull(), Mockito.isNull());
	}

	private CheckFacePicDTO checkFacePic() {
		CheckFacePicVO pic = new CheckFacePicVO();
		pic.setStaffBadge("JA26086");
		pic.setFacePic("AQID");
		CheckFacePicDTO check = new CheckFacePicDTO();
		check.setParkId(5000021);
		check.setTaskName("batch-face");
		check.setFacePicUpLoad(Collections.singletonList(pic));
		return check;
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				field.set(target, value);
				return;
			} catch (NoSuchFieldException ignored) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name + " on " + target.getClass());
	}
}
