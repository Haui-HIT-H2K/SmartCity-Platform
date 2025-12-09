package com.smartcity.config;

import com.smartcity.model.Role;
import com.smartcity.model.User;
import com.smartcity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if admin exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", encoder.encode("admin123"));
            admin.setRoles(new HashSet<>(Collections.singletonList(Role.ROLE_ADMIN)));
            userRepository.save(admin);
            System.out.println("Default admin user created: admin / admin123");
        }
    }
}
