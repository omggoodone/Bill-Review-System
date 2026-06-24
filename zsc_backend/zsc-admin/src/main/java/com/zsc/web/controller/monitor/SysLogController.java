package com.zsc.web.controller.monitor;

import com.zsc.common.core.domain.AjaxResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 系统日志文件查看
 */
@RestController
@RequestMapping("/monitor/logs")
public class SysLogController {

    private static final String LOG_DIR = "/opt/bill-review/logs/";

    /** 允许查看的日志文件白名单 */
    private static final Set<String> ALLOWED_FILES = Set.of(
        "app.log",
        "nginx-access.log",
        "nginx-error.log"
    );

    /**
     * 读取日志文件尾部内容
     */
    @PreAuthorize("@ss.hasPermi('monitor:log:list')")
    @GetMapping
    public AjaxResult readLog(@RequestParam(defaultValue = "app") String file,
                              @RequestParam(defaultValue = "200") int lines) {
        if (!ALLOWED_FILES.contains(file)) {
            return AjaxResult.error("不允许查看该日志文件");
        }
        if (lines < 1 || lines > 2000) {
            lines = 200;
        }

        String path = LOG_DIR + file;
        List<String> result = readLastLines(path, lines);
        return AjaxResult.success(Map.of(
            "file", file,
            "lines", result.size(),
            "content", String.join("\n", result)
        ));
    }

    /**
     * 读取文件最后 N 行
     */
    private List<String> readLastLines(String path, int maxLines) {
        List<String> lines = new ArrayList<>(maxLines);
        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            long pos = raf.length() - 1;
            StringBuilder sb = new StringBuilder();
            int lineCount = 0;

            while (pos >= 0 && lineCount < maxLines) {
                raf.seek(pos);
                char c = (char) raf.readByte();
                if (c == '\n' && sb.length() > 0) {
                    lines.add(0, sb.reverse().toString());
                    sb.setLength(0);
                    lineCount++;
                } else if (c != '\r') {
                    sb.append(c);
                }
                pos--;
            }
            // 最后一行（没有换行符的情况）
            if (sb.length() > 0 && lineCount < maxLines) {
                lines.add(0, sb.reverse().toString());
            }
        } catch (IOException e) {
            lines.add(0, "读取日志失败: " + e.getMessage());
        }
        return lines;
    }
}
