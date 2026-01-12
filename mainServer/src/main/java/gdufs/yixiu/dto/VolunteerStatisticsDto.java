package gdufs.yixiu.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@ColumnWidth(25)
public class VolunteerStatisticsDto {
    @ExcelProperty("维修记录ID")
    private Integer logId;

    @ExcelProperty("志愿者姓名")
    private String volunteerName;

    @ExcelProperty("志愿者ID")
    private Integer volunteerId;

    @ExcelProperty("任务ID")
    private Integer requestId;

    @ExcelProperty("维修内容")
    private String logContent;

    @ExcelProperty("维修时长")
    private String repairDuration;

    @ExcelProperty("解决方案摘要")
    private String solutionSummary;

    @ExcelProperty("上传时间")
    private LocalDateTime uploadTime;

}
