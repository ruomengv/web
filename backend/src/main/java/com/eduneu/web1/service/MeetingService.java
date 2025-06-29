package com.eduneu.web1.service;

import com.eduneu.web1.entity.Meeting;
import com.eduneu.web1.mapper.MeetingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MeetingService {

    @Autowired
    private MeetingMapper meetingMapper;

    public List<Meeting> getAllMeetings() {
        return meetingMapper.findAll();
    }

    public Optional<Meeting> getMeetingById(Long id) {
        return meetingMapper.findById(id);
    }

    public Meeting createMeeting(Meeting meeting) {
        validateMeeting(meeting);
        meetingMapper.insert(meeting);
        return meeting;
    }

    public Meeting updateMeeting(Long id, Meeting meetingDetails) {
        validateMeeting(meetingDetails);
        // 确认会议存在
        meetingMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found with id " + id));
        meetingDetails.setId(id);
        meetingMapper.update(meetingDetails);
        return meetingDetails;
    }

    public void deleteMeeting(Long id) {
        meetingMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found with id " + id));
        meetingMapper.deleteById(id);
    }

    private void validateMeeting(Meeting meeting) {
        if (meeting.getName() == null || meeting.getName().isEmpty()) {
            throw new IllegalArgumentException("会议名称不能为空");
        }
        if (meeting.getOrganizer() == null || meeting.getOrganizer().isEmpty()) {
            throw new IllegalArgumentException("组织者不能为空");
        }
        if (meeting.getStartTime() == null) {
            throw new IllegalArgumentException("开始时间不能为空");
        }
        if (meeting.getEndTime() == null) {
            throw new IllegalArgumentException("结束时间不能为空");
        }
        if (meeting.getStartTime().isAfter(meeting.getEndTime())) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
        if (meeting.getStatus() == null || meeting.getStatus().isEmpty()) {
            throw new IllegalArgumentException("状态不能为空");
        }
    }

    public List<Meeting> searchMeetings(String name, String organizer, LocalDateTime startDate, LocalDateTime endDate) {
        return meetingMapper.searchMeetings(name, organizer, startDate, endDate);
    }
}