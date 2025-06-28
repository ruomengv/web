package com.eduneu.web1.controller;

import com.eduneu.web1.entity.Course;
import com.eduneu.web1.entity.User;
import com.eduneu.web1.mapper.CourseMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseMapper courseMapper;

    // 获取当前用户
    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    // 添加课程
    @PostMapping
    public int createCourse(@RequestBody Course course, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }

        // 超级管理员直接发布，企业用户需要审核
        course.setStatus(currentUser.getRole() == 0 ? 1 : 0);
        course.setCreatorId(currentUser.getUid());
        return courseMapper.insertCourse(course);
    }

    // 更新课程
    @PutMapping("/{id}")
    public int updateCourse(@PathVariable Long id, @RequestBody Course course,
                            HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }

        Course existing = courseMapper.findCourseById(id);

        // 权限检查：超级管理员或创建者本人
        if (currentUser.getRole() != 0 && !existing.getCreatorId().equals(currentUser.getUid())) {
            throw new RuntimeException("无权限操作此课程");
        }

        course.setId(id);
        // 如果是企业用户更新，重置为待审核状态
        if (currentUser.getRole() != 0) {
            course.setStatus(0);
        }
        return courseMapper.updateCourse(course);
    }

    // 删除课程
    @DeleteMapping("/{id}")
    public int deleteCourse(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }

        Course existing = courseMapper.findCourseById(id);
        // 权限检查：超级管理员或创建者本人
        if (currentUser.getRole() != 0 && !existing.getCreatorId().equals(currentUser.getUid())) {
            throw new RuntimeException("无权限操作此课程");
        }
        return courseMapper.deleteCourse(id);
    }

    // 课程列表（分页）
    @GetMapping
    public ResponseEntity<?> listCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 参数验证
        if (page < 1 || size < 1) {
            return ResponseEntity.badRequest().body("分页参数必须大于0");
        }

        int offset = (page - 1) * size;
        List<Course> courses = courseMapper.findAllCoursesWithPage(offset, size);
        int total = courseMapper.countCourses();

        Map<String, Object> response = new HashMap<>();
        response.put("list", courses);
        response.put("total", total);

        return ResponseEntity.ok(response);
    }

    // 搜索课程（分页）
    @GetMapping("/search")
    public ResponseEntity<?> searchCourses(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 参数验证
        if (page < 1 || size < 1) {
            return ResponseEntity.badRequest().body("分页参数必须大于0");
        }

        int offset = (page - 1) * size;
        List<Course> courses = courseMapper.searchCoursesWithPage(keyword, offset, size);
        int total = courseMapper.countSearchedCourses(keyword);

        Map<String, Object> response = new HashMap<>();
        response.put("list", courses);
        response.put("total", total);

        return ResponseEntity.ok(response);
    }

    // 课程详情
    @GetMapping("/{id}")
    public Course getCourseDetail(@PathVariable Long id) {
        return courseMapper.findCourseById(id);
    }
}