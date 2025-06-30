package com.eduneu.web1.mapper;

import com.eduneu.web1.entity.Course;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CourseMapper {
    // 修复点：添加 create_time 字段
    @Insert("INSERT INTO course(title, cover, summary, sort, video_url, author, status, creator_id, reject_reason, create_time) " +
            "VALUES(#{title}, #{cover}, #{summary}, #{sort}, #{videoUrl}, #{author}, #{status}, #{creatorId}, #{rejectReason}, #{createTime})")
    int insertCourse(Course course);

    // 修复点：添加 update_time 字段
    @Update("UPDATE course SET title=#{title}, cover=#{cover}, summary=#{summary}, " +
            "sort=#{sort}, video_url=#{videoUrl}, author=#{author}, status=#{status}, reject_reason=#{rejectReason}, update_time=#{updateTime} " +
            "WHERE id=#{id}")
    int updateCourse(Course course);

    @Delete("DELETE FROM course WHERE id=#{id}")
    int deleteCourse(Long id);

    @Select("SELECT * FROM course WHERE id=#{id}")
    Course findCourseById(Long id);

    // 分页查询方法
    @Select("SELECT * FROM course LIMIT #{size} OFFSET #{offset}")
    List<Course> findAllCoursesWithPage(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM course")
    int countCourses();


    @Select("SELECT * FROM course WHERE title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR author LIKE CONCAT('%', #{keyword}, '%') " +
            "LIMIT #{size} OFFSET #{offset}")
    List<Course> searchCoursesWithPage(
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("size") int size);

    @Select("SELECT COUNT(*) FROM course WHERE title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR author LIKE CONCAT('%', #{keyword}, '%')")
    int countSearchedCourses(@Param("keyword") String keyword);


    @Select("SELECT * FROM course")
    List<Course> findAllCourses();

    @Select("SELECT * FROM course WHERE title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR author LIKE CONCAT('%', #{keyword}, '%')")
    List<Course> searchCourses(String keyword);

    @Select("SELECT * FROM course WHERE status=0")
    List<Course> findPendingCourses();
}