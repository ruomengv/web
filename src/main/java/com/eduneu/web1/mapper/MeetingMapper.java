package com.eduneu.web1.mapper;

import com.eduneu.web1.entity.Meeting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface MeetingMapper {

    List<Meeting> findAll();

    Optional<Meeting> findById(Long id);

    int insert(Meeting meeting);

    int update(Meeting meeting);

    int deleteById(Long id);

    /**
     * 【核心改动点】
     * 1. 增加 @Param("category") String category 参数
     */
    List<Meeting> searchMeetings(@Param("name") String name,
                                 @Param("organizer") String organizer,
                                 @Param("category") String category,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);
}