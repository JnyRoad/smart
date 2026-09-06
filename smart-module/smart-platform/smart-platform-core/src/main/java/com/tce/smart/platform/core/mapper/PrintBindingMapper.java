package com.tce.smart.platform.core.mapper;
import com.tce.smart.platform.api.dto.req.print.PrintBindingQuery;
import com.tce.smart.platform.core.entity.print.PrintBindingRule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import java.util.List;
/** 所有绑定修改先锁园区持久行，避免不同实例同时写入相交规则。 */
public interface PrintBindingMapper {
 List<String> companyParks(@Param("scopeId") String scopeId,@Param("personType") String personType);
 List<String> supplierParks(@Param("scopeId") String scopeId);
 String lockPark(@Param("parkId") String parkId);
 PrintBindingRule find(@Param("id") String id);
 List<PrintBindingRule> list(PrintBindingQuery query, RowBounds bounds);
 long count(PrintBindingQuery query);
 List<PrintBindingRule> candidates(@Param("parkId") String parkId,@Param("printItemType") String printItemType,@Param("personType") String personType,@Param("classificationCode") String classificationCode);
 int insert(PrintBindingRule rule);
 int update(PrintBindingRule rule);
}
