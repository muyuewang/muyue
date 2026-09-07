package com.muyue.common.utils;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

/**
 * 验证码生成工具（无第三方依赖，基于 AWT 绘制）
 *
 * @author muyue
 */
public class CaptchaUtils {

    private static final int WIDTH = 116;
    private static final int HEIGHT = 38;
    private static final Random RANDOM = new SecureRandom();

    private CaptchaUtils() {
    }

    /**
     * 生成算术表达式验证码
     *
     * @return [0] 表达式文本（如 "8 + 5 = ?"），[1] 计算结果
     */
    public static String[] generateMath() {
        int type = RANDOM.nextInt(3);
        int a;
        int b;
        int result;
        String op;
        switch (type) {
            case 0 -> {
                a = RANDOM.nextInt(90) + 10;
                b = RANDOM.nextInt(90) + 10;
                op = "+";
                result = a + b;
            }
            case 1 -> {
                a = RANDOM.nextInt(90) + 10;
                b = RANDOM.nextInt(90) + 10;
                if (a < b) {
                    int t = a;
                    a = b;
                    b = t;
                }
                op = "-";
                result = a - b;
            }
            default -> {
                a = RANDOM.nextInt(9) + 1;
                b = RANDOM.nextInt(9) + 1;
                op = "x";
                result = a * b;
            }
        }
        return new String[]{a + " " + op + " " + b + " = ?", String.valueOf(result)};
    }

    /**
     * 生成字符验证码
     *
     * @param length 字符长度
     * @return [0] 验证码文本，[1] 验证码文本（大写）
     */
    public static String[] generateChar(int length) {
        String source = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(source.charAt(RANDOM.nextInt(source.length())));
        }
        String code = sb.toString();
        return new String[]{code, code};
    }

    /**
     * 绘制验证码图片并转为 base64
     *
     * @param text 验证码文本
     * @return data:image/png;base64,xxx
     */
    public static String toBase64(String text) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 背景
        g.setColor(new Color(245, 247, 250));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 干扰线
        g.setStroke(new BasicStroke(1.2f));
        for (int i = 0; i < 6; i++) {
            g.setColor(randomColor(180, 230));
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 干扰点
        for (int i = 0; i < 40; i++) {
            g.setColor(randomColor(150, 220));
            g.fillOval(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT), 2, 2);
        }

        // 文本
        char[] chars = text.toCharArray();
        int fontSize = text.length() > 8 ? 16 : 22;
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
        g.setFont(font);
        int charWidth = (WIDTH - 12) / Math.max(chars.length, 1);
        for (int i = 0; i < chars.length; i++) {
            g.setColor(randomColor(20, 130));
            AffineTransform transform = new AffineTransform();
            transform.translate(8 + i * charWidth, HEIGHT / 2 + fontSize / 2 - 2);
            double angle = (RANDOM.nextDouble() - 0.5) * 0.5;
            transform.rotate(angle);
            g.setFont(font.deriveFont(transform));
            g.drawString(String.valueOf(chars[i]), 0, 0);
        }
        g.dispose();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("验证码图片生成失败", e);
        }
    }

    private static Color randomColor(int min, int max) {
        int range = max - min;
        return new Color(min + RANDOM.nextInt(range), min + RANDOM.nextInt(range), min + RANDOM.nextInt(range));
    }
}
