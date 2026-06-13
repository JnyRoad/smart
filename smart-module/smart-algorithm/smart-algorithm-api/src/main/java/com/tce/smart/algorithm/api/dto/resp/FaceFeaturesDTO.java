package com.tce.smart.algorithm.api.dto.resp;

import com.tce.smart.common.core.model.Result;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: FaceFeaturesDTO
 * @date: 2020-07-03 15:32
 * @author: wuling
 * @version: 1.0
 */
@Data
@NoArgsConstructor
public class FaceFeaturesDTO extends Result {
	private static final long serialVersionUID = -5878833259131060830L;

	/**
	 * 人脸特征值数据,float字符串,逗号分隔
	 */
	private String faceFeature;

	public FaceFeaturesDTO(String faceFeature, Integer code, String message) {
		super(code,message);
		this.faceFeature = faceFeature;
	}

	public static class FaceFeaturesDTOBuilder {
		private Integer code;
		private String message;
		private String faceFeature;
		FaceFeaturesDTOBuilder() {
		}
		public static FaceFeaturesDTOBuilder builder(){
			return new FaceFeaturesDTOBuilder();
		}
		public FaceFeaturesDTOBuilder code(final Integer code) {
			this.code = code;
			return this;
		}

		public FaceFeaturesDTOBuilder message(final String message) {
			this.message = message;
			return this;
		}
		public FaceFeaturesDTOBuilder faceFeature(final String faceFeature) {
			this.faceFeature = faceFeature;
			return this;
		}

		public FaceFeaturesDTO build() {
			return new FaceFeaturesDTO(this.faceFeature,this.code, this.message);
		}
	}
}
