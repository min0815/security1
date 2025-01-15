package com.cos.security1.config;

import com.cos.security1.config.oauth.PrincipalOauth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// 1. 코드받기 2. 액세스토큰 3. 사용자프로필정보가져옴 4-1. 그 정보를 토대로 회원가입 자동 진행
// 4-2. (이메일, 전화번호, 이름, 아이디) -> (집주소), 백화점몰 -> (vip등급, 일반등급)

@Configuration
@EnableMethodSecurity(securedEnabled = true) // secured 어노테이션 활성화
// prePostEnabled = true는 default
public class SecurityConfig {

    @Autowired
    private PrincipalOauth2UserService principalOauth2UserService;

    @Bean // 해당 메소드의 리턴되는 오브젝트를 IoC로 등록해준다.
    public BCryptPasswordEncoder encodePwd() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth
                -> auth
                .requestMatchers("/user/**").authenticated()
                .requestMatchers("/manager/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
                ).formLogin(form -> form
                        .loginPage("/loginForm") // 설정하고부터 접근권한 없는 페이지 가면 로그인 페이지로 이동
                        // .usernameParameter("username2") 만약 파라미터 이름이 username이랑 다를 때 설정
                        .loginProcessingUrl("/login") // /login 주소가 호출이 되면 시큐리티가 낚아채서 대신 로그인을 진행합니다
                        .defaultSuccessUrl("/"))
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/loginForm") // 이 뒤에 구글 로그인 완료된 뒤의 후처리 필요
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                .userService(principalOauth2UserService))
                        );

        return http.build();
    }
}
