package com.shifterizator.shifterizatorbackend.user.service;


import com.shifterizator.shifterizatorbackend.user.dto.ChangePasswordRequestDto;
import com.shifterizator.shifterizatorbackend.user.mapper.UserMapper;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ChangePasswordUserService {

    private UserRepository userRepository;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void changeOwnPassword(User authenticatedUser, ChangePasswordRequestDto dto) {

        if (!passwordEncoder.matches(dto.currentPassword(), authenticatedUser.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        String hashed = passwordEncoder.encode(dto.newPassword());
        authenticatedUser.setPassword(hashed);

        userRepository.save(authenticatedUser);
    }



}
