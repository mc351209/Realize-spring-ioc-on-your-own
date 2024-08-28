package com.example.springioc.service.impl;

import com.example.springioc.anno.Bean;
import com.example.springioc.anno.Di;
import com.example.springioc.dao.UserDao;
import com.example.springioc.service.UserService;

@Bean
public class UserServiceImpl implements UserService {

    @Di
    private UserDao userDao;

    @Override
    public void add() {
        System.out.println("service...");
        userDao.add();
    }
}
