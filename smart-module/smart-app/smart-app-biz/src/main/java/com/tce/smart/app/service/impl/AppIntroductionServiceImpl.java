package com.tce.smart.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.ao.AppIntroductionAo;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.entity.AppSubjectContentText;
import com.tce.smart.app.mapper.AppIntroductionMapper;
import com.tce.smart.app.service.AppIntroductionService;
import com.tce.smart.app.vo.AppIntroductionVo;
import com.tce.smart.common.core.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 裕同简介实现
 *
 * @author lbw
 * @version 1.0
 * @date 2019/4/30 10:45
 **/
@Service
public class AppIntroductionServiceImpl extends ServiceImpl<AppIntroductionMapper, AppSubject> implements AppIntroductionService {

    @Autowired
    private AppIntroductionMapper appIntroductionMapper;

    /**
     *  查询已上线内容
     * @param page
     * @return
     */
    @Override
    public IPage<List<AppSubject>> getByPageOnline(Page page) {
        return appIntroductionMapper.getByPageOnline(page);
    }

    /**
     *  查询已下线内容
     * @param page
     * @return
     */
    @Override
    public IPage<List<AppIntroductionVo>> getByPageDown(Page page) {
        return appIntroductionMapper.getByPageDown(page);
    }

    /**
     *  查询未发布内容
     * @param page
     * @return
     */
    @Override
    public IPage<List<AppIntroductionVo>> getPageByNotRelease(Page page) {
        return appIntroductionMapper.getPageByNotRelease(page);
    }

    /**
     *  裕同简介详情
     * @param id 主键ID
     * @return
     */
    @Override
    public Result detailIntroduction(Integer id) {
        Result result = this.checkId(id);
        if (!(Boolean) result.getData()) {
            return result;
        }
        AppIntroductionVo appIntroductionVo = appIntroductionMapper.detailIntroduction(id);
        return new Result<>(appIntroductionVo);
    }

    /**
     *  添加裕同简介
     * @param appIntroductionAo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addAppIntroduction(AppIntroductionAo appIntroductionAo) {
        Result result = this.checkAo(appIntroductionAo);
        if (!(Boolean) result.getData()) {
            return result;
        }
        int subjectId = this.insertSubject(appIntroductionAo);
        int textId = 0;
        try {
            textId = this.insertContentText(appIntroductionAo);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.insertSubjectContentText(textId, subjectId);
        return new Result<>(Boolean.TRUE);
    }

    /**
     * 添加裕同简介主题
     * @param appIntroductionAo
     * @return
     */
    public int insertSubject(AppIntroductionAo appIntroductionAo) {
        AppSubject appSubject = new AppSubject();
        appSubject.setSubjectName(appIntroductionAo.getSubjectName());
        //裕同简介模块编号
        appSubject.setCatalogCode("7");
        appSubject.setParentSubject(0);
        appSubject.setSubjectUrl("");
        appSubject.setTopFlag("0");
        appSubject.setSubjectOrder(-1);
        appSubject.setPublishFlag("0");
        appSubject.setDelFlag("1");
        appSubject.setCreateTime(LocalDateTime.now());
        appSubject.setUpdateTime(LocalDateTime.now());
        appSubject.insert();
        int subjectId = appSubject.getId();
        return subjectId;
    }

    /**
     *  添加内容文本
     * @param appIntroductionAo
     * @return
     */
    public int insertContentText(AppIntroductionAo appIntroductionAo) throws SQLException {
        AppContentText appContentText = new AppContentText();
        appContentText.setDelFlag("1");
        appContentText.setTextName("");
        appContentText.setTextDesc(appIntroductionAo.getTextDesc());
        appContentText.setTextOrder(0);
        appContentText.setPicBinary(appIntroductionAo.getPicBinary().getBytes(StandardCharsets.UTF_8));
        appContentText.setCreateTime(LocalDateTime.now());
        appContentText.setUpdateTime(LocalDateTime.now());
        appContentText.insert();
        int textId = appContentText.getId();
        return textId;
    }

    /**
     *  添加主题文本关联
     * @param textId
     * @param subjectId
     * @return
     */
    public Result insertSubjectContentText(int textId, int subjectId) {
        AppSubjectContentText appSubjectContentText = new AppSubjectContentText();
        appSubjectContentText.setContentTextId(textId);
        appSubjectContentText.setSubjectId(subjectId);
        return new Result<>(appSubjectContentText.insert());
    }

    /**
     *  下线操作
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result downIntroduction(Integer id) {
        Result result = this.checkId(id);
        if (!(Boolean) result.getData()) {
            return result;
        }
        AppSubject appSubject = new AppSubject();
        appSubject.setId(id);
        appSubject = appSubject.selectById(id);
        appSubject.setPublishFlag("2");
        appSubject.setUpdateTime(LocalDateTime.now());
        int order = appSubject.getSubjectOrder();
        //置为未排序状态 -1:未排序 0:置顶 1-xx排序状态
        appSubject.setSubjectOrder(-1);
        List<AppSubject> subjectList = appIntroductionMapper.downByOrderList(order);
        subjectList.forEach(subject ->{
            subject.setSubjectOrder(subject.getSubjectOrder()-1);
            subject.updateById();
        } );
        return new Result<>(appSubject.updateById());
    }

    /**
     *  上移操作
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result moveIntroductionUp(Integer id) {
        Result result = this.checkId(id);
        if (!(Boolean) result.getData()) {
            return result;
        }
        AppSubject appSubject = appIntroductionMapper.selectById(id);
        if (appSubject.getSubjectOrder() <= 1) {
            return new Result<>(Boolean.FALSE,"非法操作");
        }
        AppSubject appSubjectUp = appIntroductionMapper.selectByOrder(appSubject.getSubjectOrder()-1);
        appSubject.setSubjectOrder(appSubject.getSubjectOrder()-1);
        appSubjectUp.setSubjectOrder(appSubject.getSubjectOrder()+1);
        appSubject.updateById();
        appSubjectUp.updateById();
        return new Result<>(Boolean.TRUE);
    }

    /**
     *  下移操作
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result moveIntroductionDown(Integer id) {
        Result result = this.checkId(id);
        if (!(Boolean) result.getData()) {
            return result;
        }
        AppSubject appSubject = appIntroductionMapper.selectById(id);
        if (appSubject.getSubjectOrder() <= 0) {
            return new Result<>(Boolean.FALSE,"非法操作");
        }
        int count = appIntroductionMapper.selectNum();
        if (appSubject.getSubjectOrder() == count) {
            return new Result<>(Boolean.FALSE,"非法操作");
        }
        AppSubject appSubjectDown = appIntroductionMapper.selectByOrder(appSubject.getSubjectOrder()+1);
        appSubject.setSubjectOrder(appSubject.getSubjectOrder()+1);
        appSubjectDown.setSubjectOrder(appSubject.getSubjectOrder()-1);
        appSubject.updateById();
        appSubjectDown.updateById();
        return new Result<>(appSubject.updateById());
    }

    /**
     *  删除操作
     * @param id
     * @return
     */
    @Override
    public Result delCulture(Integer id) {
        Result result = this.checkId(id);
        if (!(Boolean) result.getData()) {
            return result;
        }
        AppSubject appSubject = new AppSubject();
        appSubject.setId(id);
        appSubject = appSubject.selectById();
        if (appSubject.getDelFlag().equals("0") && appSubject.getPublishFlag().equals("2")) {
            return new Result<>(Boolean.FALSE, "非法操作");
        }
        appSubject.setDelFlag("0");
        appSubject.setUpdateTime(LocalDateTime.now());
        AppSubjectContentText appSubjectContentText = new AppSubjectContentText();
        appSubjectContentText = appSubjectContentText.selectOne(new QueryWrapper<AppSubjectContentText>().eq("SUBJECT_ID",id));
        AppContentText appContentText = new AppContentText();
        appContentText.setId(appSubjectContentText.getContentTextId());
        appContentText.setDelFlag("0");
        appSubject.updateById();
        appContentText.updateById();
        return new Result<>(Boolean.TRUE);
    }

    /**
     *  上线操作
     * @param id
     * @return
     */
    @Override
    public Result turnOnline(Integer id) {
        Result result = this.checkId(id);
        if (!(Boolean) result.getData()) {
            return result;
        }
        AppSubject appSubject = new AppSubject();
        //置为上线状态
        appSubject.setPublishFlag("1");
        appSubject.setSubjectOrder(1);
        appSubject.setUpdateTime(LocalDateTime.now());
        appSubject.setId(id);
        List<AppSubject> appSubjectList = appIntroductionMapper.downByOrderList(1);
        appSubjectList.forEach(subject-> {
            subject.setSubjectOrder(subject.getSubjectOrder()+1);
            subject.updateById();
        });
        return new Result<>(appSubject.updateById());
    }

    /**
     *  已下线转待发布
     * @param id
     * @return
     */
    @Override
    public Result toNoRelease(Integer id) {
        Result result = this.checkId(id);
        if (!(Boolean) result.getData()) {
            return result;
        }
        AppSubject appSubject = appIntroductionMapper.selectDown(id);
        if (ObjectUtils.isEmpty(appSubject)) {
            return new Result<>(Boolean.FALSE, "id有误");
        }
        appSubject.setPublishFlag("0");
        return new Result<>(appSubject.updateById());
    }

    /**
     *  修改操作
     * @param appIntroductionAo
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateIntroduction(AppIntroductionAo appIntroductionAo, Integer id) {
        Result result = this.checkAo(appIntroductionAo);
        if (!(Boolean) result.getData()) {
            return result;
        }
        AppSubject appSubject = new AppSubject();
        appSubject.setId(id);
        appSubject = appSubject.selectById();
        appSubject.setUpdateTime(LocalDateTime.now());
        appSubject.setSubjectName(appIntroductionAo. getSubjectName());
        AppSubjectContentText appSubjectContentText = new AppSubjectContentText();
        appSubjectContentText = appSubjectContentText.selectOne(new QueryWrapper<AppSubjectContentText>().eq("SUBJECT_ID",id));
        AppContentText appContentText = new AppContentText();
        appContentText.setId(appSubjectContentText.getContentTextId());
        appContentText = appContentText.selectById();
        appContentText.setUpdateTime(LocalDateTime.now());
        appContentText.setTextDesc(appIntroductionAo.getTextDesc());
        appContentText.setPicBinary(appIntroductionAo.getPicBinary().getBytes(StandardCharsets.UTF_8));
        appContentText.updateById();
        appSubject.updateById();
        return new Result<>(Boolean.TRUE);
    }

    /**
     *  置顶操作
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result topIntroduction(Integer id) {
        Result result = this.checkId(id);
        if (!(Boolean) result.getData()) {
            return result;
        }
        AppSubject appSubjectQuery = appIntroductionMapper.selectOne(new QueryWrapper<AppSubject>().eq("SUBJECT_ORDER", 0));
        if (!ObjectUtils.isEmpty(appSubjectQuery)) {
            return new Result<>(Boolean.FALSE, "存在已置顶主题");
        }
        AppSubject appSubject = new AppSubject();
        appSubject.setId(id);
        appSubject = appSubject.selectById();
        List<AppSubject> appSubjectList = appIntroductionMapper.downByOrderList(appSubject.getSubjectOrder());
        appSubjectList.forEach(subject ->{
            subject.setSubjectOrder(subject.getSubjectOrder()-1);
            subject.updateById();
        });
        appSubject.setSubjectOrder(0);
        return new Result<>(appSubject.updateById());
    }

    /**
     *  取消置顶
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result cancelTop(Integer id) {
        Result result = this.checkId(id);
        if (!(Boolean) result.getData()) {
            return result;
        }
        AppSubject appSubjectQuery = appIntroductionMapper.selectOne(new QueryWrapper<AppSubject>().eq("SUBJECT_ORDER", 0).eq("id",id));
        if (ObjectUtils.isEmpty(appSubjectQuery)) {
            return new Result<>(Boolean.FALSE, "非法操作");
        }
        appSubjectQuery.setSubjectOrder(1);
        List<AppSubject> appSubjectList = appIntroductionMapper.downByOrderList(1);
        appSubjectList.forEach(subject ->{
            subject.setSubjectOrder(subject.getSubjectOrder()+1);
            subject.updateById();
        });
        return new Result<>(appSubjectQuery.updateById());
    }

    /**
     * 批量上线
     * @param ids
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result batchOnline(int[] ids) {
        if (ids == null || ids.length == 0) {
            return new Result<>(Boolean.FALSE, "数组不能为空");
        }
        int size = ids.length;
        List<AppSubject> appSubjectList = appIntroductionMapper.downByOrderList(1);
        appSubjectList.forEach(subject-> {
            subject.setSubjectOrder(subject.getSubjectOrder()+size);
            subject.updateById();
        });
        AppSubject appSubject = new AppSubject();
        for (int i = 0; i<size; i++) {
            appSubject.setId(ids[i]);
            appSubject = appSubject.selectById();
            appSubject.setSubjectOrder(i+1);
            appSubject.setUpdateTime(LocalDateTime.now());
            appSubject.setPublishFlag("1");
            appSubject.updateById();
        }
        return new Result<>(Boolean.TRUE);
    }

    /**
     *  批量已下线转待发布
     * @param ids
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result batchPendRelease(int[] ids) {
        if (ids == null || ids.length == 0) {
            return new Result<>(Boolean.FALSE, "数组不能为空");
        }
        int size = ids.length;
        AppSubject appSubject = new AppSubject();
        for (int i = 0; i<size; i++) {
            appSubject.setId(ids[i]);
            appSubject = appSubject.selectById();
            if (!"2".equals(appSubject.getPublishFlag())) {
                return new Result<>(Boolean.FALSE,"第"+(i+1)+"条数据有误");
            }
            appSubject.setPublishFlag("0");
            appSubject.updateById();
        }
        return new Result<>(Boolean.TRUE);
    }

    public Result checkId(Integer id) {
        if (id == null) {
            return new Result<>(Boolean.FALSE,"id不能为空");
        }
        AppSubject appSubject = new AppSubject();
        appSubject.setId(id);
        appSubject = appSubject.selectById();
        if (appSubject == null) {
            return new Result<>(Boolean.FALSE, "找不到该主题");
        }
        return new Result<>(Boolean.TRUE);
    }

    public Result checkAo(AppIntroductionAo appIntroductionAo) {
        if (StringUtils.isEmpty(appIntroductionAo.getSubjectName())) {
            return new Result<>(Boolean.FALSE,"标题名不能为空");
        }
        if (StringUtils.isEmpty(appIntroductionAo.getPicBinary())) {
            return new Result<>(Boolean.FALSE,"标题图片不能为空");
        }
        if (StringUtils.isEmpty(appIntroductionAo.getTextDesc())) {
            return new Result<>(Boolean.FALSE,"文本内容不能为空");
        }
        return new Result<>(Boolean.TRUE);
    }
}
