package com.eduneu.web1.controller;

import com.eduneu.web1.entity.MeetingRegistration;
import com.eduneu.web1.service.MeetingRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meeting/registration")
public class MeetingRegistrationController {

    @Autowired
    private MeetingRegistrationService registrationService;

    @PostMapping
    public ResponseEntity<String> saveRegistration(@RequestBody MeetingRegistration registration) {
        try {
            // 校验参数
            if (registration.getUserId() == null || registration.getMeetingId() == null) {
                return ResponseEntity.badRequest().body("用户ID和会议ID不能为空");
            }

            int result = registrationService.saveRegistration(registration);
            return result > 0
                    ? ResponseEntity.ok("提交成功")
                    : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("提交失败");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("系统错误: " + e.getMessage());
        }
    }
    @GetMapping("/{meetingId}/registrations")
    public ResponseEntity<List<MeetingRegistration>> getMeetingRegistrations(@PathVariable Long meetingId) {
        System.out.println("收到请求: 获取会议ID=" + meetingId + "的注册列表");
        List<MeetingRegistration> registrations = registrationService.getRegistrationsByMeetingId(meetingId);
        return ResponseEntity.ok(registrations);
    }
}