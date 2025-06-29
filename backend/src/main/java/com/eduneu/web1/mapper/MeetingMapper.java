package com.eduneu.web1.mapper;

import com.eduneu.web1.entity.Meeting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface MeetingMapper {

    /**
     * 查询所有会议
     */
    List<Meeting> findAll();

    /**
     * 根据ID查询会议
     */
    Optional<Meeting> findById(Long id);

    /**
     * 插入一个新的会议
     * @return 返回影响的行数
     */
    int insert(Meeting meeting);

    /**
     * 更新一个已有的会议
     * @return 返回影响的行数
     */
    int update(Meeting meeting);

    /**
     * 根据ID删除会议
     */
    int deleteById(Long id);

    /**
     * 根据动态条件搜索会议
     * @param name       会议名称
     * @param organizer  组织者
     * @param startDate  搜索开始时间
     * @param endDate    搜索结束时间
     * @return 匹配的会议列表
     */
    List<Meeting> searchMeetings(@Param("name") String name,
                                 @Param("organizer") String organizer,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);
}