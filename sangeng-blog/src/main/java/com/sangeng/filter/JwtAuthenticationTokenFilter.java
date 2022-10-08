package com.sangeng.filter;

import com.alibaba.fastjson.JSON;
import com.sangeng.domain.ResponseResult;
import com.sangeng.domain.entity.LoginUser;
import com.sangeng.enums.AppHttpCodeEnum;
import com.sangeng.utils.JwtUtil;
import com.sangeng.utils.RedisCache;
import com.sangeng.utils.WebUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private RedisCache redisCache;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //获取请求头中的token
        String token=request.getHeader("token");

        //解析获取的userid
        if (!StringUtils.hasText(token)){
            //说明该接口不需要登录，直接放行
            filterChain.doFilter(request,response);
            return;
        }
        Claims claims=null;
        try {
             claims= JwtUtil.parseJWT(token);
        } catch (Exception e) {
            e.printStackTrace();
            //token超时 token非法
            //响应告诉前端重新登录
            ResponseResult result=ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
            WebUtils.renderString(response, JSON.toJSONString(result));
            return;
        }
        String userId=claims.getSubject();
        //从Redis中获取用户信息
        LoginUser loginUser= redisCache.getCacheObject("bloglogin"+userId);
        if (Objects.isNull(loginUser)){
            //登录过期 提示重新登录
            ResponseResult result=ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
            WebUtils.renderString(response, JSON.toJSONString(result));
            return;
        }
        //存入securityContextHolder
        UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(loginUser,null,null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request,response);
    }
}
