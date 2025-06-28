package com.eduneu.web1.mapper;

import com.eduneu.web1.entity.News;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NewsMapper {
    // 修复SQL语句，添加create_time字段
    @Insert("INSERT INTO news(title, cover, summary, content, author, status, creator_id, reject_reason, create_time) " +
            "VALUES(#{title}, #{cover}, #{summary}, #{content}, #{author}, #{status}, #{creatorId}, #{rejectReason}, #{createTime})")
    int insertNews(News news);

    // 修复SQL语句，添加update_time字段
    @Update("UPDATE news SET title=#{title}, cover=#{cover}, summary=#{summary}, content=#{content}, " +
            "author=#{author}, status=#{status}, reject_reason=#{rejectReason}, update_time=#{updateTime} WHERE id=#{id}")
    int updateNews(News news);

    @Delete("DELETE FROM news WHERE id=#{id}")
    int deleteNews(Long id);

    @Select("SELECT * FROM news WHERE id=#{id}")
    News findNewsById(Long id);

    // 重命名方法避免冲突
    @Select("SELECT * FROM news WHERE title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR author LIKE CONCAT('%', #{keyword}, '%') " +
            "OR summary LIKE CONCAT('%', #{keyword}, '%') " +
            "LIMIT #{size} OFFSET #{offset}")
    List<News> searchNewsWithPage(
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("size") int size);

    @Select("SELECT COUNT(*) FROM news WHERE title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR author LIKE CONCAT('%', #{keyword}, '%') " +
            "OR summary LIKE CONCAT('%', #{keyword}, '%')")
    int countSearchedNews(@Param("keyword") String keyword);

    @Select("SELECT * FROM news WHERE creator_id=#{creatorId} " +
            "LIMIT #{size} OFFSET #{offset}")
    List<News> findNewsByCreator(
            @Param("creatorId") Long creatorId,
            @Param("offset") int offset,
            @Param("size") int size);

    @Select("SELECT COUNT(*) FROM news WHERE creator_id=#{creatorId}")
    int countNewsByCreator(@Param("creatorId") Long creatorId);

    @Select("SELECT * FROM news WHERE status=0")
    List<News> findPendingNews();

    // 新增的分页查询方法
    @Select("SELECT * FROM news LIMIT #{size} OFFSET #{offset}")
    List<News> findAllNews(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM news")
    int countNews();

    // 按作者查询
    @Select("SELECT * FROM news WHERE author = #{author} " +
            "LIMIT #{size} OFFSET #{offset}")
    List<News> findNewsByAuthor(
            @Param("author") String author,
            @Param("offset") int offset,
            @Param("size") int size);

    @Select("SELECT COUNT(*) FROM news WHERE author = #{author}")
    int countNewsByAuthor(@Param("author") String author);
}