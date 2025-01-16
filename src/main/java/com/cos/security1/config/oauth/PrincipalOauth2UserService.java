package com.cos.security1.config.oauth;

import com.cos.security1.config.auth.PrincipalDetails;
import com.cos.security1.model.User;
import com.cos.security1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    // 구글로 부터 받은 userRequest 데이터에 대한 후처리되는 함수
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("getClientRegistration: " + userRequest.getClientRegistration());
        System.out.println("getAccessToken: " + userRequest.getAccessToken().getTokenValue());
        System.out.println("getAttributes: " + super.loadUser(userRequest).getAttributes());
        // 위 정보들을 토대로 회원가입 강제 진행 예정

        OAuth2User oAuth2User = super.loadUser(userRequest);
        // 구글로그인 버튼 클릭 -> 구글로그인창 -> 구글로그인 완료 -> code를 리턴(OAuth-Client라이브러리) -> AccessToken요청
        // userRequest 정보 -> loadUser 함수 호출 -> 구글로부터 회원 프로필을 받아준다
        System.out.println("getAttributes: " + oAuth2User.getAttributes()); // super.loadUser(userRequest).getAttributes()); 와 같음

        String provider = userRequest.getClientRegistration().getClientId(); // google
        String providerId = oAuth2User.getAttribute("sub");
        String username = provider + "_" + providerId; // google_12313244325243
        String password = bCryptPasswordEncoder.encode("겟인데어");
        String email = oAuth2User.getAttribute("email");
        String role = "ROLE_USER";

        User userEntity = userRepository.findByUsername(username);

        if (userEntity == null) {
            userEntity = User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .role(role)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userRepository.save(userEntity);
        }

        return new PrincipalDetails(userEntity, oAuth2User.getAttributes());
    }
}