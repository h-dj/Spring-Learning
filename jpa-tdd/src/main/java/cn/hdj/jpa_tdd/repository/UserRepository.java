package cn.hdj.jpa_tdd.repository;

import cn.hdj.jpa_tdd.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}