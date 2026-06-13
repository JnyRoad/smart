package com.tce.smart.platform.api.dto;

import com.tce.smart.common.core.model.Result;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 车位信息
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/4/18 .
 * @Modified By:
 */
@Data
@NoArgsConstructor
public class ParkingLotDTO extends Result {
    private static final long serialVersionUID = 5739905376963935841L;

    /**
     * 车位信息数据
     */
    private List<ParkingLot> parkingLotList;

    public ParkingLotDTO(List<ParkingLot> parkingLotList, Integer code, String message) {
        super(code,message);
        this.parkingLotList = parkingLotList;
    }

    public static class ParkingLotDTOBuilder {
        private Integer code;
        private String message;
        private List<ParkingLot> parkingLotList;
        ParkingLotDTOBuilder() {
        }
        public static ParkingLotDTOBuilder builder(){
            return new ParkingLotDTOBuilder();
        }
        public ParkingLotDTOBuilder code(final Integer code) {
            this.code = code;
            return this;
        }

        public ParkingLotDTOBuilder message(final String message) {
            this.message = message;
            return this;
        }
        public ParkingLotDTOBuilder parkingLotList(final List<ParkingLot> parkingLotList) {
            this.parkingLotList = parkingLotList;
            return this;
        }

        public ParkingLotDTO build() {
            return new ParkingLotDTO(this.parkingLotList,this.code, this.message);
        }
    }

    @Data
    public static class ParkingLot implements Serializable{

        private static final long serialVersionUID = 9144707537086711038L;
        /**
         * 车库编号【必选】
         */
        private String parkingCode ;

        /**
         * 车位总数【必选】
         */
        private Integer totalParkingSpace;

        /**
         * 剩余车位数【必选】
         */
        private Integer remainParkingSpace;
    }
}
