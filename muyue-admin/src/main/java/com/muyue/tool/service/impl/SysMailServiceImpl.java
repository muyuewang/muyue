package com.muyue.tool.service.impl;

import com.muyue.common.core.domain.entity.SysMailConfig;
import com.muyue.common.exception.ServiceException;
import com.muyue.tool.mapper.SysMailConfigMapper;
import com.muyue.tool.service.ISysMailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * 邮箱工具服务实现（按库中配置动态构建 JavaMailSender）
 *
 * @author muyue
 */
@Service
@RequiredArgsConstructor
public class SysMailServiceImpl implements ISysMailService {

    private static final Long CONFIG_ID = 1L;

    private final SysMailConfigMapper configMapper;

    @Override
    public SysMailConfig getConfig() {
        SysMailConfig config = configMapper.selectById(CONFIG_ID);
        if (config == null) {
            config = new SysMailConfig();
            config.setConfigId(CONFIG_ID);
            config.setPort(465);
            config.setSslEnabled("1");
            configMapper.insert(config);
        }
        return config;
    }

    @Override
    public boolean saveConfig(SysMailConfig config) {
        config.setConfigId(CONFIG_ID);
        if (configMapper.selectById(CONFIG_ID) == null) {
            return configMapper.insert(config) > 0;
        }
        return configMapper.updateById(config) > 0;
    }

    @Override
    public void sendMail(String to, String subject, String content) {
        SysMailConfig config = getConfig();
        if (config.getHost() == null || config.getHost().isBlank()) {
            throw new ServiceException("请先在「邮箱配置」中完善 SMTP 信息");
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getHost());
        sender.setPort(config.getPort() == null ? 465 : config.getPort());
        sender.setUsername(config.getUsername());
        sender.setPassword(config.getPassword());
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        if ("1".equals(config.getSslEnabled())) {
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }
        props.put("mail.smtp.connectiontimeout", "8000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            String from = config.getFromAddr() != null && !config.getFromAddr().isBlank()
                    ? config.getFromAddr() : config.getUsername();
            helper.setFrom(from, config.getNickname() == null ? "" : config.getNickname());
            helper.setTo(to.split("[,;，；]"));
            helper.setSubject(subject);
            helper.setText(content, true);
            sender.send(message);
        } catch (Exception e) {
            throw new ServiceException("邮件发送失败：" + e.getMessage());
        }
    }
}
