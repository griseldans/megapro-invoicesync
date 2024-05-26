package com.megapro.invoicesync.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.megapro.invoicesync.dto.UserMapper;
import com.megapro.invoicesync.dto.request.CreateUserAppRequestDTO;
import com.megapro.invoicesync.service.UserService;

import jakarta.validation.Valid;

@Controller
public class UserAppController {
    @Autowired
    UserMapper userMapper;

    @Autowired
    UserService userService;

    @PostMapping("/create-user-account")
    public ResponseEntity<String> createUserAccount(@Valid @ModelAttribute CreateUserAppRequestDTO userAppDTO){
        var userApp =  userMapper.createUserAppRequestDTOToUserApp(userAppDTO);
        userService.createUserApp(userApp, userAppDTO);
        return ResponseEntity.ok(userApp.getEmail());
    }

}
