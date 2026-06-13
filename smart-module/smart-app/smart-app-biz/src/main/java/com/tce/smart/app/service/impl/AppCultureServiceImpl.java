package com.tce.smart.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.ao.AppCultureAo;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.entity.AppSubjectContentText;
import com.tce.smart.app.mapper.AppCultureMapper;
import com.tce.smart.app.service.AppCultureService;
import com.tce.smart.app.vo.AppCultureVo;
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
 *  企业文化实现
 *
 * @author lbw
 * @version 1.0
 * @date 2019/4/25 13:46
 **/
@Service
public class AppCultureServiceImpl extends ServiceImpl<AppCultureMapper, AppSubject> implements AppCultureService {

    @Autowired
    private AppCultureMapper appCultureMapper;

    /**
     *  查询已上线内容
     * @param page
     * @return
     */
    @Override
    public IPage<List<AppSubject>> getByPageOnline(Page page) {
        IPage<List<AppSubject>> appSubjectList = appCultureMapper.getByPageOnline(page);
        return appSubjectList;
    }

    /**
     *  查询已下线内容
     * @param page
     * @return
     */
    @Override
    public IPage<List<AppCultureVo>> getByPageDown(Page page) {
        IPage<List<AppCultureVo>> appSubjectList = appCultureMapper.getByPageDown(page);
        return appSubjectList;
    }

    /**
     *  查询未发布内容
     * @param page
     * @return
     */
    @Override
    public IPage<List<AppCultureVo>> getPageByNotRelease(Page page) {
        IPage<List<AppCultureVo>> appSubjectList = appCultureMapper.getPageByNotRelease(page);
        return appSubjectList;
    }

    /**
     *  企业文化详细信息
     * @param id 主键ID
     * @return
     */
    @Override
    public Result detailCulture(Integer id) {
        Result result = this.checkId(id);
        if (!(Boolean)result.getData()) {
            return result;
        }
        AppCultureVo appCultureVo = appCultureMapper.detailCulture(id);
        return new Result<>(appCultureVo);
    }

    /**
     *  下线操作
     * @param id 主键ID
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result downCulture(Integer id) {
        Result result = this.checkId(id);
        if (!(Boolean)result.getData()) {
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
        List<AppSubject> subjectList = appCultureMapper.downByOrderList(order);
        subjectList.forEach(subject ->{
            subject.setSubjectOrder(subject.getSubjectOrder()-1);
            subject.updateById();
        } );
        return new Result<>(appSubject.updateById());
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
        List<AppSubject> appSubjectList = appCultureMapper.downByOrderList(1);
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
        return new Result<>();
    }

    /**
     *  上移操作
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result moveCultureUp(Integer id) {
        Result result = this.checkId(id);
        if (!(Boolean)result.getData()) {
            return result;
        }
        AppSubject appSubject = appCultureMapper.selectById(id);
        if (appSubject.getSubjectOrder() <= 1) {
            return new Result<>(Boolean.FALSE,"非法操作");
        }
        AppSubject appSubjectUp = appCultureMapper.selectByOrder(appSubject.getSubjectOrder()-1);
        appSubject.setSubjectOrder(appSubject.getSubjectOrder()-1);
        appSubjectUp.setSubjectOrder(appSubject.getSubjectOrder()+1);
        appSubject.updateById();
        appSubjectUp.updateById();
        return new Result<>();
    }

    /**
     *  下移操作
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result moveCultureDown(Integer id) {
        Result result = this.checkId(id);
        if (!(Boolean)result.getData()) {
            return result;
        }
        AppSubject appSubject = appCultureMapper.selectById(id);
        if (appSubject.getSubjectOrder() <= 0) {
            return new Result<>(Boolean.FALSE,"非法操作");
        }
        int count = appCultureMapper.selectNum();
        if (appSubject.getSubjectOrder() == count) {
            return new Result<>(Boolean.FALSE,"非法操作");
        }
        AppSubject appSubjectDown = appCultureMapper.selectByOrder(appSubject.getSubjectOrder()+1);
        appSubject.setSubjectOrder(appSubject.getSubjectOrder()+1);
        appSubjectDown.setSubjectOrder(appSubject.getSubjectOrder()-1);
        appSubject.updateById();
        appSubjectDown.updateById();
        return new Result<>(appSubject.updateById());
    }

    /**
     *  删除主题
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
        if (appSubject.getDelFlag() == "0" && appSubject.getPublishFlag() == "2") {
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
        return new Result<>();
    }

    /**
     *  上线操作
     * @param id
     * @return
     */
    @Override
    public Result turnOnline(Integer id) {
        Result result = this.checkId(id);
        if (!(Boolean)result.getData()) {
            return result;
        }
        AppSubject appSubject = new AppSubject();
        //置为上线状态
        appSubject.setPublishFlag("1");
        appSubject.setSubjectOrder(1);
        appSubject.setUpdateTime(LocalDateTime.now());
        appSubject.setId(id);
        List<AppSubject> appSubjectList = appCultureMapper.downByOrderList(1);
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
        if (!(Boolean)result.getData()) {
            return result;
        }
        AppSubject appSubject = appCultureMapper.selectDown(id);
        if (ObjectUtils.isEmpty(appSubject)) {
            return new Result<>(Boolean.FALSE, "id有误");
        }
        appSubject.setPublishFlag("0");
        return new Result<>(appSubject.updateById());
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
        return new Result<>();
    }

    /**
     * 修改企业文化
     * @param appCultureAo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateCulture(AppCultureAo appCultureAo, Integer id) {
        Result result = this.checkAo(appCultureAo);
        if (!(Boolean)result.getData()) {
            return result;
        }
        AppSubject appSubject = new AppSubject();
        appSubject.setId(id);
        appSubject = appSubject.selectById();
        appSubject.setUpdateTime(LocalDateTime.now());
        appSubject.setSubjectName(appCultureAo. getSubjectName());
        AppSubjectContentText appSubjectContentText = new AppSubjectContentText();
        appSubjectContentText = appSubjectContentText.selectOne(new QueryWrapper<AppSubjectContentText>().eq("SUBJECT_ID",id));
        AppContentText appContentText = new AppContentText();
        appContentText.setId(appSubjectContentText.getContentTextId());
        appContentText = appContentText.selectById();
        appContentText.setUpdateTime(LocalDateTime.now());
        appContentText.setTextDesc(appCultureAo.getTextDesc());
        appContentText.setPicBinary(appCultureAo.getPicBinary().getBytes(StandardCharsets.UTF_8));
        appContentText.updateById();
        appSubject.updateById();
        return new Result<>();
    }

    /**
     * 置顶操作
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result topCulture(Integer id) {
        Result result = this.checkId(id);
        if (!(Boolean)result.getData()) {
            return result;
        }
        AppSubject appSubjectQuery = appCultureMapper.selectOne(new QueryWrapper<AppSubject>().eq("SUBJECT_ORDER", 0));
        if (!ObjectUtils.isEmpty(appSubjectQuery)) {
            return new Result<>(Boolean.FALSE, "存在已置顶主题");
        }
        AppSubject appSubject = new AppSubject();
        appSubject.setId(id);
        appSubject = appSubject.selectById();
        List<AppSubject> appSubjectList = appCultureMapper.downByOrderList(appSubject.getSubjectOrder());
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
        AppSubject appSubjectQuery = appCultureMapper.selectOne(new QueryWrapper<AppSubject>().eq("SUBJECT_ORDER", 0).eq("id",id));
        if (ObjectUtils.isEmpty(appSubjectQuery)) {
            return new Result<>(Boolean.FALSE, "非法操作");
        }
        appSubjectQuery.setSubjectOrder(1);
        List<AppSubject> appSubjectList = appCultureMapper.downByOrderList(1);
        appSubjectList.forEach(subject ->{
            subject.setSubjectOrder(subject.getSubjectOrder()+1);
            subject.updateById();
        });
        return new Result<>(appSubjectQuery.updateById());
    }

    /**
     *  新增
     * @param appCultureAo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addAppCulture(AppCultureAo appCultureAo){
        Result result = this.checkAo(appCultureAo);
        if (!(Boolean) result.getData()) {
            return result;
        }
        int subjectId = this.insertSubject(appCultureAo);
        int textId = 0;
        try {
            textId = this.insertContentText(appCultureAo);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.insertSubjectContentText(textId, subjectId);
        return new Result<>();
    }

    /**
     * 添加企业文化
     * @param appCultureAo
     * @return
     */
    public int insertSubject(AppCultureAo appCultureAo) {
        AppSubject appSubject = new AppSubject();
        appSubject.setSubjectName(appCultureAo.getSubjectName());
        //企业文化模块编号
        appSubject.setCatalogCode("8");
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
     * @param appCultureAo
     * @return
     */
    public int insertContentText(AppCultureAo appCultureAo) throws SQLException {
        AppContentText appContentText = new AppContentText();
        appContentText.setDelFlag("1");
        appContentText.setTextName("");
        appContentText.setTextDesc(appCultureAo.getTextDesc());
        appContentText.setTextOrder(0);
        appContentText.setPicBinary(appCultureAo.getPicBinary().getBytes(StandardCharsets.UTF_8));
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

    public Result checkAo(AppCultureAo appCultureAo) {
        if (StringUtils.isEmpty(appCultureAo.getSubjectName())) {
            return new Result<>(Boolean.FALSE,"标题名不能为空");
        }
        if (StringUtils.isEmpty(appCultureAo.getPicBinary())) {
            return new Result<>(Boolean.FALSE,"标题图片不能为空");
        }
        if (StringUtils.isEmpty(appCultureAo.getTextDesc())) {
            return new Result<>(Boolean.FALSE,"文本内容不能为空");
        }
        return new Result<>(Boolean.TRUE);
    }
}
