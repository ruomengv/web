package com.eduneu.web1.service;

import com.eduneu.web1.entity.MeetingRegistration;
import com.eduneu.web1.mapper.MeetingRegistrationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeetingRegistrationService {

    @Autowired
    private MeetingRegistrationMapper registrationMapper;

    public int saveRegistration(MeetingRegistration registration) {
        return registrationMapper.saveRegistration(registration);
    }
    public List<MeetingRegistration> getRegistrationsByMeetingId(Long meetingId) {
        // 添加日志
        System.out.println("查询会议ID: " + meetingId);
        return registrationMapper.getRegistrationsByMeetingId(meetingId);
    }
}