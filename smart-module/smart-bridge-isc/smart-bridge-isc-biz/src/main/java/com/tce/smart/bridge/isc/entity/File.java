package com.tce.smart.bridge.isc.entity;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import com.tce.smart.bridge.isc.annotation.Family;

import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-file
 * @ClassName: Image
 * @Author jinbo
 * @Date 2019/11/16
 */
@Data
@Component
@com.tce.smart.bridge.isc.annotation.Table(name="i_file")
public class File extends Table {
	@Family
	private FileFamily file;

	public byte[] get(){
		if(Objects.isNull(this.file)){
			return null;
		}
		return Base64Utils.decodeFromString(this.file.getData());
	}

	public void put(byte[] file){
		if(Objects.isNull(this.file)){
			this.file = new FileFamily();
		}
		this.file.setData(Base64Utils.encodeToString(file));
	}

	public void setFileType(Integer fileType) {

		if(Objects.isNull(this.file)){
			this.file = new FileFamily();
		}
		this.file.setFileType(fileType);
	}
}
