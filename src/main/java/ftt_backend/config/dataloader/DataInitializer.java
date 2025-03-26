/**
 *  서버 구동 시점에 DB에 존재하지 않는 경우 뱃지 초기 데이터를 삽입 하는 코드
 * */

package ftt_backend.config.dataloader;

import ftt_backend.model.Badge;
import ftt_backend.repository.BadgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private BadgeRepository badgeRepository;

    @Override
    public void run(String... args) throws Exception {
        // 이미 존재하는지 체크
        if (badgeRepository.count() == 0) {
            // Badge_01
            Badge b1 = new Badge();
            b1.setBadgeName("뚜벅뚜벅 초심자");
            b1.setDescription("계정 생성시 주어지는 뱃지");
            b1.setIconPath("/badge/Badge_01.svg");
            b1.setCompletionThreshold(0); // 0 이상이면 획득 가능
            badgeRepository.save(b1);

            // Badge_02
            Badge b2 = new Badge();
            b2.setBadgeName("목표를 위한 노력가!");
            b2.setDescription("작업을 30회 이상 완료");
            b2.setIconPath("/badge/Badge_02.svg");
            b2.setCompletionThreshold(30);
            badgeRepository.save(b2);

            // Badge_02
            Badge b3 = new Badge();
            b3.setBadgeName("꾸준한 실천러!");
            b3.setDescription("작업을 80회 이상 완료");
            b3.setIconPath("/badge/Badge_03.svg");
            b3.setCompletionThreshold(80);
            badgeRepository.save(b3);

            // Badge_04
            Badge b4 = new Badge();
            b4.setBadgeName("열일 챔피언!");
            b4.setDescription("작업을 120회 이상 완료");
            b4.setIconPath("/badge/Badge_04.svg");
            b4.setCompletionThreshold(120);
            badgeRepository.save(b4);

            // Badge_05
            Badge b5 = new Badge();
            b5.setBadgeName("미루기를 모르는 사람!");
            b5.setDescription("작업을 300회 이상 완료");
            b5.setIconPath("/badge/Badge_05.svg");
            b5.setCompletionThreshold(300);
            badgeRepository.save(b5);

            // Badge_06
            Badge b6 = new Badge();
            b6.setBadgeName("갓생을 위한 노력");
            b6.setDescription("작업을 700회 이상 완료");
            b6.setIconPath("/badge/Badge_06.svg");
            b6.setCompletionThreshold(700);
            badgeRepository.save(b6);

            // Badge_07
            Badge b7 = new Badge();
            b7.setBadgeName("이정도면 미친 사람");
            b7.setDescription("작업을 1200회 이상 완료");
            b7.setIconPath("/badge/Badge_07.svg");
            b7.setCompletionThreshold(1200);
            badgeRepository.save(b7);

            // Badge_08
            Badge b8 = new Badge();
            b8.setBadgeName("갓생");
            b8.setDescription("작업을 2000회 이상 완료");
            b8.setIconPath("/badge/Badge_08.svg");
            b8.setCompletionThreshold(2000);
            badgeRepository.save(b8);

        }
    }
}
