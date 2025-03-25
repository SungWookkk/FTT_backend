package ftt_backend.controller;

import ftt_backend.model.UserBadge;
import ftt_backend.repository.BadgeUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BadgeController {

    @Autowired
    private BadgeUserRepository badgeUserRepository;

    @GetMapping("/user-badges/{userId}")
    public List<UserBadge> getUserBadges(@PathVariable Long userId) {
        return badgeUserRepository.findByUserId(userId);
    }
}
