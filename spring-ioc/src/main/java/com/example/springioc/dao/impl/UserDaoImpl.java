package com.example.springioc.dao.impl;

import com.example.springioc.anno.Bean;
import com.example.springioc.dao.UserDao;

@Bean
public class UserDaoImpl implements UserDao {

    @Override
    public void add() {
        System.out.println("dao。。。");
    }
}
