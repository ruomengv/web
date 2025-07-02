package com.eduneu.web1;

import com.eduneu.web1.controller.AdminCourseController;
import com.eduneu.web1.dto.AuditDTO;
import com.eduneu.web1.entity.Course;
import com.eduneu.web1.entity.User;
import com.eduneu.web1.mapper.CourseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminCourseControllerTest {

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private AdminCourseController adminCourseController;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = new MockHttpSession();
    }

    // 正常情况的测试用例
    @Test
    void approveCourse_AdminLoggedIn() {
        User admin = new User();
        admin.setUid(1L);
        admin.setRole(0);
        session.setAttribute("currentUser", admin);

        Course course = new Course();
        course.setId(1L);
        when(courseMapper.findCourseById(1L)).thenReturn(course);
        when(courseMapper.updateCourse(course)).thenReturn(1);

        int result = adminCourseController.approveCourse(1L, session);
        assertEquals(1, result);
    }

    @Test
    void approveCourse_UserNotAdmin() {
        User nonAdmin = new User();
        nonAdmin.setUid(1L);
        nonAdmin.setRole(1);
        session.setAttribute("currentUser", nonAdmin);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminCourseController.approveCourse(1L, session));
        assertEquals("无管理员权限", exception.getMessage());
    }

    @Test
    void rejectCourse_AdminLoggedIn() {
        User admin = new User();
        admin.setUid(1L);
        admin.setRole(0);
        session.setAttribute("currentUser", admin);

        Course course = new Course();
        course.setId(1L);
        when(courseMapper.findCourseById(1L)).thenReturn(course);
        when(courseMapper.updateCourse(course)).thenReturn(1);

        AuditDTO auditDTO = new AuditDTO();
        auditDTO.setReason("Not good");

        int result = adminCourseController.rejectCourse(1L, auditDTO, session);
        assertEquals(1, result);
    }

    @Test
    void rejectCourse_UserNotAdmin() {
        User nonAdmin = new User();
        nonAdmin.setUid(1L);
        nonAdmin.setRole(1);
        session.setAttribute("currentUser", nonAdmin);

        AuditDTO auditDTO = new AuditDTO();
        auditDTO.setReason("Not good");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminCourseController.rejectCourse(1L, auditDTO, session));
        assertEquals("无管理员权限", exception.getMessage());
    }

    //边界条件和异常处理测试
    @Test
    void approveCourse_CourseNotFound() {
        User admin = new User();
        admin.setUid(1L);
        admin.setRole(0);
        session.setAttribute("currentUser", admin);

        when(courseMapper.findCourseById(1L)).thenReturn(null);

        assertThrows(NullPointerException.class,
                () -> adminCourseController.approveCourse(1L, session));
    }

    @Test
    void approveCourse_UserNotLoggedIn() {
        MockHttpSession emptySession = new MockHttpSession();

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminCourseController.approveCourse(1L, emptySession));
        assertEquals("无管理员权限", exception.getMessage());
    }

    @Test
    void rejectCourse_AuditDTONull() {
        User admin = new User();
        admin.setUid(1L);
        admin.setRole(0);
        session.setAttribute("currentUser", admin);

        Course course = new Course();
        course.setId(1L);
        when(courseMapper.findCourseById(1L)).thenReturn(course);

        assertThrows(NullPointerException.class,
                () -> adminCourseController.rejectCourse(1L, null, session));
    }

    @Test
    void rejectCourse_EmptyReason() {
        User admin = new User();
        admin.setUid(1L);
        admin.setRole(0);
        session.setAttribute("currentUser", admin);

        Course course = new Course();
        course.setId(1L);
        when(courseMapper.findCourseById(1L)).thenReturn(course);
        when(courseMapper.updateCourse(course)).thenReturn(1); // 假设空原因也能通过

        AuditDTO auditDTO = new AuditDTO();
        auditDTO.setReason("");

        // 根据实际代码行为调整预期
        int result = adminCourseController.rejectCourse(1L, auditDTO, session);
        assertEquals(1, result);
    }

    @Test
    void rejectCourse_UpdateFailed() {
        User admin = new User();
        admin.setUid(1L);
        admin.setRole(0);
        session.setAttribute("currentUser", admin);

        Course course = new Course();
        course.setId(1L);
        when(courseMapper.findCourseById(1L)).thenReturn(course);
        when(courseMapper.updateCourse(course)).thenReturn(0);

        AuditDTO auditDTO = new AuditDTO();
        auditDTO.setReason("Not good");


        int result = adminCourseController.rejectCourse(1L, auditDTO, session);
        assertEquals(0, result);
    }

    @Test
    void approveCourse_InvalidCourseId() {
        User admin = new User();
        admin.setUid(1L);
        admin.setRole(0);
        session.setAttribute("currentUser", admin);

        when(courseMapper.findCourseById(0L)).thenReturn(null);
        assertThrows(NullPointerException.class,
                () -> adminCourseController.approveCourse(0L, session));

        when(courseMapper.findCourseById(-1L)).thenReturn(null);
        assertThrows(NullPointerException.class,
                () -> adminCourseController.approveCourse(-1L, session));
    }

    @Test
    void rejectCourse_CourseNotFound() {
        User admin = new User();
        admin.setUid(1L);
        admin.setRole(0);
        session.setAttribute("currentUser", admin);

        when(courseMapper.findCourseById(1L)).thenReturn(null);

        AuditDTO auditDTO = new AuditDTO();
        auditDTO.setReason("Not good");

        assertThrows(NullPointerException.class,
                () -> adminCourseController.rejectCourse(1L, auditDTO, session));
    }

    @Test
    void rejectCourse_UserNotLoggedIn() {
        MockHttpSession emptySession = new MockHttpSession();

        AuditDTO auditDTO = new AuditDTO();
        auditDTO.setReason("Not good");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminCourseController.rejectCourse(1L, auditDTO, emptySession));
        assertEquals("无管理员权限", exception.getMessage());
    }

    @Test
    void approveCourse_UpdateFailed() {
        User admin = new User();
        admin.setUid(1L);
        admin.setRole(0);
        session.setAttribute("currentUser", admin);

        Course course = new Course();
        course.setId(1L);
        when(courseMapper.findCourseById(1L)).thenReturn(course);
        when(courseMapper.updateCourse(course)).thenReturn(0);


        int result = adminCourseController.approveCourse(1L, session);
        assertEquals(0, result);
    }
}