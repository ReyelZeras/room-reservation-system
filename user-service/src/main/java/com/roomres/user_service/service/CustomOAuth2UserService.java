package com.roomres.user_service.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    public CustomOAuth2UserService(UserService userService){
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest){
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String providerId = Objects.toString(attributes.get("id"), null);
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String login = (String) attributes.get("login"); // Username vindo do GitHub

        if (email == null) {
            email = login + "@github.com";
        }

        // Extrai o nome do provedor dinamicamente (ex: "github")
        String provider = userRequest.getClientRegistration().getRegistrationId();

        // CORREÇÃO: Passando os 5 parâmetros EXATAMENTE na ordem que o UserService exige:
        // (email, username, name, provider, providerId)
        userService.processOAuthUser(email, login, name, provider, providerId);

        return oAuth2User;
    }
}