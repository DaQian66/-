package com.attendance.repository;
import com.attendance.entity.RoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface RoleMenuRepository extends JpaRepository<RoleMenu, Integer> {
    List<RoleMenu> findByRoleId(Integer roleId);

    @Modifying
    @Transactional
    void deleteByRoleId(Integer roleId);
}
