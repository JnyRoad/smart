package com.tce.smart.schedule.service.platform;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtStaff;

/**
 * @author sunfujian
 * @since 2021/9/9 20:18
 */
public interface SmtStaffTaskService extends IService<SmtStaff> {
	/**
	 * 同步许昌员工人脸图片信息
	 */
	void syncXCStaffPhoto();

	/**
	 * 同步员工人脸图片信息
	 *
	 * @param type 获取图片方式，1-图库接口，2-共享目录
	 */
	void syncStaffNoPhoto(Integer type);

	Boolean updateStaffPhotoXc(Integer badge);
}
