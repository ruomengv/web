package com.eduneu.web1.service;

import com.eduneu.web1.entity.Meeting ;
import com.eduneu.web1.mapper.MeetingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MeetingService {
    @Autowired
    private MeetingMapper meetingRepository;

    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAll();
    }

    public Optional<Meeting> getMeetingById(Long id) {
        return meetingRepository.findById(id);
    }

    public Meeting createMeeting(Meeting meeting) {
        validateMeeting(meeting);
        return meetingRepository.save(meeting);
    }

    public Meeting updateMeeting(Long id, Meeting meetingDetails) {
        validateMeeting(meetingDetails);
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found with id " + id));
        meeting.setName(meetingDetails.getName());
        meeting.setOrganizer(meetingDetails.getOrganizer());
        meeting.setStartTime(meetingDetails.getStartTime());
        meeting.setEndTime(meetingDetails.getEndTime());
        meeting.setContent(meetingDetails.getContent());
        meeting.setStatus(meetingDetails.getStatus());
        return meetingRepository.save(meeting);
    }

    public void deleteMeeting(Long id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found with id " + id));
        meetingRepository.delete(meeting);
    }

    private void validateMeeting(Meeting meeting) {
        if (meeting.getName() == null || meeting.getName().isEmpty()) {
            throw new RuntimeException("会议名称不能为空");
        }
        if (meeting.getOrganizer() == null || meeting.getOrganizer().isEmpty()) {
            throw new RuntimeException("组织者不能为空");
        }
        if (meeting.getStartTime() == null) {
            throw new RuntimeException("开始时间不能为空");
        }
        if (meeting.getEndTime() == null) {
            throw new RuntimeException("结束时间不能为空");
        }
        if (meeting.getStatus() == null || meeting.getStatus().isEmpty()) {
            throw new RuntimeException("状态不能为空");
        }
    }


        public List<Meeting> searchMeetings(String name, String organizer, LocalDateTime startTime) {
            // 根据条件搜索会议
            return meetingRepository.searchMeetings(name, organizer, startTime);
        }
    }


