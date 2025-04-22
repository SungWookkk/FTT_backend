package ftt_backend.socket;

import lombok.Data;

@Data
public class PresenceMessage {
    private Long userId;
    private Long teamId;
}
