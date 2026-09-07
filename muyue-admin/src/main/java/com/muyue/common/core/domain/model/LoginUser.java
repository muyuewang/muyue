package com.muyue.common.core.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.muyue.common.core.domain.entity.SysUser;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 登录用户身份（Spring Security 用户主体）
 *
 * @author muyue
 */
@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long deptId;

    /** 登录令牌 */
    private String token;

    /** 登录时间（毫秒） */
    private Long loginTime;

    /** 过期时间（毫秒） */
    private Long expireTime;

    /** 登录 IP */
    private String ipaddr;

    /** 用户信息 */
    private SysUser user;

    /** 权限标识集合 */
    private Set<String> permissions;

    /** 角色标识集合 */
    private List<String> roles;

    public LoginUser(SysUser user, Set<String> permissions, List<String> roles) {
        this.user = user;
        this.userId = user.getUserId();
        this.deptId = user.getDeptId();
        this.permissions = permissions;
        this.roles = roles;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (permissions != null) {
            permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        }
        if (roles != null) {
            roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
        }
        return authorities;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return user != null ? user.getPassword() : null;
    }

    @Override
    public String getUsername() {
        return user != null ? user.getUserName() : null;
    }

    /** 账号是否未过期 */
    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    /** 账号是否未锁定 */
    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    /** 凭证是否未过期 */
    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** 是否启用 */
    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return user != null && "0".equals(user.getStatus());
    }
}
