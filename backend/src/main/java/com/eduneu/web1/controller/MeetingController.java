package com.eduneu.web1.controller;

import com.eduneu.web1.entity.Meeting;
import com.eduneu.web1.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.eduneu.web1.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/meetings")
public class MeetingController {

    @Autowired
    private MeetingService meetingService;

    // 用于解析带时区偏移的输入格式
    private final DateTimeFormatter offsetFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @GetMapping("/listAll")
    public ResponseEntity<List<Meeting>> listAll() {
        List<Meeting> data = meetingService.getAllMeetings();
        return ResponseEntity.ok(data);
    }

    @PostMapping("/addMeeting")
    public ResponseEntity<?> add(@RequestBody Meeting meeting) {
        try {
            Meeting createdMeeting = meetingService.createMeeting(meeting);
            return new ResponseEntity<>(createdMeeting, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/deleteMeeting")
    public ResponseEntity<String> delete(@RequestBody Map<String, Long> payload) {
        try {
            Long id = payload.get("id");
            if (id == null) {
                return new ResponseEntity<>("ID cannot be null.", HttpStatus.BAD_REQUEST);
            }
            meetingService.deleteMeeting(id);
            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/updateMeeting")
    public ResponseEntity<?> update(@RequestBody Meeting meeting) {
        try {
            if (meeting.getId() == null) {
                return new ResponseEntity<>("ID is required for update.", HttpStatus.BAD_REQUEST);
            }
            Meeting updatedMeeting = meetingService.updateMeeting(meeting.getId(), meeting);
            return new ResponseEntity<>(updatedMeeting, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/getMeetingById")
    public ResponseEntity<?> getById(@RequestBody Map<String, Long> payload) {
        Long id = payload.get("id");
        if (id == null) {
            return new ResponseEntity<>("ID cannot be null.", HttpStatus.BAD_REQUEST);
        }
        return meetingService.getMeetingById(id)
                .<ResponseEntity<?>>map(meeting -> new ResponseEntity<>(meeting, HttpStatus.OK))
                .orElse(new ResponseEntity<>("Meeting not found.", HttpStatus.NOT_FOUND));
    }

    @PostMapping("/searchMeetings")
    public ResponseEntity<?> searchMeetings(@RequestBody Map<String, String> params) {
        try {
            String name = params.get("name");
            String organizer = params.get("organizer");
            String startDateStr = params.get("searchStartDate");
            String endDateStr = params.get("searchEndDate");

            LocalDateTime searchStartDateTime = parseToLocalDateTime(startDateStr, true);
            LocalDateTime searchEndDateTime = parseToLocalDateTime(endDateStr, false);

            if (searchStartDateTime != null && searchEndDateTime == null && startDateStr.length() <= 10) {
                searchEndDateTime = searchStartDateTime.with(LocalTime.MAX);
            }

            List<Meeting> meetings = meetingService.searchMeetings(name, organizer, searchStartDateTime, searchEndDateTime);
            return new ResponseEntity<>(meetings, HttpStatus.OK);

        } catch (DateTimeParseException e) {
            return new ResponseEntity<>("Invalid date format. Use yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("An internal server error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * 审批会议。
     * @param payload 包含会议ID: {"id": 123}
     * @param session Spring会自动注入当前的HttpSession
     * @return 更新后的会议
     */
    @PostMapping("/approve")
    public ResponseEntity<?> approveMeeting(@RequestBody Map<String, Long> payload, HttpSession session) {
        try {
            // 在方法开头调用私有的权限检查方法
            checkAdmin(session);

            // --- 权限检查通过后，才执行业务逻辑 ---
            Long id = payload.get("id");
            if (id == null) {
                return ResponseEntity.badRequest().body("缺少会议ID");
            }

            Meeting approvedMeeting = meetingService.approveMeeting(id);
            return ResponseEntity.ok(approvedMeeting);

        } catch (ResponseStatusException e) {
            // 捕获权限异常，返回相应的状态码和信息
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (RuntimeException e) {
            // 捕获其他业务异常
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    //用于解析日期字符串
    private LocalDateTime parseToLocalDateTime(String dateTimeStr, boolean isStart) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            LocalDate date = LocalDate.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return isStart ? date.atStartOfDay() : date.atTime(LocalTime.MAX);
        }
    }

    private void checkAdmin(HttpSession session) {
        Object userObj = session.getAttribute("currentUser");
        if (userObj == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户未登录，无权访问");
        }
        if (userObj instanceof User user) {
            if (user.getRole() != 0) { // 假设 role 0 是管理员
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无管理员权限");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "会话数据异常");
        }
    }

}