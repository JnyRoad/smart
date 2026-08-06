package com.tce.smart.admin.controller;

import com.tce.smart.admin.api.dto.SocialDetailsSecretRotateReqDTO;
import com.tce.smart.admin.api.dto.SocialDetailsUpdateReqDTO;
import com.tce.smart.admin.api.entity.SysSocialDetails;
import com.tce.smart.admin.service.SysSocialDetailsService;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 三方账号编辑契约：查询不泄露密钥，普通编辑不要求回传密钥，轮换必须走显式受权入口。 */
public class SocialDetailsControllerContractTest {

    @Test
    public void socialUpdateUsesSecretFreeRequestAndHasExplicitSecretRotationRoute() throws IOException {
        String controller = read("src/main/java/com/tce/smart/admin/controller/SocialDetailsController.java");

        assertTrue("编辑请求必须是排除 appSecret 的专用 DTO",
                controller.contains("SocialDetailsUpdateReqDTO sysSocialDetails"));
        assertTrue("密钥轮换必须是独立 PUT 路由", controller.contains("@PutMapping(\"/secret/{id}\")"));
        assertTrue("轮换密钥必须要求与编辑一致的客户端管理权限",
                controller.contains("@pms.hasPermission('sys_client_edit')"));
        assertFalse("普通编辑不得直接 updateById 客户端提交的实体，以免空密钥覆盖存量密钥",
                controller.contains("updateById(sysSocialDetails)"));
    }

    @Test
    public void socialManagementUiUsesBackendPermissionsAndDoesNotRequireSecretWhenEditing() throws IOException {
        String page = read("../../../smart-ui/src/views/admin/social/index.vue");
        String option = read("../../../smart-ui/src/const/crud/admin/sys-social-details.js");

        assertTrue("管理页面权限名必须与后端 sys_client_* 一致", page.contains("permissions.sys_client_edit"));
        assertTrue("删除按钮必须匹配后端 sys_client_del 权限", page.contains("permissions.sys_client_del"));
        assertFalse("旧的生成器权限名不会在已部署权限模型中生效", page.contains("generator_syssocialdetails_"));
        assertTrue("新增仍需密钥，编辑时不允许旧密钥必填", page.contains("if (!row.appSecret)"));
        assertFalse("密钥字段不能在编辑表单里沿用必填规则", option.contains("message: '请输入appSecret'"));
        assertTrue("编辑必须改为显式轮换入口", page.contains("rotateSecret"));
    }

    @Test
    public void normalEditPreservesStoredSecretAndOnlyExplicitRouteChangesIt() {
        SysSocialDetailsService service = mock(SysSocialDetailsService.class);
        SysSocialDetails existing = new SysSocialDetails();
        existing.setId(7);
        existing.setAppSecret("stored-secret");
        when(service.getById(7)).thenReturn(existing);
        when(service.updateById(existing)).thenReturn(Boolean.TRUE);
        SocialDetailsController controller = new SocialDetailsController(service);

        SocialDetailsUpdateReqDTO update = new SocialDetailsUpdateReqDTO();
        update.setId(7);
        update.setType("wechat");
        update.setAppId("new-app-id");
        update.setRedirectUrl("https://example.invalid/callback");
        controller.updateById(update);
        assertEquals("普通编辑不得改写存量密钥", "stored-secret", existing.getAppSecret());

        SocialDetailsSecretRotateReqDTO rotate = new SocialDetailsSecretRotateReqDTO();
        rotate.setAppSecret("rotated-secret");
        controller.rotateSecret(7, rotate);
        ArgumentCaptor<SysSocialDetails> saved = ArgumentCaptor.forClass(SysSocialDetails.class);
        verify(service, org.mockito.Mockito.times(2)).updateById(saved.capture());
        assertEquals("仅显式轮换操作可以改变密钥", "rotated-secret", saved.getAllValues().get(1).getAppSecret());
    }

    private String read(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
