package gdufs.yixiu.controller;

import com.alibaba.excel.EasyExcel;
import gdufs.yixiu.annotation.AdminLoginToken;
import gdufs.yixiu.dao.VolunteerMapper;
import gdufs.yixiu.dto.VolunteerStatisticsDto;
import gdufs.yixiu.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/export")
public class ExportController {
    @Autowired
    private TaskService taskService;
    @Autowired
    private VolunteerMapper volunteerMapper;

    @AdminLoginToken
    @GetMapping("/volunteerStatistics")
    public ResponseEntity<StreamingResponseBody> exportVolunteerRepairLogsWithDate(
            @RequestParam(name = "volunteerId") Integer volunteerId,
            @RequestParam(name = "startDate") String startDate,
            @RequestParam(name = "endDate") String endDate
            ) {
        String volunteerName = volunteerMapper.findVolunteerNameByVolunteerId(volunteerId);
        // 构建文件名
        String datePart = "";
        if (startDate != null && endDate != null) {
            datePart = "_" + startDate + "_至_" + endDate;
        } else if (startDate != null) {
            datePart = "_" + startDate + "_起";
        } else if (endDate != null) {
            datePart = "_至_" + endDate;
        }

        String fileName = URLEncoder.encode(
                "志愿者_" + volunteerName + "_" + datePart + "_维修记录.xlsx",
                StandardCharsets.UTF_8
        ).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        );
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename*=''" + fileName );
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");

        StreamingResponseBody body = outputStream -> {
            List<VolunteerStatisticsDto> repairLogs =
                    taskService.exportRepairLogsWithDate(volunteerId, startDate, endDate);

            EasyExcel.write(outputStream, VolunteerStatisticsDto.class)
                    .autoCloseStream(false)
                    .sheet("维修记录")
                    .doWrite(repairLogs);

            log.info("导出志愿者 (volunteerId:{}) 的维修记录 {}条 成功", volunteerId, repairLogs.size());
        };
        return ResponseEntity.ok()
                .headers(headers)
                .body(body);
    }
}
