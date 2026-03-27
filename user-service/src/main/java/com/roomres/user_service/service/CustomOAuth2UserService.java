package com.roomres.user_service.service;

import com.roomres.user_service.model.User;
import com.roomres.user_service.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;


import java.util.Map;

@Service
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
        final String finalEmail = email;

        // Busca o usuário pelo ID do GitHub ou cria um novo usando construtor manual
        userRepository.findByProviderId(providerId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(login);
                    newUser.setEmail(finalEmail);
                    newUser.setName(name);
                    newUser.setProvider("github");
                    newUser.setProviderId(providerId);
                    newUser.setRole("USER");
                    return userRepository.save(newUser);
                });


        return oAuth2User;
    }

}
