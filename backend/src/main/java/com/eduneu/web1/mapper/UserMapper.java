package com.eduneu.web1.mapper;

import com.eduneu.web1.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    // 登录
    @Select("SELECT * FROM user WHERE username = #{username} AND password = #{password}")
    User login(User user);

    // 获取所有用户
    @Select("SELECT * FROM user")
    List<User> findAllUsers();

    // 条件查询用户
    @Select({
            "<script>",
            "SELECT * FROM user",
            "<where>",
            "   <if test='username != null'> AND username LIKE CONCAT('%', #{username}, '%') </if>",
            "   <if test='telephone != null'> OR telephone LIKE CONCAT('%', #{telephone}, '%') </if>",
            "   <if test='status != null'> OR status = #{status} </if>",
            "</where>",
            "</script>"
    })
    List<User> findUsersByCondition(@Param("username") String username,
                                    @Param("telephone") String telephone,
                                    @Param("status") Integer status);

    // 删除用户
    @Delete("DELETE FROM user WHERE uid = #{uid}")
    int deleteUser(@Param("uid") Long uid);

    // 添加用户
    @Insert("INSERT INTO user(username, password, nickname, telephone, email, gender, status, role, company_id, create_time) " +
            "VALUES(#{username}, #{password}, #{nickname}, #{telephone}, #{email}, #{gender}, #{status}, #{role}, #{companyId}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "uid")
    int insertUser(User user);

    // 更新用户
    @Update("UPDATE user SET username=#{username}, nickname=#{nickname}, telephone=#{telephone}, " +
            "email=#{email}, gender=#{gender}, update_time=#{updateTime} WHERE uid=#{uid}")
    int updateUser(User user);

    // 修改密码
    @Update("UPDATE user SET password = #{newPassword} WHERE uid = #{uid} AND password = #{oldPassword}")
    int updatePassword(@Param("uid") Long uid,
                       @Param("oldPassword") String oldPassword,
                       @Param("newPassword") String newPassword);

    // 根据ID查询用户
    @Select("SELECT * FROM user WHERE uid = #{uid}")
    User findUserById(@Param("uid") Long uid);

    // 新增：更新用户状态
    @Update("UPDATE user SET status = #{status}, update_time = #{updateTime} WHERE uid = #{uid}")
    int updateUserStatus(
            @Param("uid") Long uid,
            @Param("status") Integer status,
            @Param("updateTime") Date updateTime
    );

}