package com.if7100.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.if7100.entity.Organizacion;

@Repository
public interface OrganizacionRepository extends JpaRepository<Organizacion, Integer>{
    
}
