package com.tce.smart.algorithm.util;

import com.tce.smart.algorithm.api.annotation.Desc;
import com.tce.smart.algorithm.api.dto.resp.ItemDTO;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: CardUtils
 * @Package com.tce.operator.jsiot.phone.util
 * @Description:
 * @Author wuxinjian
 * @Date 2018/12/25 17:26
 * @Version V1.0
 */
@UtilityClass
public class WenTongOcrUtils {

	private final ConcurrentHashMap<String,Method> classMethod = new ConcurrentHashMap<>(64);

    /**
     * 接收识别参数返回 String 的字符串用于后续加密等操作
     */
    public String setRecgnPlainParam(String imgBase64, String type, String option, String password) {
        return getOneLine(imgBase64) + "==##" + type + "==##" + option + "==##" + password;
    }

    private String getOneLine(String withr) {
        StringReader sr = new StringReader(withr);
        BufferedReader br = new BufferedReader(sr);
        String line;
        StringBuilder temp = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                temp.append(line);
            }
            br.close();
            sr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp.toString();
    }

	public <T> T convertToBean(List<ItemDTO> itemDTOList, Class<T> tClass) throws Exception {
	T t = tClass.newInstance();
		Field[] fields = tClass.getDeclaredFields();
		itemDTOList.forEach(item -> {
			try {
				for (Field field : fields) {
					Desc desc = field.getAnnotation(Desc.class);
					String fieldName = desc.type();
					if (item.getDesc().equals(fieldName)) {
						String name = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
						String keyMethod = tClass.getName() + name;
						Method method;
						if(Objects.nonNull(classMethod.get(keyMethod))){
							method = classMethod.get(keyMethod);
						} else {
							method = tClass.getMethod("set" + name, field.getType());
							classMethod.put(keyMethod,method);
						}
						method.invoke(t,item.getContent());
					}
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		});
		return t;
	}
}
