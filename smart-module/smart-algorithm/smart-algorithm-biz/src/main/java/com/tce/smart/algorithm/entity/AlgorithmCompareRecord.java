package com.tce.smart.algorithm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author wuxinjian
 * @date 2020-02-05 16:09:01
 */
@Data
@Builder
@TableName("algorithm_compare_record")
@EqualsAndHashCode(callSuper = true)
public class AlgorithmCompareRecord extends Model<AlgorithmCompareRecord> {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.UUID)
	private String id;
	/**
	 * 算法类型：文通OCR（ocr_wentong）（身份证正面/反面）；阿里OCR（ocr_ali）（身份证正面）；百度离线静默活体（staticlive_baidu_offline）；百度离线人像比对（compare_baidu_offline）；中科视拓静默活体（staticlive_seeta）；中科视拓人像比对（compare_seeta）；飞搜人像比对（compare_faceall）；
	 */
	private String algorithmType;
	/**
	 * 唯一请求ID
	 */
	private String requestId;
	/**
	 * 图片AID
	 */
	private String imageAId;
	/**
	 * 图片BID
	 */
	private String imageBId;
	/**
	 * 比对相似度:0-1小数
	 */
	private Double similarity;
	/**
	 * 1成功,0失败
	 */
	private Integer isSuccess;
	/**
	 * 错误信息
	 */
	private String errorMessage;
	/**
	 * 请求耗时(ms)
	 */
	private Integer consumeTime;
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

}
