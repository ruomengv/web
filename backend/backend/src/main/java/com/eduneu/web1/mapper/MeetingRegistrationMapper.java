package com.eduneu.web1.mapper;

import com.eduneu.web1.entity.MeetingRegistration;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MeetingRegistrationMapper {
    @Insert("INSERT INTO meeting_registration " +
            "(user_id, meeting_id, unit, name, gender, phone, email) " +
            "VALUES (#{userId}, #{meetingId}, #{unit}, #{name}, #{gender}, #{phone}, #{email})")
    int saveRegistration(MeetingRegistration registration);
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "meetingId", column = "meeting_id"),
            @Result(property = "unit", column = "unit"),
            @Result(property = "name", column = "name"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "email", column = "email")
    })
    @Select("SELECT * FROM meeting_registration WHERE meeting_id = #{meetingId}")
    List<MeetingRegistration> getRegistrationsByMeetingId(Long meetingId);
}
