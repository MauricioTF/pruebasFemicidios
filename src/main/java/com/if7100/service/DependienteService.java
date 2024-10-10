package com.if7100.service;

import java.util.List;

import com.if7100.entity.Dependiente;
import com.if7100.entity.DependienteVictima;
import com.if7100.entity.TipoRelacionFamiliar;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface DependienteService
{
	void saveDependienteVictima(DependienteVictima dependienteVictima);

	List<Dependiente> getAllDependiente();
	
	Dependiente saveDependiente(Dependiente dependiente);
	
	Dependiente getDependienteById(Integer Id);
	
	Dependiente updateDependiente(Dependiente dependiente);
	
	 List<TipoRelacionFamiliar> getAllTipoRelacionesFamilires();

	  List<TipoRelacionFamiliar> getAllTipoRelacionesFamiliaresPage(Pageable pageable);
	
	void deleteDependienteById(Integer Id);

	Dependiente getDependienteByCVDNI(String CVDNI);

    Page<Dependiente> getAllDependientePage(Pageable pageable);
}
