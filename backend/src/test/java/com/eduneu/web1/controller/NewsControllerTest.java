package com.eduneu.web1.controller;

import com.eduneu.web1.entity.News;
import com.eduneu.web1.entity.User;
import com.eduneu.web1.mapper.NewsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NewsControllerTest {

    @Mock
    private NewsMapper newsMapper;

    @InjectMocks
    private NewsController newsController;

    private MockHttpSession session;
    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = new MockHttpSession();

        adminUser = new User();
        adminUser.setUid(1L);
        adminUser.setRole(0); // 超级管理员

        regularUser = new User();
        regularUser.setUid(2L);
        regularUser.setRole(1); // 企业用户
    }

    // ========== createNews 测试 ==========
    @Test
    void createNews_AdminUserSuccess() {
        session.setAttribute("currentUser", adminUser);
        News news = createNews(1L);

        when(newsMapper.insertNews(any(News.class))).thenReturn(1);

        int result = newsController.createNews(news, session);
        assertEquals(1, result);
        assertEquals(1, news.getStatus()); // 超级管理员直接发布
        verify(newsMapper).insertNews(any(News.class));
    }

    @Test
    void createNews_RegularUserPending() {
        session.setAttribute("currentUser", regularUser);
        News news = createNews(1L);

        when(newsMapper.insertNews(any(News.class))).thenReturn(1);

        int result = newsController.createNews(news, session);
        assertEquals(1, result);
        assertEquals(0, news.getStatus()); // 企业用户需要审核
    }

    @Test
    void createNews_UserNotLoggedIn() {
        News news = createNews(1L);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> newsController.createNews(news, session));
        assertEquals("用户未登录", exception.getMessage());
        verify(newsMapper, never()).insertNews(any(News.class));
    }

    @Test
    void createNews_DatabaseError() {
        session.setAttribute("currentUser", adminUser);
        News news = createNews(1L);

        when(newsMapper.insertNews(any(News.class)))
                .thenThrow(new DataAccessException("数据库错误") {});

        assertThrows(DataAccessException.class,
                () -> newsController.createNews(news, session));
    }

    // ========== updateNews 测试 ==========
    @Test
    void updateNews_AdminSuccess() {
        session.setAttribute("currentUser", adminUser);
        News existingNews = createNews(1L);
        News updateData = createNews(1L);
        updateData.setTitle("Updated Title");

        when(newsMapper.findNewsById(1L)).thenReturn(existingNews);
        when(newsMapper.updateNews(any(News.class))).thenReturn(1);

        int result = newsController.updateNews(1L, updateData, session);
        assertEquals(1, result);
        assertNotNull(updateData.getUpdateTime());
    }

    @Test
    void updateNews_CreatorSuccess() {
        session.setAttribute("currentUser", regularUser);
        News existingNews = createNews(1L);
        existingNews.setCreatorId(regularUser.getUid());
        News updateData = createNews(1L);
        updateData.setTitle("Updated Title");

        when(newsMapper.findNewsById(1L)).thenReturn(existingNews);
        when(newsMapper.updateNews(any(News.class))).thenReturn(1);

        int result = newsController.updateNews(1L, updateData, session);
        assertEquals(1, result);
        assertEquals(0, updateData.getStatus()); // 企业用户更新后重置为待审核
    }

    @Test
    void updateNews_Unauthorized() {
        session.setAttribute("currentUser", regularUser);
        News existingNews = createNews(1L);
        existingNews.setCreatorId(999L); // 不是当前用户创建的

        when(newsMapper.findNewsById(1L)).thenReturn(existingNews);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> newsController.updateNews(1L, createNews(1L), session));
        assertEquals("无权限操作此动态", exception.getMessage());
    }

    // ========== deleteNews 测试 ==========
    @Test
    void deleteNews_AdminSuccess() {
        session.setAttribute("currentUser", adminUser);
        News existingNews = createNews(1L);

        when(newsMapper.findNewsById(1L)).thenReturn(existingNews);
        when(newsMapper.deleteNews(1L)).thenReturn(1);

        int result = newsController.deleteNews(1L, session);
        assertEquals(1, result);
    }

    @Test
    void deleteNews_CreatorSuccess() {
        session.setAttribute("currentUser", regularUser);
        News existingNews = createNews(1L);
        existingNews.setCreatorId(regularUser.getUid());

        when(newsMapper.findNewsById(1L)).thenReturn(existingNews);
        when(newsMapper.deleteNews(1L)).thenReturn(1);

        int result = newsController.deleteNews(1L, session);
        assertEquals(1, result);
    }

    // ========== listUserNews 测试 ==========
    @Test
    void listUserNews_Success() {
        int page = 1;
        int size = 10;
        List<News> newsList = Arrays.asList(createNews(1L), createNews(2L));

        when(newsMapper.findAllNews(anyInt(), anyInt())).thenReturn(newsList);
        when(newsMapper.countNews()).thenReturn(2);

        ResponseEntity<?> response = newsController.listUserNews(page, size, null, null, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, ((NewsController.PageResult<?>) response.getBody()).getTotal());
    }

    @Test
    void listUserNews_WithQuery() {
        int page = 1;
        int size = 10;
        String query = "test";
        List<News> newsList = Collections.singletonList(createNews(1L));

        when(newsMapper.searchNewsWithPage(eq(query), anyInt(), anyInt())).thenReturn(newsList);
        when(newsMapper.countSearchedNews(query)).thenReturn(1);

        ResponseEntity<?> response = newsController.listUserNews(page, size, query, null, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, ((NewsController.PageResult<?>) response.getBody()).getTotal());
    }

    @Test
    void listUserNews_WithAuthor() {
        int page = 1;
        int size = 10;
        String author = "author1";
        List<News> newsList = Collections.singletonList(createNews(1L));

        when(newsMapper.findNewsByAuthor(eq(author), anyInt(), anyInt())).thenReturn(newsList);
        when(newsMapper.countNewsByAuthor(author)).thenReturn(1);

        ResponseEntity<?> response = newsController.listUserNews(page, size, null, author, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, ((NewsController.PageResult<?>) response.getBody()).getTotal());
    }

    @Test
    void listUserNews_InvalidParams() {
        ResponseEntity<?> response = newsController.listUserNews(0, 10, null, null, session);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("分页参数必须大于0", response.getBody());
    }

    // ========== getNewsDetail 测试 ==========
    @Test
    void getNewsDetail_Success() {
        News news = createNews(1L);
        when(newsMapper.findNewsById(1L)).thenReturn(news);

        News result = newsController.getNewsDetail(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void getNewsDetail_NotFound() {
        when(newsMapper.findNewsById(1L)).thenReturn(null);

        News result = newsController.getNewsDetail(1L);
        assertNull(result);
    }

    // ========== 辅助方法 ==========
    private News createNews(Long id) {
        News news = new News();
        news.setId(id);
        news.setTitle("Test News " + id);
        news.setContent("Content " + id);
        news.setCreateTime(new Date());
        news.setStatus(1);
        news.setCreatorId(1L);
        return news;
    }
}