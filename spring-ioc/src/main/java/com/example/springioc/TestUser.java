package com.example.springioc;

import com.example.springioc.bean.AnnotationApplicationContext;
import com.example.springioc.bean.ApplicationContext;
import com.example.springioc.service.UserService;

public class TestUser {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationApplicationContext("com.example.springioc");
        UserService userService = (UserService) context.getBean(UserService.class);
        userService.add();
    }
}
