package com.eduneu.web1;

import com.eduneu.web1.controller.MeetingRegistrationController;
import com.eduneu.web1.entity.MeetingRegistration;
import com.eduneu.web1.service.MeetingRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class MeetingRegistrationControllerTest {

    @Mock
    private MeetingRegistrationService registrationService;

    @InjectMocks
    private MeetingRegistrationController registrationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ========== saveRegistration 测试 ==========
    @Test
    void saveRegistration_Success() {
        MeetingRegistration registration = createValidRegistration();

        when(registrationService.saveRegistration(any(MeetingRegistration.class))).thenReturn(1);

        ResponseEntity<String> response = registrationController.saveRegistration(registration);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("提交成功", response.getBody());
        verify(registrationService).saveRegistration(any(MeetingRegistration.class));
    }

    @Test
    void saveRegistration_Failure() {
        MeetingRegistration registration = createValidRegistration();

        when(registrationService.saveRegistration(any(MeetingRegistration.class))).thenReturn(0);

        ResponseEntity<String> response = registrationController.saveRegistration(registration);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("提交失败", response.getBody());
    }

    @Test
    void saveRegistration_ValidationFailure() {
        // 用户ID为空
        MeetingRegistration registration1 = new MeetingRegistration();
        registration1.setMeetingId(100L);
        ResponseEntity<String> response1 = registrationController.saveRegistration(registration1);
        assertEquals(HttpStatus.BAD_REQUEST, response1.getStatusCode());
        assertEquals("用户ID和会议ID不能为空", response1.getBody());

        // 会议ID为空
        MeetingRegistration registration2 = new MeetingRegistration();
        registration2.setUserId(1L);
        ResponseEntity<String> response2 = registrationController.saveRegistration(registration2);
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
        assertEquals("用户ID和会议ID不能为空", response2.getBody());

        // 两者都为空
        ResponseEntity<String> response3 = registrationController.saveRegistration(new MeetingRegistration());
        assertEquals(HttpStatus.BAD_REQUEST, response3.getStatusCode());
        assertEquals("用户ID和会议ID不能为空", response3.getBody());

        verify(registrationService, never()).saveRegistration(any(MeetingRegistration.class));
    }

    @Test
    void saveRegistration_DuplicateRegistration() {
        MeetingRegistration registration = createValidRegistration();

        when(registrationService.saveRegistration(any(MeetingRegistration.class)))
                .thenThrow(new RuntimeException("用户已报名"));

        ResponseEntity<String> response = registrationController.saveRegistration(registration);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("用户已报名"));
    }

    @Test
    void saveRegistration_MeetingFull() {
        MeetingRegistration registration = createValidRegistration();

        when(registrationService.saveRegistration(any(MeetingRegistration.class)))
                .thenThrow(new RuntimeException("会议人数已满"));

        ResponseEntity<String> response = registrationController.saveRegistration(registration);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("会议人数已满"));
    }

    @Test
    void saveRegistration_DatabaseError() {
        MeetingRegistration registration = createValidRegistration();

        when(registrationService.saveRegistration(any(MeetingRegistration.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        ResponseEntity<String> response = registrationController.saveRegistration(registration);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("数据库连接失败"));
    }

    @Test
    void saveRegistration_DataAccessException() {
        MeetingRegistration registration = createValidRegistration();

        when(registrationService.saveRegistration(any(MeetingRegistration.class)))
                .thenThrow(new DataAccessException("数据库访问异常") {});

        ResponseEntity<String> response = registrationController.saveRegistration(registration);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("数据库访问异常"));
    }

    // ========== getMeetingRegistrations 测试 ==========
    @Test
    void getMeetingRegistrations_Success() {
        Long meetingId = 100L;
        List<MeetingRegistration> expectedRegistrations = Arrays.asList(
                createRegistration(1L, meetingId),
                createRegistration(2L, meetingId)
        );

        when(registrationService.getRegistrationsByMeetingId(meetingId)).thenReturn(expectedRegistrations);

        ResponseEntity<List<MeetingRegistration>> response =
                registrationController.getMeetingRegistrations(meetingId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals(expectedRegistrations, response.getBody());
    }

    @Test
    void getMeetingRegistrations_EmptyList() {
        Long meetingId = 100L;

        when(registrationService.getRegistrationsByMeetingId(meetingId)).thenReturn(Collections.emptyList());

        ResponseEntity<List<MeetingRegistration>> response =
                registrationController.getMeetingRegistrations(meetingId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getMeetingRegistrations_InvalidMeetingId() {
        // 根据实际控制器行为，null meetingId返回200 OK
        ResponseEntity<List<MeetingRegistration>> response =
                registrationController.getMeetingRegistrations(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(registrationService, never()).getRegistrationsByMeetingId(anyLong());
    }

    @Test
    void getMeetingRegistrations_ServiceException() {
        Long meetingId = 100L;

        when(registrationService.getRegistrationsByMeetingId(meetingId))
                .thenThrow(new RuntimeException("查询失败"));

        // 根据控制器实际行为（不处理异常），预期会抛出异常
        assertThrows(RuntimeException.class,
                () -> registrationController.getMeetingRegistrations(meetingId));

        // 验证服务方法被调用
        verify(registrationService).getRegistrationsByMeetingId(meetingId);
    }

    // ========== 辅助方法 ==========
    private MeetingRegistration createValidRegistration() {
        MeetingRegistration registration = new MeetingRegistration();
        registration.setUserId(1L);
        registration.setMeetingId(100L);
        return registration;
    }

    private MeetingRegistration createRegistration(Long id, Long meetingId) {
        MeetingRegistration registration = new MeetingRegistration();
        registration.setId(id);
        registration.setUserId(id);
        registration.setMeetingId(meetingId);
        return registration;
    }
}