package com.eduneu.web1.controller;

import com.eduneu.web1.entity.Meeting;
import com.eduneu.web1.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("api/meetings")
public class MeetingController {

    @Autowired
    private MeetingService meetingService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @GetMapping("/listAll")
    public ResponseEntity<List<Meeting>> listAll() {
        List<Meeting> data = meetingService.getAllMeetings();
        return ResponseEntity.ok(data);
    }

    @PostMapping("/addMeeting")
    public ResponseEntity<Meeting> add(@RequestBody Map<String, String> credentials) {
        try {
            Meeting meeting = new Meeting();
            if (credentials.get("id") != null) {
                meeting.setId(Long.parseLong(credentials.get("id")));
            }
            meeting.setName(credentials.get("name"));
            meeting.setOrganizer(credentials.get("organizer"));

            // Parse startTime and endTime to OffsetDateTime, then convert to LocalDateTime
            OffsetDateTime startTime = OffsetDateTime.parse(credentials.get("startTime"), formatter);
            OffsetDateTime endTime = OffsetDateTime.parse(credentials.get("endTime"), formatter);
            meeting.setStartTime(startTime.toLocalDateTime());
            meeting.setEndTime(endTime.toLocalDateTime());

            meeting.setContent(credentials.get("content"));
            meeting.setStatus(credentials.get("status"));

            Meeting meetingAdd = meetingService.createMeeting(meeting);
            return new ResponseEntity<>(meetingAdd, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/deleteMeeting")
    public ResponseEntity<String> delete(@RequestBody Map<String, String> credentials) {
        try {
            meetingService.deleteMeeting(Long.valueOf(credentials.get("id")));
            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("failure", HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/updateMeeting")
    public ResponseEntity<Meeting> update(@RequestBody Map<String, String> credentials) {
        try {
            // Validate and parse id
            String idStr = credentials.get("id");
            if (idStr == null || idStr.isEmpty()) {
                throw new IllegalArgumentException("ID cannot be null or empty");
            }
            Long id = Long.parseLong(idStr);

            // Validate and parse other fields
            String name = credentials.get("name");
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name cannot be null or empty");
            }

            String organizer = credentials.get("organizer");
            if (organizer == null || organizer.isEmpty()) {
                throw new IllegalArgumentException("Organizer cannot be null or empty");
            }

            String startTimeStr = credentials.get("startTime");
            if (startTimeStr == null || startTimeStr.isEmpty()) {
                throw new IllegalArgumentException("Start time cannot be null or empty");
            }
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            String endTimeStr = credentials.get("endTime");
            if (endTimeStr == null || endTimeStr.isEmpty()) {
                throw new IllegalArgumentException("End time cannot be null or empty");
            }
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            String content = credentials.get("content");
            if (content == null || content.isEmpty()) {
                throw new IllegalArgumentException("Content cannot be null or empty");
            }

            String status = credentials.get("status");
            if (status == null || status.isEmpty()) {
                throw new IllegalArgumentException("Status cannot be null or empty");
            }

            // Create and update meeting
            Meeting meeting = new Meeting();
            meeting.setId(id);
            meeting.setName(name);
            meeting.setOrganizer(organizer);
            meeting.setStartTime(startTime);
            meeting.setEndTime(endTime);
            meeting.setContent(content);
            meeting.setStatus(status);

            Meeting updatedMeeting = meetingService.updateMeeting(meeting.getId(), meeting);
            return new ResponseEntity<>(updatedMeeting, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/getMeetingById")
    public ResponseEntity<Meeting> getById(@RequestBody Map<String, String> credentials) {
        try {
            Meeting meeting = meetingService.getMeetingById(Long.valueOf(credentials.get("id"))).orElse(null);
            return new ResponseEntity<>(meeting, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/searchMeetings")
    public ResponseEntity<List<Meeting>> searchMeetings(@RequestBody Map<String, String> params) {
        String name = params.get("name");
        String organizer = params.get("organizer");
        OffsetDateTime startTimeStr = OffsetDateTime.parse(params.get("startTime"), formatter);
        LocalDateTime startTime1 = (startTimeStr.toLocalDateTime());
        //LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;

        List<Meeting> meetings = meetingService.searchMeetings(name, organizer, startTime1);
        return new ResponseEntity<>(meetings, HttpStatus.OK);
    }

}