package com.muyue.common.config;

import com.muyue.common.config.properties.SecurityProperties;
import com.muyue.common.security.AccessDeniedHandlerImpl;
import com.muyue.common.security.AuthenticationEntryPointImpl;
import com.muyue.common.security.JwtAuthenticationTokenFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.web.util.pattern.PatternParseException;

import java.util.Arrays;

/**
 * Spring Security 配置（无状态 + JWT）
 *
 * @author muyue
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final AccessDeniedHandlerImpl accessDeniedHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * AuthenticationManager（密码校验由 DaoAuthenticationProvider + UserDetailsService 完成）
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // 禁用 Spring Security 默认的 /logout 处理，登出由 AuthController 删除 Redis/内存会话
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .headers(headers -> headers.frameOptions(frame -> frame.disable()).cacheControl(cache -> cache.disable()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(safePatterns(securityProperties.getAnonymousArray())).permitAll()
                        .requestMatchers(safePatterns(securityProperties.getExcludesArray())).permitAll()
                        .anyRequest().authenticated());

        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 过滤非法的路径表达式。
     * <p>
     * Spring Security 6 使用 PathPattern，诸如 /**\/*.css 这类写法会在请求匹配阶段抛
     * PatternParseException 导致所有接口 500，这里统一校验并忽略，仅打印告警。
     */
    private String[] safePatterns(String[] patterns) {
        if (patterns == null || patterns.length == 0) {
            return new String[0];
        }
        PathPatternParser parser = new PathPatternParser();
        return Arrays.stream(patterns)
                .filter(pattern -> {
                    try {
                        parser.parse(pattern);
                        return true;
                    } catch (PatternParseException e) {
                        log.warn("忽略非法的路径匹配表达式：{}（PathPattern 中 ** 只能出现在末尾）", pattern);
                        return false;
                    }
                })
                .toArray(String[]::new);
    }
}
