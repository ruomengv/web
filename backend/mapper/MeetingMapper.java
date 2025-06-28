package com.eduneu.web1.mapper;

import com.eduneu.web1.entity.Meeting ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingMapper extends JpaRepository<Meeting, Long> {
    @Query("SELECT m FROM Meeting m WHERE " +
            "(?1 IS NULL OR m.name LIKE %?1%) AND " +
            "(?2 IS NULL OR m.organizer LIKE %?2%) AND " +
            "(?3 IS NULL OR m.startTime >= ?3)")
    List<Meeting> searchMeetings(String name, String organizer, LocalDateTime startTime);
}
