package com.tce.smart.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.AppealAreaAo;
import com.tce.smart.app.ao.fore.AppSubjectAO;
import com.tce.smart.app.dto.AppQuestionDto;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.vo.AppQuestionVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.tce.smart.app.vo.AppSubjectDetailsVo;
import com.tce.smart.app.vo.AppSubjectVo;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 主题信息
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:44:43
 */
@Mapper
public interface AppSubjectMapper extends BaseMapper<AppSubject> {


	/**
	 * 通过主题分类获得主题
	 * @param page
	 * @param catalogCode
	 * @return
	 */
	IPage<AppSubject> getAppSubjectPage(Page page, @Param("publishFlag") String publishFlag, @Param("catalogCode") String catalogCode, @Param("parkId") Integer parkId);

	/**
	 * 通过主题分类获得主题
	 * @param page
	 * @param catalogCode
	 * @return
	 */
	IPage<AppSubject> getAppSubjectPageByParkId(Page page, @Param("query") AppealAreaAo appealAreaAo, @Param("parkIds") List<Integer> parkIds);

	/**
	 * app端获取主题
	 * @param page
	 * @param catalogCode
	 * @return
	 */
	IPage<AppSubjectAO> getAppSubjectListByApp(Page page, @Param("query") AppealAreaAo appealAreaAo, @Param("parkIds") List<Integer> parkIds);


	/**
	 * 批量修改序号
	 * @param operate
	 * @param frist
	 * @param last
	 */
	void updateBatchOrder(@Param("operate") String operate, @Param("catalogCode") String catalogCode, @Param("frist") Integer frist, @Param("last") Integer last );

	/**
	 * 通过ID获取排序序号
	 * @param id
	 * @return
	 */
	Integer selectOrder(@Param("id")Integer id);

	/**
	 * 通过序号获取ID(已发布)
	 * @param order
	 * @return
	 */
	Integer selectId(@Param("order") Integer order, @Param("catalogCode") String catalogCode);

	/**
	 * 更改指定ID主题的序号
	 * @param id
	 * @param order
	 */
	void updateOrder(@Param("id") Integer id, @Param("order") Integer order);

	/**
	 * 通过主题id查找图片
	 * @param id
	 * @return
	 */
	AppContentText selectText(@Param("id") Integer id);

	/**
	 * 通过主题id查找图片 不查询二进制数据
	 * @param id
	 * @return
	 */
	AppContentText selectTextNew(@Param("id") Integer id);

	/**
	 * 根据主题id找文本id
	 * @param subjectId
	 * @return
	 */
	Integer selectTextId(@Param("subjectId") Integer subjectId);

    void addBootSubject(@Param("subject") AppSubject appSubject);

	IPage<AppSubject> getAppQuestionPage(Page page, @Param("query") AppQuestionDto appQuestionDto);

	AppQuestionVo getAppQuestion(@Param("query") Integer id);


}
