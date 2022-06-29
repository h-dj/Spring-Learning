package cn.hdj.security.core;

import cn.hdj.common.consts.GlobalConstants;
import cn.hdj.modules.user.domain.dto.SysPermissionDTO;
import cn.hdj.modules.user.service.ISysPermissionService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author huangjiajian
 * @Date 2022/6/27 下午8:08
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@Slf4j
@Service(value = "da")
public class DynamicAuthService {

    @Autowired
    @Lazy
    private ISysPermissionService sysPermissionService;

    public boolean check(Authentication authentication, HttpServletRequest request) {

        //请求url
        String url = request.getRequestURI();
        String requestMethod = request.getMethod();

        String restfulPath = requestMethod + ":" + url;

        List<SysPermissionDTO> permissions = sysPermissionService.listPermRoles();
        if (CollectionUtil.isNotEmpty(permissions)) {
            Set<String> authorizedRoles = new HashSet<>(); // 拥有访问权限的角色
            Map<String, List<String>> urlPermRoles = new HashMap<>();
            PathMatcher pathMatcher = new AntPathMatcher();
            // 初始化URL【权限->角色(集合)】规则
            List<SysPermissionDTO> urlPermList = permissions.stream()
                    .filter(item -> StrUtil.isNotBlank(item.getUrlPerm()))
                    .collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(urlPermList)) {

                urlPermList.stream().forEach(item -> {
                    String perm = item.getUrlPerm();
                    List<String> roles = item.getRoles();
                    urlPermRoles.put(perm, roles);
                });
            }

            boolean requireCheck = false; // 是否需要鉴权，默认未设置拦截规则不需鉴权

            for (Map.Entry<String, List<String>> permRoles : urlPermRoles.entrySet()) {
                String perm = permRoles.getKey();
                if (pathMatcher.match(perm, restfulPath)) {
                    //当前可访问路径的角色
                    authorizedRoles.addAll(permRoles.getValue());
                    if (requireCheck == false) {
                        requireCheck = true;
                    }
                }
            }
            // 没有设置拦截规则放行
            if (requireCheck == false) {
                return true;
            }

            // 判断JWT中携带的用户角色是否有权限访问
            return authentication.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(authority -> {
                        String roleCode = StrUtil.removePrefix(authority, GlobalConstants.AUTHORITY_PREFIX);// ROLE_ADMIN移除前缀ROLE_得到用户的角色编码ADMIN
                        if (GlobalConstants.ROOT_ROLE_CODE.equals(roleCode)) {
                            return true; // 如果是超级管理员则放行
                        }
                        return CollectionUtil.contains(authorizedRoles, roleCode);
                    });
        }
        return false;
    }
}
