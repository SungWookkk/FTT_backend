package ftt_backend.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TeamDto {
    private Long id;
    private String teamName;
    private String description;
    private String announcement;
    private String category;
    private String status;
    private List<String> memberUsernames;  // 멤버들의 닉네임
    private String leaderUsername;         // 팀장 닉네임

}
