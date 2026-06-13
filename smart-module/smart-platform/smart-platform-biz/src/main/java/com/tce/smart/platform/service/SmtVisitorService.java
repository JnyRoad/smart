package com.tce.smart.platform.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.VisitorAgainReqDTO;
import com.tce.smart.platform.api.dto.req.VisitorRecordReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchVisitorDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.VisitorListRespDTO;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.AreaDeviceSnapRespDTO;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.ParkVisitorRespDTO;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.vo.*;

import java.util.List;

/**
 * 访客表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
public interface SmtVisitorService extends IService<SmtVisitor> {

	/**
	 * @Title:TODO
	 * @Param :
	 * @Return :Object
	 * @Exception :
	 * @Author:TCE-Liangyuan
	 * @Date： 2019年4月15日 上午9:15:08
	 */

	Boolean updateVisitorStatusById(SmtVisitor smtVisitor);

	/**
	 * @Title:TODO
	 * @Param :
	 * @Return :Object
	 * @Exception :
	 * @Author:TCE-Liangyuan
	 * @Date： 2019年4月15日 上午11:19:20
	 */

	SmtVisitor saveSmtVisitor(SaveSmtVisitor saveSmtVisitor);

	/**
	 * 公众号预约
	 * @param saveWechatSmtVisitor
	 * @return
	 */
	SmtVisitor saveWechatSmtVisitor(SaveWechatSmtVisitorDTO saveWechatSmtVisitor);

	/**
	 * 微信公众号预约 再约一次
	 * @param id
	 * @return
	 */
	Boolean wechatSmtVisitorAgain(VisitorAgainReqDTO visitorAgainReqDTO);

	SmtVisitor searchReceptionistForApp(SmtVisitor smtVisitor);

	/**
	 * @Title:TODO
	 * @Param :
	 * @Return :Object
	 * @Exception :
	 * @Author:TCE-Liangyuan
	 * @Date： 2019年4月16日 下午4:11:15
	 */

	SearchVisitorDetail searchVisitorDetailById(Long id, SmtSnapVehicleService smtSnapVehicleService);

	IPage<SearchTodayVisitor> getTodayVisitor(Page page);

	SearchTadayVisitorDetail getNewSnapVisitor();

	/**
	 * 抓拍车辆如果是访客的则补全车辆记录信息，否则不处理
	 * @param entity 抓拍车辆信息
	 * @return
	 */
	void visitorSnapHandle(AddSnapVehicleDTO entity, SmtSnapVehicleService smtSnapVehicleService);

	SmtVisitor searchReceptionist(SmtVisitor smtVisitor);

	IPage<SearchSmtVisitorVO> getSmtVisitorPage(Page page, SearchSmtVisitorDTO searchSmtVisitorDTO);

	SearchAppVisitorDetailVO searchAppVisitorDetailById(Long id);

	IPage<SearchAppSmtVisitorVO> searchAppVisitorPage(Page page, SearchAppVisitorDTO searchAppVisitorDTO);

	void addFellowVisitor(AddFellowVisitorDTO addFellowVisitorDTO);

	Integer searchAppVisitorCount(SearchAppVisitorCountDTO searchAppVisitorCountDTO);

	void addWechatFellowVisitor(AddWechatFellowVisitorDTO addWechatFellowVisitorDTO);

	//访客当日进门总数
	SnapPersonCountVO searchComeInToday();
    //访客当日出门总数
	SnapPersonCountVO searchVisitorOutToday();
	//访客分析数据查询
	List<SearchVisitorAnalysisVO> searchVisitorAnalysisToday(Integer parkId);
	//访客设备分析数据
	List<SearchVisitorDeviceVO> searchVisitorDeviceToday();
    //访客设备抓拍的最新数据
	List<SearchVisitorDeviceAnalysisVO> searchVisitorDeviceAnalysisToday();
	//检测身份证号是否是黑名单
	Boolean checkBlackVisitor(SmtVisitor smtVisitor);

	/**
	 * 获取设备的最后抓拍记录
	 * @param dd
	 * @param deviceId
	 */
	void getSnapPersonLasted(SearchVisitorDeviceAnalysisVO dd, String deviceId);

	Result getVisitorRefuseType();

	//检测车牌号是否是黑名单
	Boolean checkBlackVehicle(SmtVisitor smtVisitor);

	/**
	 * 获取访问者预约记录
	 */
	IPage<VisitorListRespDTO> getVisitRecord(Page page,String visitorPhone);


	SearchVisitorDetailRespDTO searchVisitorById(Long id);

	SearchVisitorDetailRespDTO searchVisitorByCode(String code);

	Boolean delSmsCode(Long id);

	Boolean smbPutPhoto(Long id);

	/**
	 * 重新下发访客设备权限
	 * @param id
	 * @return
	 */
	Boolean repeatVisitorDeviceAuth(Long id);

	Boolean updateHfStatus(SmtVisitor smtVisitor);

	SmtVisitor saveHfVisitor(SaveSmtVisitor saveSmtVisitor);

	/**
	 * 合肥访客邀约
	 * @param saveSmtVisitor
	 */
	String saveHfInvitation(SaveSmtVisitor saveSmtVisitor, String token);

	void updateOaStatusTask();
}
