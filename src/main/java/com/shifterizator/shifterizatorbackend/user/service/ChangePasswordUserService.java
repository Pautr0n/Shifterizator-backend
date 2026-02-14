package com.shifterizator.shifterizatorbackend.user.service;


import com.shifterizator.shifterizatorbackend.user.dto.ChangePasswordRequestDto;
import com.shifterizator.shifterizatorbackend.user.exception.InvalidPasswordException;
import com.shifterizator.shifterizatorbackend.user.mapper.UserMapper;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ChangePasswordUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

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
