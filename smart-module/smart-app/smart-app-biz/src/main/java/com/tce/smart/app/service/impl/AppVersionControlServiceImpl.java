package com.tce.smart.app.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.enums.ExceptionType;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.RegexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.entity.AppVersionControl;
import com.tce.smart.app.mapper.AppVersionControlMapper;
import com.tce.smart.app.service.AppVersionControlService;

import java.util.List;

/**
 * App版本控制
 *
 * @author mingkai.wu
 * @date 2019-04-25 11:31:36
 */
@Service
public class AppVersionControlServiceImpl extends ServiceImpl<AppVersionControlMapper, AppVersionControl> implements AppVersionControlService {
    @Autowired
	private AppVersionControlMapper mapper;

	@Override
	public Result updateVersionById(AppVersionControl appVersionControl) {

		if(appVersionControl == null)
		{
			return new Result<>(Boolean.FALSE, "APP版本参数不能为空");
		}
		if(!RegexUtils.matchCode(appVersionControl.getVersionCode()))
		{
			return new Result<>(Boolean.FALSE, "版本号格式错误");
		}
		if(!RegexUtils.matchName(appVersionControl.getVersionDesc()))
		{
			return new Result<>(Boolean.FALSE, "升级信息描述只允许汉字、字母与数字的组合，最长为30个字符");
		}
		Integer count=mapper.selectCount(Wrappers.<AppVersionControl> query().lambda()
				.eq(AppVersionControl::getVersionCode, appVersionControl.getVersionCode()));
		if(count>0)
		{
			return new Result<>(Boolean.FALSE, "版本号已存在请重新填写");
		}
		return new Result<>(this.updateById(appVersionControl));
	}

	@Override
	public Result downVersionById(Integer id) {
		return null;
	}

    @Override
	public Result removeVersionById(Integer id) {
		AppVersionControl appVersionControl = this.getById(id);
		appVersionControl.setDelFlag("0");
		return new Result<>(this.updateById(appVersionControl));
	}

	@Override
	public IPage<List<AppVersionControl>> getSmtAreaPage(Page page, AppVersionControl appVersionControl) {
		return null;
	}

	@Override
	public Result addAppVersionControl(AppVersionControl appVersionControl) {

		if(appVersionControl==null)
		{
			return new Result<>(Boolean.FALSE, "APP版本参数不能为空");
		}
		if(!RegexUtils.matchCode(appVersionControl.getVersionCode()))
		{
			return new Result<>(Boolean.FALSE, "版本号格式错误");
		}
		if(!RegexUtils.matchName(appVersionControl.getVersionDesc()))
		{
			return new Result<>(Boolean.FALSE, "升级信息描述只允许汉字、字母与数字的组合，最长为30个字符");
		}
		Integer count=mapper.selectCount(Wrappers.<AppVersionControl> query().lambda()
		    .eq(AppVersionControl::getVersionCode, appVersionControl.getVersionCode()));
		if(count>0)
		{
			return new Result<>(Boolean.FALSE, "版本号已存在请重新填写");
		}
		return new Result<>(this.save(appVersionControl));
	}

    @Override
    public IPage<AppVersionControl> selectByPage(Page<AppVersionControl> page, AppVersionControl appVersonControl) {
        return this.page(page, Wrappers.query(appVersonControl));
    }

	@Override
	public AppVersionControl selectById(Integer id) {
		//TODO 判断逻辑,有任何业务异常,使用throw new TCEException(ExceptionType eType);
		return this.baseMapper.selectById(id);
	}

	@Override
	public Integer saveAppVersionControl(AppVersionControl appVersonControl) {
		//TODO 判断各个字段合法性与其他逻辑处理,有任何业务异常,使用throw new TCEException(ExceptionType eType);
		this.save(appVersonControl);
		return appVersonControl.getId();
	}

    @Override
    public void updateAppVersionControl(AppVersionControl appVersonControl) {
        //TODO 判断各个字段合法性与其他逻辑处理,有任何业务异常,使用throw new TCEException(ExceptionType eType);
        this.updateById(appVersonControl);
    }

    @Override
    public void deleteById(Integer id) {
        //TODO 判断逻辑,有任何业务异常,使用throw new TCEException(ExceptionType eType);
        this.removeById(id);
    }
}
