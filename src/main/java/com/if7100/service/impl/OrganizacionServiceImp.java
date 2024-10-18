package com.if7100.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.if7100.entity.Organizacion;
import com.if7100.repository.OrganizacionRepository;
import com.if7100.service.OrganizacionService;

@Service
public class OrganizacionServiceImp implements OrganizacionService{
    
    private OrganizacionRepository organizacionRepository;

    public OrganizacionServiceImp(OrganizacionRepository organizacionRepository){
        super();
        this.organizacionRepository = organizacionRepository;
    }

    @Override
    public List<Organizacion> getAllOrganizacion() {
        return organizacionRepository.findAll();
    }

    @Override
    public Page<Organizacion> getAllOrganizacionPage(Pageable pageable) {
        return organizacionRepository.findAll(pageable);
    }

    @Override
    public Organizacion saveOrganizacion(Organizacion organizacion) {
        return organizacionRepository.save(organizacion);

    }

    @Override
    public Organizacion getOrganizacionById(Integer Ci_Codigo_Organizacion) {
        return organizacionRepository.findById(Ci_Codigo_Organizacion).get();

    }

    @Override
    public Organizacion updateOrganizacion(Organizacion organizacion) {
		return organizacionRepository.save(organizacion);
    }

    @Override
    public void deleteOrganizacionById(Integer Ci_Codigo_Organizacion) {
        organizacionRepository.deleteById(Ci_Codigo_Organizacion);

    }

}
