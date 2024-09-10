package com.if7100.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ta_identidadesgeneros")
public class IdentidadGenero {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer Id;
	@Column(name = "nombre", nullable = false)
	private String nombre;
	@Column(name = "descripcion", nullable = false)
	private String descripcion;
	@Column(name = "codigo_pais", nullable = false)
	private String codigo_pais;

	public IdentidadGenero() {
		// TODO Auto-generated constructor stub
	}

	public IdentidadGenero(String nombre,String descripcion, String codigo_pais) {
		super();
		this.nombre=nombre;
		this.descripcion=descripcion;
		this.codigo_pais=codigo_pais;
	}
	public Integer getId() {
		return Id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getCodigoPais() {
		return codigo_pais;
	}

	public void setCodigoPais(String codigo_pais) {
		this.codigo_pais = codigo_pais;
	}

	public void setId(Integer id) {
		Id = id;
	}

	

}


