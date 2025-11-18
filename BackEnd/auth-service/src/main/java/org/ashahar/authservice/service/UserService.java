package org.ashahar.authservice.service;

import org.ashahar.authservice.repo.UserRepo;
import org.ashahar.authservice.model.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepo userRepo;
    public UserService(UserRepo userrepo) {
        this.userRepo = userrepo;
    }

    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

}
