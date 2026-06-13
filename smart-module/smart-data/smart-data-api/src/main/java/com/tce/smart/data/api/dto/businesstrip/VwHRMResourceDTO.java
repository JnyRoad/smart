package com.tce.smart.data.api.dto.businesstrip;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工表
 *
 * @author liangyuan
 * @date 2019-06-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VwHRMResourceDTO extends Model<VwHRMResourceDTO> {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -2939044796684576727L;

	private Integer id;
	private String workCode;
	private String lastName;
	private String telePhone;
	private String mobile;
	private String email;

}
