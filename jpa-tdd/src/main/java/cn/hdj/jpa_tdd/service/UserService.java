package cn.hdj.jpa_tdd.service;

import cn.hdj.jpa_tdd.entity.User;
import cn.hdj.jpa_tdd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }
}