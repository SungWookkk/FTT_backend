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
            b1.setBadgeName("Badge_01");
            b1.setDescription("초급 뱃지입니다.");
            b1.setIconPath("/badge/Badge_01.svg");
            badgeRepository.save(b1);

            // Badge_02
            Badge b2 = new Badge();
            b2.setBadgeName("Badge_02");
            b2.setDescription("중급 뱃지입니다.");
            b2.setIconPath("/badge/Badge_02.svg");
            badgeRepository.save(b2);

            // Badge_02
            Badge b3 = new Badge();
            b3.setBadgeName("Badge_03");
            b3.setDescription("중급 뱃지입니다.");
            b3.setIconPath("/badge/Badge_03.svg");
            badgeRepository.save(b3);

            // Badge_04
            Badge b4 = new Badge();
            b4.setBadgeName("Badge_04");
            b4.setDescription("중급 뱃지입니다.");
            b4.setIconPath("/badge/Badge_04.svg");
            badgeRepository.save(b4);

            // Badge_05
            Badge b5 = new Badge();
            b5.setBadgeName("Badge_05");
            b5.setDescription("중급 뱃지입니다.");
            b5.setIconPath("/badge/Badge_05.svg");
            badgeRepository.save(b5);

            // Badge_06
            Badge b6 = new Badge();
            b6.setBadgeName("Badge_06");
            b6.setDescription("중급 뱃지입니다.");
            b6.setIconPath("/badge/Badge_06.svg");
            badgeRepository.save(b6);

            // Badge_07
            Badge b7 = new Badge();
            b7.setBadgeName("Badge_07");
            b7.setDescription("중급 뱃지입니다.");
            b7.setIconPath("/badge/Badge_07.svg");
            badgeRepository.save(b7);

            // Badge_08
            Badge b8 = new Badge();
            b8.setBadgeName("Badge_08");
            b8.setDescription("중급 뱃지입니다.");
            b8.setIconPath("/badge/Badge_08.svg");
            badgeRepository.save(b8);

            // Badge_09
            Badge b9 = new Badge();
            b9.setBadgeName("Badge_09");
            b9.setDescription("중급 뱃지입니다.");
            b9.setIconPath("/badge/Badge_09.svg");
            badgeRepository.save(b9);
        }
    }
}
