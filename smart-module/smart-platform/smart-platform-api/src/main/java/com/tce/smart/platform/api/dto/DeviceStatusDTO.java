package com.tce.smart.platform.api.dto;

import com.tce.smart.common.core.model.Result;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 人员卡片删除信息
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/4/18 .
 * @Modified By:
 */
@Data
@NoArgsConstructor
public class DeviceStatusDTO extends Result {
    private static final long serialVersionUID = -8787117565141488555L;
    private List<DeviceStatus> deviceStatusList;

    public DeviceStatusDTO(List<DeviceStatus> deviceStatusList,Integer code, String message) {
        super(code,message);
        this.deviceStatusList = deviceStatusList;
    }

    public static class DeviceStatusDTOBuilder {
        private Integer code;
        private String message;
        private List<DeviceStatus> deviceStatusList;
        DeviceStatusDTOBuilder() {
        }
        public static DeviceStatusDTOBuilder builder(){
            return new DeviceStatusDTOBuilder();
        }
        public DeviceStatusDTOBuilder code(final Integer code) {
            this.code = code;
            return this;
        }

        public DeviceStatusDTOBuilder message(final String message) {
            this.message = message;
            return this;
        }
        public DeviceStatusDTOBuilder deviceStatusList(final List<DeviceStatus> deviceStatusList) {
            this.deviceStatusList = deviceStatusList;
            return this;
        }

        public DeviceStatusDTO build() {
            return new DeviceStatusDTO(this.deviceStatusList,this.code, this.message);
        }
    }

    @Data
    public static class DeviceStatus implements Serializable{
        private static final long serialVersionUID = 9135855217348242307L;
        /**
         * 设备编号【必选】
         */
        private String deviceCode ;

        /**
         * 设备状态【必选】
         */
        private Integer deviceStatus;
    }
}
