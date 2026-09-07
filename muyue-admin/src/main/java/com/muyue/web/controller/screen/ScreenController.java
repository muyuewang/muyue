package com.muyue.web.controller.screen;

import com.muyue.common.core.domain.R;
import com.muyue.screen.service.ScreenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 数据大屏控制器（登录即可访问）
 *
 * @author muyue
 */
@RestController
@RequestMapping("/screen")
@RequiredArgsConstructor
public class ScreenController {

    private final ScreenService screenService;

    /** 大屏统计数据 */
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        return R.ok(screenService.stats());
    }
}
