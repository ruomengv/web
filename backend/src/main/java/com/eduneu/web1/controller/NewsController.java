package com.eduneu.web1.controller;

import com.eduneu.web1.entity.News;
import com.eduneu.web1.entity.User;
import com.eduneu.web1.mapper.NewsMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsMapper newsMapper;

    // 获取当前用户
    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    // 发布动态
    @PostMapping
    public int createNews(@RequestBody News news, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }

        // 设置创建时间
        news.setCreateTime(new Date());

        // 超级管理员直接发布，企业用户需要审核
        news.setStatus(currentUser.getRole() == 0 ? 1 : 0);
        news.setCreatorId(currentUser.getUid());
        return newsMapper.insertNews(news);
    }

    // 更新动态
    @PutMapping("/{id}")
    public int updateNews(@PathVariable Long id, @RequestBody News news,
                          HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }

        News existing = newsMapper.findNewsById(id);

        // 权限检查：超级管理员或创建者本人
        if (currentUser.getRole() != 0 && !existing.getCreatorId().equals(currentUser.getUid())) {
            throw new RuntimeException("无权限操作此动态");
        }

        news.setId(id);
        // 设置更新时间
        news.setUpdateTime(new Date());

        // 如果是企业用户更新，重置为待审核状态
        if (currentUser.getRole() != 0) {
            news.setStatus(0);
        }
        return newsMapper.updateNews(news);
    }

    // 删除动态
    @DeleteMapping("/{id}")
    public int deleteNews(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }

        News existing = newsMapper.findNewsById(id);
        // 权限检查：超级管理员或创建者本人
        if (currentUser.getRole() != 0 && !existing.getCreatorId().equals(currentUser.getUid())) {
            throw new RuntimeException("无权限操作此动态");
        }
        return newsMapper.deleteNews(id);
    }

    // 获取用户动态列表（带分页）
    @GetMapping
    public ResponseEntity<?> listUserNews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String author,
            HttpSession session) {

        // 参数验证
        if (page < 1 || size < 1) {
            return ResponseEntity.badRequest().body("分页参数必须大于0");
        }

        int offset = (page - 1) * size;
        List<News> newsList;
        int total;

        if (query != null && !query.isEmpty()) {
            // 使用重命名后的方法
            newsList = newsMapper.searchNewsWithPage(query, offset, size);
            total = newsMapper.countSearchedNews(query);
        } else if (author != null && !author.isEmpty()) {
            newsList = newsMapper.findNewsByAuthor(author, offset, size);
            total = newsMapper.countNewsByAuthor(author);
        } else {
            newsList = newsMapper.findAllNews(offset, size);
            total = newsMapper.countNews();
        }

        return ResponseEntity.ok(new PageResult(total, newsList));
    }

    // 动态详情
    @GetMapping("/{id}")
    public News getNewsDetail(@PathVariable Long id) {
        return newsMapper.findNewsById(id);
    }

    // 分页结果封装类
    static class PageResult<T> {
        private int total;
        private List<T> list;

        public PageResult(int total, List<T> list) {
            this.total = total;
            this.list = list;
        }

        public int getTotal() { return total; }
        public List<T> getList() { return list; }
    }
}