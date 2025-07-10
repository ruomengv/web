package com.eduneu.web1.service;

import com.eduneu.web1.entity.Meeting;
import com.eduneu.web1.mapper.MeetingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MeetingServiceTest {

    @Mock
    private MeetingMapper meetingMapper;

    @InjectMocks
    private MeetingService meetingService;

    private Meeting validMeeting;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validMeeting = createValidMeeting();
    }

    // ============== 辅助方法 ==============

    private Meeting createValidMeeting() {
        Meeting meeting = new Meeting();
        meeting.setId(1L);
        meeting.setName("有效会议名称");
        meeting.setOrganizer("组织者");
        meeting.setStartTime(LocalDateTime.now().plusHours(1));
        meeting.setEndTime(LocalDateTime.now().plusHours(2));
        meeting.setStatus("planned");
        meeting.setCategory("技术会议");
        return meeting;
    }

    // ============== getAllMeetings 方法测试 ==============

    @Test
    void testGetAllMeetings_WithMeetings() {
        when(meetingMapper.findAll()).thenReturn(Arrays.asList(validMeeting));
        List<Meeting> result = meetingService.getAllMeetings();
        assertEquals(1, result.size());
        verify(meetingMapper, times(1)).findAll();
    }

    @Test
    void testGetAllMeetings_Empty() {
        when(meetingMapper.findAll()).thenReturn(Collections.emptyList());
        List<Meeting> result = meetingService.getAllMeetings();
        assertTrue(result.isEmpty());
        verify(meetingMapper, times(1)).findAll();
    }

    // ============== getMeetingById 方法测试 ==============

    @Test
    void testGetMeetingById_ExistingId() {
        when(meetingMapper.findById(1L)).thenReturn(Optional.of(validMeeting));
        Optional<Meeting> result = meetingService.getMeetingById(1L);
        assertTrue(result.isPresent());
        assertEquals(validMeeting, result.get());
        verify(meetingMapper, times(1)).findById(1L);
    }

    @Test
    void testGetMeetingById_NonExistingId() {
        when(meetingMapper.findById(2L)).thenReturn(Optional.empty());
        Optional<Meeting> result = meetingService.getMeetingById(2L);
        assertFalse(result.isPresent());
        verify(meetingMapper, times(1)).findById(2L);
    }

    @Test
    void testGetMeetingById_NullId() {
        Optional<Meeting> result = meetingService.getMeetingById(null);
        assertFalse(result.isPresent());
        verify(meetingMapper, times(1)).findById(null);
    }

    // ============== createMeeting 方法测试 ==============

    @Test
    void testCreateMeeting_ValidInput() {
        when(meetingMapper.insert(validMeeting)).thenReturn(1);
        Meeting result = meetingService.createMeeting(validMeeting);
        assertEquals(validMeeting, result);
        verify(meetingMapper, times(1)).insert(validMeeting);
    }

    @Test
    void testCreateMeeting_NullMeeting() {
        assertThrows(NullPointerException.class, () -> meetingService.createMeeting(null));
        verify(meetingMapper, never()).insert(any());
    }

    @ParameterizedTest
    @MethodSource("provideEmptyStrings")
    void testCreateMeeting_EmptyName(String name) {
        validMeeting.setName(name);
        assertThrows(IllegalArgumentException.class, () -> meetingService.createMeeting(validMeeting));
        verify(meetingMapper, never()).insert(any());
    }

    @ParameterizedTest
    @MethodSource("provideEmptyStrings")
    void testCreateMeeting_EmptyOrganizer(String organizer) {
        validMeeting.setOrganizer(organizer);
        assertThrows(IllegalArgumentException.class, () -> meetingService.createMeeting(validMeeting));
        verify(meetingMapper, never()).insert(any());
    }

    @Test
    void testCreateMeeting_MissingStartTime() {
        validMeeting.setStartTime(null);
        assertThrows(IllegalArgumentException.class, () -> meetingService.createMeeting(validMeeting));
        verify(meetingMapper, never()).insert(any());
    }

    @Test
    void testCreateMeeting_MissingEndTime() {
        validMeeting.setEndTime(null);
        assertThrows(IllegalArgumentException.class, () -> meetingService.createMeeting(validMeeting));
        verify(meetingMapper, never()).insert(any());
    }

    @Test
    void testCreateMeeting_StartTimeAfterEndTime() {
        validMeeting.setStartTime(LocalDateTime.now().plusHours(2));
        validMeeting.setEndTime(LocalDateTime.now().plusHours(1));
        assertThrows(IllegalArgumentException.class, () -> meetingService.createMeeting(validMeeting));
        verify(meetingMapper, never()).insert(any());
    }

    @ParameterizedTest
    @MethodSource("provideEmptyStrings")
    void testCreateMeeting_EmptyStatus(String status) {
        validMeeting.setStatus(status);
        assertThrows(IllegalArgumentException.class, () -> meetingService.createMeeting(validMeeting));
        verify(meetingMapper, never()).insert(any());
    }

    // ============== updateMeeting 方法测试 ==============

    @Test
    void testUpdateMeeting_ValidInput() {
        when(meetingMapper.findById(1L)).thenReturn(Optional.of(validMeeting));
        when(meetingMapper.update(validMeeting)).thenReturn(1);

        Meeting updatedMeeting = createValidMeeting();
        updatedMeeting.setName("更新后的会议名称");

        Meeting result = meetingService.updateMeeting(1L, updatedMeeting);
        assertEquals("更新后的会议名称", result.getName());
        verify(meetingMapper, times(1)).findById(1L);
        verify(meetingMapper, times(1)).update(updatedMeeting);
    }

    @Test
    void testUpdateMeeting_NonExistingId() {
        when(meetingMapper.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> meetingService.updateMeeting(2L, validMeeting));
        verify(meetingMapper, times(1)).findById(2L);
        verify(meetingMapper, never()).update(any());
    }

    @Test
    void testUpdateMeeting_InvalidData() {
        validMeeting.setName(null);
        assertThrows(IllegalArgumentException.class, () -> meetingService.updateMeeting(1L, validMeeting));
        verify(meetingMapper, never()).findById(any());
        verify(meetingMapper, never()).update(any());
    }

    // ============== deleteMeeting 方法测试 ==============

    @Test
    void testDeleteMeeting_ExistingId() {
        when(meetingMapper.findById(1L)).thenReturn(Optional.of(validMeeting));
        assertDoesNotThrow(() -> meetingService.deleteMeeting(1L));
        verify(meetingMapper, times(1)).findById(1L);
        verify(meetingMapper, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteMeeting_NonExistingId() {
        when(meetingMapper.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> meetingService.deleteMeeting(2L));
        verify(meetingMapper, times(1)).findById(2L);
        verify(meetingMapper, never()).deleteById(any());
    }

    // ============== approveMeeting 方法测试 ==============

    @Test
    void testApproveMeeting_Valid() {
        Meeting plannedMeeting = createValidMeeting();
        plannedMeeting.setStatus("planned");

        when(meetingMapper.findById(1L)).thenReturn(Optional.of(plannedMeeting));
        when(meetingMapper.update(plannedMeeting)).thenReturn(1);

        Meeting result = meetingService.approveMeeting(1L);
        assertEquals("scheduled", result.getStatus());
        verify(meetingMapper, times(1)).findById(1L);
        verify(meetingMapper, times(1)).update(plannedMeeting);
    }

    @Test
    void testApproveMeeting_NonExistingId() {
        when(meetingMapper.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> meetingService.approveMeeting(2L));
        verify(meetingMapper, times(1)).findById(2L);
        verify(meetingMapper, never()).update(any());
    }

    @Test
    void testApproveMeeting_AlreadyApproved() {
        Meeting scheduledMeeting = createValidMeeting();
        scheduledMeeting.setStatus("scheduled");

        when(meetingMapper.findById(1L)).thenReturn(Optional.of(scheduledMeeting));
        assertThrows(IllegalStateException.class, () -> meetingService.approveMeeting(1L));
        verify(meetingMapper, times(1)).findById(1L);
        verify(meetingMapper, never()).update(any());
    }

    @Test
    void testApproveMeeting_WrongStatus() {
        Meeting cancelledMeeting = createValidMeeting();
        cancelledMeeting.setStatus("cancelled");

        when(meetingMapper.findById(1L)).thenReturn(Optional.of(cancelledMeeting));
        assertThrows(IllegalStateException.class, () -> meetingService.approveMeeting(1L));
        verify(meetingMapper, times(1)).findById(1L);
        verify(meetingMapper, never()).update(any());
    }

    // ============== searchMeetings 方法测试 ==============

    @Test
    void testSearchMeetings_WithCriteria() {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        when(meetingMapper.searchMeetings("技术", "张三", "技术会议", startDate, endDate))
                .thenReturn(Arrays.asList(validMeeting));

        List<Meeting> result = meetingService.searchMeetings("技术", "张三", "技术会议", startDate, endDate);
        assertEquals(1, result.size());
        verify(meetingMapper, times(1)).searchMeetings("技术", "张三", "技术会议", startDate, endDate);
    }

    @Test
    void testSearchMeetings_EmptyCriteria() {
        when(meetingMapper.searchMeetings(null, null, null, null, null))
                .thenReturn(Collections.emptyList());

        List<Meeting> result = meetingService.searchMeetings(null, null, null, null, null);
        assertTrue(result.isEmpty());
        verify(meetingMapper, times(1)).searchMeetings(null, null, null, null, null);
    }

    // 修改：开始时间等于结束时间的测试（业务允许这种情况）
    @Test
    void testCreateMeeting_StartTimeEqualsEndTime() {
        validMeeting.setStartTime(LocalDateTime.now());
        validMeeting.setEndTime(LocalDateTime.now());

        // 预期不会抛出异常
        assertDoesNotThrow(() -> meetingService.createMeeting(validMeeting));

        // 验证会议是否成功创建
        verify(meetingMapper, times(1)).insert(validMeeting);
    }

    // 提供空字符串和 null 的方法
    private static Stream<Arguments> provideEmptyStrings() {
        return Stream.of(
                Arguments.of((String) null),
                Arguments.of("")
        );
    }
}