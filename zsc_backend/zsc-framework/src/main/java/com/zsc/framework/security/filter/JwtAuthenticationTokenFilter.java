package com.zsc.framework.security.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.alibaba.fastjson2.JSON;
import com.zsc.common.constant.HttpStatus;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.domain.model.LoginUser;
import com.zsc.common.utils.SecurityUtils;
import com.zsc.common.utils.ServletUtils;
import com.zsc.common.utils.StringUtils;
import com.zsc.framework.web.service.TokenService;

/**
 * token过滤器 验证token有效性
 * 
 * @author zsc
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter
{
    @Autowired
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNull(SecurityUtils.getAuthentication()))
        {
            tokenService.verifyToken(loginUser);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        else if (StringUtils.isNull(loginUser) && StringUtils.isNotEmpty(request.getHeader("Authorization")))
        {
            // token存在但Redis中已无数据（被管理员强制下线），直接返回401
            int code = HttpStatus.UNAUTHORIZED;
            String msg = "您的账号已被停用或已在其他设备登录，请重新登录";
            ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(code, msg)));
            return;
        }
        chain.doFilter(request, response);
    }
}
