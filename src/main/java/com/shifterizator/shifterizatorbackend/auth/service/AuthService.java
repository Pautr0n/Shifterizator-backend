package com.shifterizator.shifterizatorbackend.auth.service;

import com.shifterizator.shifterizatorbackend.user.model.User;

public class AuthService {

    public User getAuthenticatedUser(){
        return new User();
    }
}
