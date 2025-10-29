package com.bpe.platform.service;

import com.bpe.platform.entity.User;
import com.bpe.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .disabled(!user.isEnabled())
                .build();
    }
    
    public User createUser(String username, String password, String name, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("이미 존재하는 사용자명입니다.");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }
        
        User user = new User(username, passwordEncoder.encode(password), name, email);
        return userRepository.save(user);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User save(User user) {
        return userRepository.save(user);
    }
    
    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}

