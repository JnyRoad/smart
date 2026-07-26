package com.tce.smart.app.service.fore;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** 换绑手机只能由已认证会话完成，并且必须先完成服务端绑定的旧手机号验证。 */
public class PhoneChangeSecurityContractTest {

    @Test
    public void phoneChangeEndpointsUseScopedPostBodiesInsteadOfUrlParameters() throws IOException {
        String controller = read("src/main/java/com/tce/smart/app/controller/fore/SettingServiceController.java");
        String api = read("../../../smart-app-uniapp/api/api-mine.js");

        assertTrue("旧手机号验证码必须由已认证会话触发", controller.contains("@PostMapping(\"/phone/old/send\")"));
        assertTrue("旧手机号校验必须使用最小 JSON 请求体", controller.contains("@PostMapping(\"/phone/old/verify\")"));
        assertTrue("新手机号发送必须使用最小 JSON 请求体", controller.contains("@PostMapping(\"/phone/new/send\")"));
        assertTrue("确认换绑必须使用最小 JSON 请求体", controller.contains("@PostMapping(\"/phone/new/confirm\")"));
        assertFalse("旧 GET 查询串入口不能继续保留", controller.contains("/updatephone/"));
        assertTrue("UniApp 不能将手机号或验证码放入 URL", api.contains("axios.post(API_SETTING_OLDPHONE_SEND"));
        assertFalse("UniApp 不能复用密码找回挑战", api.contains("apiPassword"));
        assertFalse("UniApp 不能继续用 GET 查询串传换绑信息", api.contains("API_SETTING_VERIFY_OLDPHONE}?"));
    }

    @Test
    public void serverRequiresOldPhoneVerificationBeforeNewPhoneActions() throws IOException {
        String service = read("src/main/java/com/tce/smart/app/service/fore/impl/SettingServiceImpl.java");

        assertTrue("旧手机号验证必须保存与当前会话绑定的短时授权", service.contains("PHONE_CHANGE_OLD_VERIFIED_KEY"));
        assertTrue("旧手机号授权必须绑定账号 ID 和旧号指纹，不能仅按可变手机号或前端值判断",
                service.contains("user.getId()") && service.contains("phoneFingerprint(currentStaffPhone())"));
        assertTrue("新手机号发送前必须检查旧手机号验证", service.contains("requireOldPhoneVerified()"));
        assertTrue("换绑提交前必须检查旧手机号验证", service.contains("clearOldPhoneVerified()"));
        assertTrue("换绑成功后必须清除旧手机号验证，避免重放", service.contains("stringRedisTemplate.delete(phoneChangeAuthKey())"));
    }

    private String read(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
