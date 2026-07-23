package com.tce.smart.admin.controller;

import com.tce.smart.admin.api.dto.PasswordUpdateReqDTO;
import org.junit.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/** 管理端旧改密路由不能被普通用户当作公开密码找回通道使用。 */
public class UserControllerAccessContractTest {

    @Test
    public void legacyManagementPasswordRouteRequiresUserManagementPermission() throws Exception {
        Method method = UserController.class.getMethod("updatePwd", PasswordUpdateReqDTO.class);
        assertEquals("/password/update", method.getAnnotation(PutMapping.class).value()[0]);
        PreAuthorize permission = method.getAnnotation(PreAuthorize.class);
        assertNotNull("管理端改密必须要求用户管理权限", permission);
        assertEquals("@pms.hasPermission('sys_user_edit')", permission.value());
    }
}
