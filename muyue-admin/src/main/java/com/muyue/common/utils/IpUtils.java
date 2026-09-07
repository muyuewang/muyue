package com.muyue.common.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 网络相关工具
 *
 * @author muyue
 */
public class IpUtils {

    private IpUtils() {
    }

    /**
     * 获取客户端 IP
     */
    public static String getIpAddr(HttpServletRequest request) {
        return ServletUtils.getIpAddress(request);
    }

    /**
     * 获取本机 IP
     */
    public static String getHostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    /**
     * 获取本机名称
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "未知";
        }
    }
}
