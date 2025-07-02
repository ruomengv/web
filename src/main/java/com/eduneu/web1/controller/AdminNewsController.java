package com.eduneu.web1.controller;

import com.eduneu.web1.dto.AuditDTO;
import com.eduneu.web1.entity.News;
import com.eduneu.web1.entity.User;
import com.eduneu.web1.mapper.NewsMapper;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/news")
public class AdminNewsController {
    private static final Logger logger = LoggerFactory.getLogger(AdminNewsController.class);

    @Autowired
    private NewsMapper newsMapper;

    // 获取当前用户
    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    // 检查管理员权限
    private void checkAdmin(HttpSession session) {
        User currentUser = getCurrentUser(session);
        logger.info("Checking admin access for session: {}", session.getId());
        if (currentUser == null) {
            logger.error("User not found in session");
            throw new RuntimeException("用户会话已过期，请重新登录");
        }
        if (currentUser.getRole() != 0) {
            logger.error("User is not admin. Role: {}", currentUser.getRole());
            throw new RuntimeException("权限不足，需要管理员权限");
        }
        logger.info("User {} is admin", currentUser.getUid());
    }

    // 待审核动态列表
    @GetMapping("/audits")
    public ResponseEntity<?> pendingAudits(HttpSession session) {
        try {
            checkAdmin(session);
            return ResponseEntity.ok(newsMapper.findPendingNews());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // 审核通过
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveNews(@PathVariable Long id, HttpSession session) {
        try {
            checkAdmin(session);
            News news = newsMapper.findNewsById(id);
            if (news == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("动态不存在");
            }
            news.setStatus(1); // 已发布
            newsMapper.updateNews(news);
            return ResponseEntity.ok("审核通过");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // 审核驳回
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectNews(@PathVariable Long id, @RequestBody AuditDTO auditDTO, HttpSession session) {
        try {
            checkAdmin(session);
            News news = newsMapper.findNewsById(id);
            if (news == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("动态不存在");
            }
            news.setStatus(2); // 驳回
            news.setRejectReason(auditDTO.getReason());
            newsMapper.updateNews(news);
            return ResponseEntity.ok("已驳回");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}