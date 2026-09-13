package com.mess.app.repository;

import com.mess.app.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {

    Optional<Department> findByCode(String code);

    Optional<Department> findByCodeIgnoreCase(String code);

    List<Department> findByActiveTrue();

    List<Department> findByActiveTrueOrderByCodeAsc();

    List<Department> findAllByOrderByCodeAsc();

    boolean existsByCode(String code);

    boolean existsByCodeIgnoreCase(String code);
}