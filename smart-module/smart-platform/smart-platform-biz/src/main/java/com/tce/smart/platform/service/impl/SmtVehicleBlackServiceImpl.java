package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtVehicleBlack;
import com.tce.smart.platform.core.mapper.SmtVehicleBlackMapper;
import com.tce.smart.platform.service.SmtVehicleBlackService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 车辆黑名单表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:02
 */
@Service
@AllArgsConstructor
public class SmtVehicleBlackServiceImpl extends ServiceImpl<SmtVehicleBlackMapper, SmtVehicleBlack> implements SmtVehicleBlackService {

}
