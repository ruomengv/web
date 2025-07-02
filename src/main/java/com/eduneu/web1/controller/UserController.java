package com.eduneu.web1.controller;

import com.eduneu.web1.dto.PasswordDTO;
import com.eduneu.web1.entity.User;
import com.eduneu.web1.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserMapper userMapper;

    // 获取当前用户
    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpSession session) {
        try {
            User currentUser = getCurrentUser(session);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户未登录");
            }
            User user = userMapper.findUserById(currentUser.getUid());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("获取用户信息失败: " + e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody User user, HttpSession session) {
        try {
            User currentUser = getCurrentUser(session);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户未登录");
            }

            // 只能更新自己的信息
            user.setUid(currentUser.getUid());
            user.setUpdateTime(new Date());
            int result = userMapper.updateUser(user);
            if (result > 0) {
                // 更新session中的用户信息
                session.setAttribute("currentUser", userMapper.findUserById(currentUser.getUid()));
                return ResponseEntity.ok("更新成功");
            }
            return ResponseEntity.badRequest().body("更新失败");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("更新失败: " + e.getMessage());
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordDTO dto, HttpSession session) {
        try {
            User currentUser = getCurrentUser(session);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户未登录");
            }

            // 只能修改自己的密码
            dto.setUid(currentUser.getUid());
            int result = userMapper.updatePassword(dto.getUid(), dto.getOldPassword(), dto.getNewPassword());
            if (result > 0) {
                return ResponseEntity.ok("密码修改成功");
            }
            return ResponseEntity.badRequest().body("密码修改失败，请检查旧密码是否正确");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("密码修改失败: " + e.getMessage());
        }
    }

    // 用户列表（管理员）
    @GetMapping("/list")
    public ResponseEntity<?> listUsers(@RequestParam(required = false) String username,
                                       @RequestParam(required = false) String telephone,
                                       @RequestParam(required = false) Integer status,
                                       HttpSession session) {
        try {
            // 检查管理员权限
            User currentUser = getCurrentUser(session);
            if (currentUser == null || currentUser.getRole() != 0) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无管理员权限");
            }

            List<User> userList = userMapper.findUsersByCondition(username, telephone, status);
            return ResponseEntity.ok(userList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("获取用户列表失败: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user, HttpSession session) {
        try {
            // 检查管理员权限
            User currentUser = getCurrentUser(session);
            if (currentUser == null || currentUser.getRole() != 0) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无管理员权限");
            }

            user.setCreateTime(new Date());
            int result = userMapper.insertUser(user);
            if (result > 0) {
                return ResponseEntity.ok("用户创建成功");
            }
            return ResponseEntity.badRequest().body("用户创建失败");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("用户创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/admin/{uid}")
    public ResponseEntity<?> adminUpdateUser(@PathVariable Long uid, @RequestBody User user, HttpSession session) {
        try {
            // 检查管理员权限
            User currentUser = getCurrentUser(session);
            if (currentUser == null || currentUser.getRole() != 0) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无管理员权限");
            }

            user.setUid(uid);
            user.setUpdateTime(new Date());
            int result = userMapper.updateUser(user);
            if (result > 0) {
                return ResponseEntity.ok("用户更新成功");
            }
            return ResponseEntity.badRequest().body("用户更新失败");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("用户更新失败: " + e.getMessage());
        }
    }

    // 用户状态更新接口
    @PutMapping("/admin/status/{uid}")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long uid,
            @RequestBody Map<String, Integer> requestBody,
            HttpSession session) {
        try {
            // 检查管理员权限
            User currentUser = getCurrentUser(session);
            if (currentUser == null || currentUser.getRole() != 0) {
                logger.warn("非管理员尝试更新用户状态: {}", currentUser);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无管理员权限");
            }

            // 验证状态值
            if (!requestBody.containsKey("status")) {
                return ResponseEntity.badRequest().body("缺少状态参数");
            }

            Integer status = requestBody.get("status");
            if (status == null || (status != 0 && status != 1)) {
                return ResponseEntity.badRequest().body("无效的状态值");
            }

            // 调用Mapper方法 - 传递三个单独参数
            int result = userMapper.updateUserStatus(uid, status, new Date());

            if (result > 0) {
                logger.info("用户状态更新成功: ID={}, 新状态={}", uid, status);
                return ResponseEntity.ok("状态更新成功");
            }
            return ResponseEntity.badRequest().body("状态更新失败");
        } catch (Exception e) {
            logger.error("用户状态更新异常", e);
            return ResponseEntity.internalServerError().body("状态更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/admin/{uid}")
    public ResponseEntity<?> deleteUser(@PathVariable Long uid, HttpSession session) {
        try {
            // 检查管理员权限
            User currentUser = getCurrentUser(session);
            if (currentUser == null || currentUser.getRole() != 0) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无管理员权限");
            }

            int result = userMapper.deleteUser(uid);
            if (result > 0) {
                return ResponseEntity.ok("用户删除成功");
            }
            return ResponseEntity.badRequest().body("用户删除失败");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("用户删除失败: " + e.getMessage());
        }
    }
}