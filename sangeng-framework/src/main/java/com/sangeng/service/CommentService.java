package com.sangeng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sangeng.domain.ResponseResult;
import com.sangeng.domain.entity.Comment;


/**
 * 评论表(Comment)表服务接口
 *
 * @author makejava
 * @since 2022-10-12 15:18:08
 */
public interface CommentService extends IService<Comment> {


    ResponseResult addComment(Comment comment);

    ResponseResult commentList(String commentType, Long articleId, Integer pageNum, Integer pageSize);
}
