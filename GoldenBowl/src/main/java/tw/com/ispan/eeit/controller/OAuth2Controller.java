package tw.com.ispan.eeit.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class OAuth2Controller {
	public String loginSuccess(@AuthenticationPrincipal OAuth2User principal) {
		String email = principal.getAttribute("email");
		String name = principal.getAttribute("name");
		return "Google 登入成功，歡迎" + name + "Email" + email;
	}
}