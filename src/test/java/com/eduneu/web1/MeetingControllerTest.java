package com.eduneu.web1;

import com.eduneu.web1.controller.MeetingController;
import com.eduneu.web1.entity.Meeting;
import com.eduneu.web1.service.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MeetingControllerTest {

    @Mock
    private MeetingService meetingService;

    @InjectMocks
    private MeetingController meetingController;

    private Meeting testMeeting;

    @BeforeEach
    void setUp() {
        testMeeting = new Meeting();
        testMeeting.setId(1L);
        testMeeting.setName("Test Meeting");
        testMeeting.setOrganizer("Organizer");
    }

    // ===== delete方法测试 =====
    @Test
    void delete_ShouldReturnSuccessWhenValidId() {
        // 准备测试数据
        Map<String, Long> payload = new HashMap<>();
        payload.put("id", 1L);

        // 模拟服务层
        doNothing().when(meetingService).deleteMeeting(1L);

        // 调用控制器方法
        ResponseEntity<String> response = meetingController.delete(payload);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody());
        verify(meetingService, times(1)).deleteMeeting(1L);
    }

    @Test
    void delete_ShouldReturnBadRequestWhenIdIsNull() {
        // 准备测试数据
        Map<String, Long> payload = new HashMap<>();
        payload.put("id", null);

        // 调用控制器方法
        ResponseEntity<String> response = meetingController.delete(payload);

        // 验证结果（精确匹配控制器返回的消息，包括标点符号）
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("ID cannot be null.", response.getBody()); // 注意结尾有句点
        verify(meetingService, never()).deleteMeeting(any());
    }

    @Test
    void delete_ShouldReturnBadRequestWhenPayloadEmpty() {
        // 准备测试数据
        Map<String, Long> payload = Collections.emptyMap();

        // 调用控制器方法
        ResponseEntity<String> response = meetingController.delete(payload);

        // 验证结果（精确匹配控制器返回的消息）
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("ID cannot be null.", response.getBody()); // 统一使用相同的错误消息
        verify(meetingService, never()).deleteMeeting(any());
    }

    @Test
    void delete_ShouldReturnNotFoundWhenMeetingNotExist() {
        // 准备测试数据
        Map<String, Long> payload = new HashMap<>();
        payload.put("id", 99L);

        // 模拟服务层抛出异常
        doThrow(new RuntimeException("Meeting not found"))
                .when(meetingService).deleteMeeting(99L);

        // 调用控制器方法
        ResponseEntity<String> response = meetingController.delete(payload);

        // 验证结果
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Meeting not found", response.getBody());
    }

    // ===== 其他方法测试 =====
    @Test
    void listAll_ShouldReturnAllMeetings() {
        // 模拟服务层
        when(meetingService.getAllMeetings()).thenReturn(Arrays.asList(testMeeting));

        // 调用控制器方法
        ResponseEntity<List<Meeting>> response = meetingController.listAll();

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testMeeting, response.getBody().get(0));
    }

    @Test
    void add_ShouldReturnCreatedMeeting() {
        // 模拟服务层
        when(meetingService.createMeeting(any(Meeting.class))).thenReturn(testMeeting);

        // 调用控制器方法
        ResponseEntity<?> response = meetingController.add(testMeeting);

        // 验证结果
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testMeeting, response.getBody());
    }

    @Test
    void update_ShouldReturnUpdatedMeeting() {
        // 模拟服务层
        when(meetingService.updateMeeting(eq(1L), any(Meeting.class))).thenReturn(testMeeting);

        // 设置会议ID
        testMeeting.setId(1L);

        // 调用控制器方法
        ResponseEntity<?> response = meetingController.update(testMeeting);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testMeeting, response.getBody());
    }
}