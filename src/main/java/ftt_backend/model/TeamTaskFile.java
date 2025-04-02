package ftt_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * - 서버 로컬 디렉토리에 저장된 파일의 메타데이터를 저장
 * - 파일은 실제로 /uploads/team_tasks/ 등의 경로에 저장되며, DB에는 파일 경로 및 관련 정보만 저장
 * - TeamTask와 1:N 관계 (하나의 TeamTask가 여러 파일을 가질 수 있음)
 */
@Entity
@Table(name = "team_task_file")
@Data
@Getter
@Setter
public class TeamTaskFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자가 업로드한 원본 파일명 (다운로드 시 원본 파일명 제공)
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    // 서버에 저장된 파일명 (충돌 방지를 위해 UUID 등으로 생성)
    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    // 파일이 저장된 경로
    @Column(name = "file_path", nullable = false)
    private String filePath;

    // 파일 크기 (바이트 단위)
    @Column(name = "file_size", nullable = false)
    private long fileSize;

    // 파일 업로드 시각
    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;

    /**
     * 팀 작업과 다대일 관계
     * 하나의 TeamTask는 여러 개의 TeamTaskFile을 가질 수 있음
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_task_id")
    private TeamTask teamTask;
}
