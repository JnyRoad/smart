package com.tce.smart.admin;

import com.ulisesbocchio.jasyptspringboot.encryptor.DefaultLazyEncryptor;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.core.env.StandardEnvironment;

/**
 * 加解密单元测试
 */
public class SmartAdminApplicationTest {
//	@Test
	public static void testJasypt() {
		String encryptorPassword = System.getProperty("jasypt.encryptor.password", System.getenv("JASYPT_ENCRYPTOR_PASSWORD"));
		if (encryptorPassword == null || encryptorPassword.trim().isEmpty()) {
			throw new IllegalStateException("jasypt.encryptor.password is not configured");
		}
		System.setProperty("jasypt.encryptor.password", encryptorPassword);
		StringEncryptor stringEncryptor = new DefaultLazyEncryptor(new StandardEnvironment());

		//加密方法
		System.out.println(stringEncryptor.encrypt("example"));
	}

	public static Boolean checkPassword(String password){

		if(password.length() < 8){
			//长度小于8位
			return false;
		} else if(!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$")){
			//没有包含数字、大写字母、小写字母 3种字符
			return false;
		}

		boolean isStrong = true;
		for(int i = 0;i < password.toCharArray().length - 2;i++){
			char c1 = password.charAt(i);
			char c2 = password.charAt(i+1);
			char c3 = password.charAt(i+2);
			//先判断c1是否为数字或字母
			if(!Character.isDigit(c1) && !Character.isLowerCase(c1) && !Character.isUpperCase(c2)){
				//非数字和字母
				continue;
			}
			if(c1 == c2 && c1 == c3){
				//三个相同的字符
				isStrong = false;
				break;
			} else if((c2 - c1) == 1 && (c3 - c2) == 1){
				//三个连续的字符
				isStrong = false;
				break;
			}
		}
		return isStrong;
	}
}
