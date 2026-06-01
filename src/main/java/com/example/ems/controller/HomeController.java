package com.example.ems.controller;

import com.example.ems.repository.UserAccountRepository;
import com.example.ems.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	private final UserAccountRepository userRepo;
	private final NotificationService notificationService;

	public HomeController(UserAccountRepository userRepo, NotificationService notificationService) {
		this.userRepo = userRepo;
		this.notificationService = notificationService;
	}

	@GetMapping("/")
	public String index(Authentication auth, Model model) {
		if (auth != null && auth.isAuthenticated()) {
			String username = auth.getName();
			var user = userRepo.findByUsername(username);
			if (user.isPresent()) {
				long unreadCount = notificationService.getUnreadCount(user.get());
				model.addAttribute("unreadNotificationCount", unreadCount);
			}
		}
		return "index";
	}

	@GetMapping("/help")
	public String help() {
		return "help";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}
}


