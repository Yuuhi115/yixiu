package gdufs.yixiu.dto.community.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VolunteerDataVO {
    private Integer volunteerId;
    private String grade;
    private Integer status;
    private Integer fixedNum;
    private Double finishRate;
    private Integer contactType;
    private String contactNumber;
}
