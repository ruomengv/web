package com.eduneu.web1.service;

import com.eduneu.web1.entity.MeetingRegistration;
import com.eduneu.web1.mapper.MeetingRegistrationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MeetingRegistrationServiceTest {

    @Mock
    private MeetingRegistrationMapper registrationMapper;

    @InjectMocks
    private MeetingRegistrationService meetingRegistrationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ============== saveRegistration 方法测试 ==============

    @Test
    void testSaveRegistration_Success() {
        // 准备测试数据
        MeetingRegistration registration = new MeetingRegistration();
        registration.setId(1L);
        registration.setMeetingId(100L);
        registration.setUserId(200L);

        // 设置mock返回值
        when(registrationMapper.saveRegistration(registration)).thenReturn(1);

        // 执行测试
        int result = meetingRegistrationService.saveRegistration(registration);

        // 验证结果
        assertEquals(1, result);
        verify(registrationMapper, times(1)).saveRegistration(registration);
    }

    @Test
    void testSaveRegistration_Failure() {
        // 准备测试数据
        MeetingRegistration registration = new MeetingRegistration();

        // 设置mock返回值（插入失败）
        when(registrationMapper.saveRegistration(registration)).thenReturn(0);

        // 执行测试
        int result = meetingRegistrationService.saveRegistration(registration);

        // 验证结果
        assertEquals(0, result);
        verify(registrationMapper, times(1)).saveRegistration(registration);
    }

    @Test
    void testSaveRegistration_NullInput() {
        // 执行测试
        int result = meetingRegistrationService.saveRegistration(null);

        // 验证结果
        assertEquals(0, result);
        verify(registrationMapper, times(1)).saveRegistration(null);
    }

    // ============== getRegistrationsByMeetingId 方法测试 ==============

    @Test
    void testGetRegistrationsByMeetingId_NormalCase() {
        // 准备测试数据
        Long meetingId = 100L;
        MeetingRegistration reg1 = new MeetingRegistration();
        reg1.setId(1L);
        reg1.setMeetingId(meetingId);
        MeetingRegistration reg2 = new MeetingRegistration();
        reg2.setId(2L);
        reg2.setMeetingId(meetingId);
        List<MeetingRegistration> expectedList = Arrays.asList(reg1, reg2);

        // 设置mock返回值
        when(registrationMapper.getRegistrationsByMeetingId(meetingId)).thenReturn(expectedList);

        // 执行测试
        List<MeetingRegistration> result = meetingRegistrationService.getRegistrationsByMeetingId(meetingId);

        // 验证结果
        assertEquals(2, result.size());
        assertEquals(expectedList, result);
        verify(registrationMapper, times(1)).getRegistrationsByMeetingId(meetingId);
    }

    @Test
    void testGetRegistrationsByMeetingId_NoRegistrations() {
        // 准备测试数据
        Long meetingId = 200L;

        // 设置mock返回空列表
        when(registrationMapper.getRegistrationsByMeetingId(meetingId)).thenReturn(Collections.emptyList());

        // 执行测试
        List<MeetingRegistration> result = meetingRegistrationService.getRegistrationsByMeetingId(meetingId);

        // 验证结果
        assertEquals(0, result.size());
        verify(registrationMapper, times(1)).getRegistrationsByMeetingId(meetingId);
    }

    @Test
    void testGetRegistrationsByMeetingId_NullMeetingId() {
        // 设置mock返回空列表（当meetingId为null时）
        when(registrationMapper.getRegistrationsByMeetingId(null)).thenReturn(Collections.emptyList());

        // 执行测试
        List<MeetingRegistration> result = meetingRegistrationService.getRegistrationsByMeetingId(null);

        // 验证结果
        assertTrue(result.isEmpty());
        verify(registrationMapper, times(1)).getRegistrationsByMeetingId(null);
    }

    @Test
    void testGetRegistrationsByMeetingId_BoundaryValue_MinLong() {
        // 准备测试数据
        Long meetingId = Long.MIN_VALUE;

        // 设置mock返回空列表（假设数据库中不存在该ID的记录）
        when(registrationMapper.getRegistrationsByMeetingId(meetingId)).thenReturn(Collections.emptyList());

        // 执行测试
        List<MeetingRegistration> result = meetingRegistrationService.getRegistrationsByMeetingId(meetingId);

        // 验证结果
        assertTrue(result.isEmpty());
        verify(registrationMapper, times(1)).getRegistrationsByMeetingId(meetingId);
    }

    @Test
    void testGetRegistrationsByMeetingId_BoundaryValue_MaxLong() {
        // 准备测试数据
        Long meetingId = Long.MAX_VALUE;

        // 设置mock返回空列表（假设数据库中不存在该ID的记录）
        when(registrationMapper.getRegistrationsByMeetingId(meetingId)).thenReturn(Collections.emptyList());

        // 执行测试
        List<MeetingRegistration> result = meetingRegistrationService.getRegistrationsByMeetingId(meetingId);

        // 验证结果
        assertTrue(result.isEmpty());
        verify(registrationMapper, times(1)).getRegistrationsByMeetingId(meetingId);
    }

    @Test
    void testGetRegistrationsByMeetingId_MapperThrowsException() {
        // 准备测试数据
        Long meetingId = 300L;

        // 设置mock抛出异常
        when(registrationMapper.getRegistrationsByMeetingId(meetingId)).thenThrow(new RuntimeException("数据库连接失败"));

        // 执行测试并断言抛出异常
        assertThrows(RuntimeException.class, () -> {
            meetingRegistrationService.getRegistrationsByMeetingId(meetingId);
        });

        // 验证mapper方法被调用一次
        verify(registrationMapper, times(1)).getRegistrationsByMeetingId(meetingId);
    }
}