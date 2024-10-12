package com.if7100.controller;

import com.if7100.entity.Hecho;
import com.if7100.repository.HechoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HechoControllerTest {

    @Autowired
    private HechoRepository hechoRepository;

    private Hecho hecho = new Hecho(2, 2, 2, 2, "SI", "SI", 2, "Limon","Limon","Limon", "2023-05-12T01:20"," ", 52);
    private Hecho consultado = new Hecho();

    @Test
    public void testUno() throws Exception{
        hechoRepository.save(hecho);
    }

    @Test
    public void testDos() throws Exception{
        consultado=hechoRepository.findByVictima();
        assertEquals(consultado.getVictima(),2);
        assertNotEquals(consultado.getVictima(),1);
    }

    @Test
    public void testTres() throws Exception{
        consultado=hechoRepository.findByCIIdVictima(2);
        consultado.setCodigoPais(1);
        hechoRepository.save(consultado);
        consultado=hechoRepository.findByCIIdVictima(2);
        assertNotEquals(consultado.getCodigoPais(),52);
    }

    @Test
    public void testCuatro() throws Exception{
        consultado = hechoRepository.findByCIIdVictima(2);
        hechoRepository.delete(consultado);
    }

}
