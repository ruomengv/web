package com.eduneu.web1.controller;

import com.eduneu.web1.dto.AuditDTO;
import com.eduneu.web1.entity.Course;
import com.eduneu.web1.entity.User;
import com.eduneu.web1.mapper.CourseMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/courses")
public class AdminCourseController {

    @Autowired
    private CourseMapper courseMapper;

    // 获取当前用户
    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    // 检查管理员权限
    private void checkAdmin(HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || currentUser.getRole() != 0) {
            throw new RuntimeException("无管理员权限");
        }
    }

    // 待审核课程列表（分页版本）
    @GetMapping("/audits")
    public List<Course> pendingAudits(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        checkAdmin(session);
        int offset = (page - 1) * size;
        return courseMapper.findPendingCoursesWithPage(offset, size);
    }

    // 审核通过
    @PutMapping("/{id}/approve")
    public int approveCourse(@PathVariable Long id, HttpSession session) {
        checkAdmin(session);
        Course course = courseMapper.findCourseById(id);
        course.setStatus(1); // 已发布
        return courseMapper.updateCourse(course);
    }

    // 审核驳回
    @PutMapping("/{id}/reject")
    public int rejectCourse(@PathVariable Long id, @RequestBody AuditDTO auditDTO, HttpSession session) {
        checkAdmin(session);
        Course course = courseMapper.findCourseById(id);
        course.setStatus(2); // 驳回
        course.setRejectReason(auditDTO.getReason());
        return courseMapper.updateCourse(course);
    }
}