/*
 * Copyright 2025 Haui.HIT - H2K
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        if (!userRepository.existsByUsername("admin@vti.com.vn")) {
            User admin = new User("admin@vti.com.vn", encoder.encode("Admin@123"));
            admin.setRoles(new HashSet<>(Collections.singletonList(Role.ROLE_ADMIN)));
            userRepository.save(admin);
            System.out.println("Default admin user created: admin@vti.com.vn / Admin@123");
        }
    }
}
