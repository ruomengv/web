package com.eduneu.web1.controller;

import com.eduneu.web1.dto.PasswordDTO;
import com.eduneu.web1.entity.User;
import com.eduneu.web1.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private HttpSession session;

    @InjectMocks
    private UserController userController;

    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        adminUser = new User();
        adminUser.setUid(1L);
        adminUser.setUsername("admin");
        adminUser.setRole(0); // 管理员

        regularUser = new User();
        regularUser.setUid(2L);
        regularUser.setUsername("user");
        regularUser.setRole(1); // 普通用户
    }

    // ========== getProfile 测试 ==========
    @Test
    void getProfile_Success() {
        when(session.getAttribute("currentUser")).thenReturn(regularUser);
        when(userMapper.findUserById(regularUser.getUid())).thenReturn(regularUser);

        ResponseEntity<?> response = userController.getProfile(session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(regularUser, response.getBody());
    }

    @Test
    void getProfile_UserNotLoggedIn() {
        when(session.getAttribute("currentUser")).thenReturn(null);

        ResponseEntity<?> response = userController.getProfile(session);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("用户未登录", response.getBody());
    }

    @Test
    void getProfile_DatabaseError() {
        when(session.getAttribute("currentUser")).thenReturn(regularUser);
        when(userMapper.findUserById(regularUser.getUid()))
                .thenThrow(new DataAccessException("数据库错误") {});

        ResponseEntity<?> response = userController.getProfile(session);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("数据库错误"));
    }

    // ========== updateProfile 测试 ==========
    @Test
    void updateProfile_Success() {
        User updatedUser = new User();
        updatedUser.setUsername("newUsername");

        when(session.getAttribute("currentUser")).thenReturn(regularUser);
        when(userMapper.updateUser(any(User.class))).thenReturn(1);
        when(userMapper.findUserById(regularUser.getUid())).thenReturn(updatedUser);

        ResponseEntity<?> response = userController.updateProfile(updatedUser, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("更新成功", response.getBody());
        verify(session).setAttribute(eq("currentUser"), any(User.class));
    }

    @Test
    void updateProfile_UpdateFailed() {
        User updatedUser = new User();

        when(session.getAttribute("currentUser")).thenReturn(regularUser);
        when(userMapper.updateUser(any(User.class))).thenReturn(0);

        ResponseEntity<?> response = userController.updateProfile(updatedUser, session);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("更新失败", response.getBody());
    }

    // ========== changePassword 测试 ==========
    @Test
    void changePassword_Success() {
        PasswordDTO dto = new PasswordDTO();
        dto.setOldPassword("old");
        dto.setNewPassword("new");

        when(session.getAttribute("currentUser")).thenReturn(regularUser);
        when(userMapper.updatePassword(eq(regularUser.getUid()), eq("old"), eq("new")))
                .thenReturn(1);

        ResponseEntity<?> response = userController.changePassword(dto, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("密码修改成功", response.getBody());
    }

    @Test
    void changePassword_WrongOldPassword() {
        PasswordDTO dto = new PasswordDTO();
        dto.setOldPassword("wrong");
        dto.setNewPassword("new");

        when(session.getAttribute("currentUser")).thenReturn(regularUser);
        when(userMapper.updatePassword(anyLong(), anyString(), anyString())).thenReturn(0);

        ResponseEntity<?> response = userController.changePassword(dto, session);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("密码修改失败，请检查旧密码是否正确", response.getBody());
    }

    // ========== listUsers 测试 ==========
    @Test
    void listUsers_AdminSuccess() {
        List<User> userList = Arrays.asList(adminUser, regularUser);

        when(session.getAttribute("currentUser")).thenReturn(adminUser);
        when(userMapper.findUsersByCondition(null, null, null)).thenReturn(userList);

        ResponseEntity<?> response = userController.listUsers(null, null, null, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, ((List<?>) response.getBody()).size());
    }

    @Test
    void listUsers_NonAdminAccess() {
        when(session.getAttribute("currentUser")).thenReturn(regularUser);

        ResponseEntity<?> response = userController.listUsers(null, null, null, session);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("无管理员权限", response.getBody());
    }

    @Test
    void listUsers_WithSearchConditions() {
        when(session.getAttribute("currentUser")).thenReturn(adminUser);
        when(userMapper.findUsersByCondition("test", "123", 1))
                .thenReturn(Collections.singletonList(regularUser));

        ResponseEntity<?> response = userController.listUsers("test", "123", 1, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, ((List<?>) response.getBody()).size());
    }

    // ========== createUser 测试 ==========
    @Test
    void createUser_AdminSuccess() {
        User newUser = new User();
        newUser.setUsername("newUser");

        when(session.getAttribute("currentUser")).thenReturn(adminUser);
        when(userMapper.insertUser(any(User.class))).thenReturn(1);

        ResponseEntity<?> response = userController.createUser(newUser, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("用户创建成功", response.getBody());
        assertNotNull(newUser.getCreateTime());
    }

    // ========== adminUpdateUser 测试 ==========
    @Test
    void adminUpdateUser_Success() {
        User updatedUser = new User();
        updatedUser.setUid(2L);
        updatedUser.setUsername("updated");

        when(session.getAttribute("currentUser")).thenReturn(adminUser);
        when(userMapper.updateUser(any(User.class))).thenReturn(1);

        ResponseEntity<?> response = userController.adminUpdateUser(2L, updatedUser, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("用户更新成功", response.getBody());
        assertNotNull(updatedUser.getUpdateTime());
    }

    // ========== updateUserStatus 测试 ==========
    @Test
    void updateUserStatus_Success() {
        Map<String, Integer> request = new HashMap<>();
        request.put("status", 1);

        when(session.getAttribute("currentUser")).thenReturn(adminUser);
        when(userMapper.updateUserStatus(eq(2L), eq(1), any(Date.class))).thenReturn(1);

        ResponseEntity<?> response = userController.updateUserStatus(2L, request, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("状态更新成功", response.getBody());
    }

    @Test
    void updateUserStatus_InvalidStatus() {
        Map<String, Integer> request = new HashMap<>();
        request.put("status", 2); // 无效状态

        when(session.getAttribute("currentUser")).thenReturn(adminUser);

        ResponseEntity<?> response = userController.updateUserStatus(2L, request, session);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("无效的状态值", response.getBody());
    }

    // ========== deleteUser 测试 ==========
    @Test
    void deleteUser_Success() {
        when(session.getAttribute("currentUser")).thenReturn(adminUser);
        when(userMapper.deleteUser(2L)).thenReturn(1);

        ResponseEntity<?> response = userController.deleteUser(2L, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("用户删除成功", response.getBody());
    }

    @Test
    void deleteUser_NotFound() {
        when(session.getAttribute("currentUser")).thenReturn(adminUser);
        when(userMapper.deleteUser(2L)).thenReturn(0);

        ResponseEntity<?> response = userController.deleteUser(2L, session);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("用户删除失败", response.getBody());
    }
}