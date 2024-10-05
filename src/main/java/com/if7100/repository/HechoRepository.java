package com.if7100.repository;

import com.if7100.entity.Hecho;
import com.if7100.entity.ProcesoJudicial;
import com.if7100.entity.Victima;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HechoRepository extends JpaRepository<Hecho, Integer> {

    //probando

    Hecho findByCITipoVictima(Integer CITipoVictima);

    Hecho findByCITipoRelacion(Integer CITipoRelacion);

    Hecho findByCIModalidad(Integer CIModalidad);

    //Hecho findByCIIdVictima(Integer CIIdVictima);
    List<Hecho> findByVictima(Victima victima);

    //Hecho findByCIIdProceso(Integer CIIdProceso);
    List<Hecho> findByProcesoJudicial(ProcesoJudicial procesoJudicial);

    Hecho findByCVAgresionSexual(String CVAgresionSexual);

    Hecho findByCVDenunciaPrevia(String CVDenunciaPrevia);

    Hecho findByCIIdGenerador(Integer CIIdGenerador);

    Hecho findByCDFecha(String CDFecha);







    //Muestra los hechos segun el codigo de pais del hecho
    List<Hecho> findByCodigoPais(Integer codigoPais);

    //Muestra los hechos segun el codigo de pais de la victima
    @Query("SELECT h FROM Hecho h WHERE h.victima.codigoPais = :codigoPais")
    List<Hecho> findHechosByVictimaCodigoPais(@Param("codigoPais") Integer codigoPais);

   
}
