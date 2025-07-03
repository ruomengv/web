package com.eduneu.web1.service;

import com.eduneu.web1.entity.Meeting;
import com.eduneu.web1.mapper.MeetingMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MeetingService {

    @Autowired
    private MeetingMapper meetingMapper;

    // ... 其他方法 getAllMeetings, getMeetingById, createMeeting, updateMeeting, deleteMeeting 保持不变 ...
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
        // 可选：如果 category 是必填项，可以在这里添加校验
        // if (meeting.getCategory() == null || meeting.getCategory().isEmpty()) {
        //     throw new IllegalArgumentException("会议类别不能为空");
        // }
    }

    @Transactional
    public Meeting approveMeeting(Long meetingId) {
        Meeting meeting = meetingMapper.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("找不到ID为 " + meetingId + " 的会议"));

        if (!"planned".equalsIgnoreCase(meeting.getStatus())) {
            throw new IllegalStateException("只有 'planned' 状态的会议才能被审批");
        }

        meeting.setStatus("scheduled");
        meetingMapper.update(meeting);
        return meeting;
    }


    public List<Meeting> searchMeetings(String name, String organizer, String category, LocalDateTime startDate, LocalDateTime endDate) {
        return meetingMapper.searchMeetings(name, organizer, category, startDate, endDate);
    }
}