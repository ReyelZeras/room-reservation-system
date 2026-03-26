package com.roomres.user_service.service;

import com.roomres.user_service.model.User;
import com.roomres.user_service.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Objects;

public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest){
        OAuth2User oAuth2User = super.loadUser(userRequest);

        //Extrair dados do github
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String providerId = String.valueOf(attributes.get("id"));
        String name = (String) attributes.get("name");
        String login = (String) attributes.get("login");

        //Se o email vier nulo (comum no git se estiver privado), login é utilizado
        if (email == null) email = login + "@github.com";

        //Guardar ou atualizar no banco
        String finalEmail = email;
        userRepository.findByProviderId(providerId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(finalEmail)
                        .username(login)
                        .name(name)
                        .provider("github")
                        .providerId(providerId)
                        .role("USER")
                        .build()
                ));

        return oAuth2User;
    }

}
