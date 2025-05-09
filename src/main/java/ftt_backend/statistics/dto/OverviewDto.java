package ftt_backend.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OverviewDto {
    private String title;
    private String value;
    private String subtitle;   // 없으면 null
}
