/**
 * 
 */
package com.if7100.controller;

import com.if7100.entity.*;
import com.if7100.service.BitacoraService;
import com.if7100.service.DependienteService;
import com.if7100.service.DependienteVictimaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.if7100.service.PerfilService;
import com.if7100.service.TipoRelacionFamiliarService;
import com.if7100.service.VictimaService;
import com.if7100.repository.DependienteVictimaRepository;

/**
 * @author Hadji
 *
 */

import com.if7100.repository.UsuarioRepository;

import java.util.List;
import java.util.stream.IntStream;

@Controller
public class DependienteController {
	@Autowired
	private DependienteVictimaService dependienteVictimaService;

	private DependienteService dependienteService;
	private DependienteVictimaRepository dependienteVictimaRepository;

	// instancias para control de acceso
	private UsuarioRepository usuarioRepository;
	private Perfil perfil;
	private PerfilService perfilService;
	// instancias para control de bitacora
	private BitacoraService bitacoraService;
	private VictimaService victimaService;

	private Usuario usuario;

	// Instancias el control de Nivel educativo
	private TipoRelacionFamiliarService tipoRelacionFamiliarService;

	// Constructor con todos las instancias
	public DependienteController(DependienteService dependienteService, UsuarioRepository usuarioRepository,
			PerfilService perfilService, BitacoraService bitacoraService,
			TipoRelacionFamiliarService tipoRelacionFamiliarService,
			VictimaService victimaservice) {
		super();
		this.dependienteService = dependienteService;
		this.usuarioRepository = usuarioRepository;
		this.perfilService = perfilService;
		this.bitacoraService = bitacoraService;
		this.tipoRelacionFamiliarService = tipoRelacionFamiliarService;
		this.victimaService = victimaservice;
	}

	private void validarPerfil() {

		try {
			Usuario usuario = new Usuario();

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String username = authentication.getName();
			this.usuario = new Usuario(usuarioRepository.findByCVCedula(username));

			this.perfil = new Perfil(
					perfilService.getPerfilById(usuarioRepository.findByCVCedula(username).getCIPerfil()));

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private Pageable initPages(int pg, int paginasDeseadas, int numeroTotalElementos) {
		int numeroPagina = pg - 1;
		if (numeroTotalElementos < 10) {
			paginasDeseadas = 1;
		}
		if (numeroTotalElementos < 1) {
			numeroTotalElementos = 1;
		}
		int tamanoPagina = (int) Math.ceil(numeroTotalElementos / (double) paginasDeseadas);
		return PageRequest.of(numeroPagina, tamanoPagina);
	}

	private void modelAttributes(Model model) {

		model.addAttribute("tipoRelacionFamiliar", tipoRelacionFamiliarService.getAllTipoRelacionFamiliar());
	}

	@GetMapping("/dependientes")
	public String listdependientes(Model model) {
		return "redirect:/dependiente/1";
	}

	@GetMapping("/dependiente/{pg}")
	public String listdependiente(Model model, @PathVariable Integer pg) {
		if (pg < 1) {
			return "redirect:/dependiente/1";
		}

		int numeroTotalElementos = dependienteService.getAllDependiente().size();

		Pageable pageable = initPages(pg, 5, numeroTotalElementos);

		Page<Dependiente> dependientePage = dependienteService.getAllDependientePage(pageable);

		List<Integer> nPaginas = IntStream.rangeClosed(1, dependientePage.getTotalPages())
				.boxed()
				.toList();

		model.addAttribute("PaginaActual", pg);
		model.addAttribute("nPaginas", nPaginas);
		model.addAttribute("dependiente", dependientePage.getContent());
		model.addAttribute("tipoRelacionesFamiliares", dependienteService.getAllTipoRelacionesFamiliaresPage(pageable));
		return "dependientes/dependiente";
	}

	@GetMapping("/dependientes/new")
	public String createdependienteForm(Model model) {

		try {
			this.validarPerfil();
			if (!this.perfil.getCVRol().equals("Consulta")) {

				Dependiente dependiente = new Dependiente();

				// Obtener todas las víctimas y agregarlas al modelo
				List<Victima> victimas = victimaService.getAllVictima();
				model.addAttribute("victimas", victimas);

				model.addAttribute("dependiente", dependiente);
				modelAttributes(model);
				bitacoraService.saveBitacora(new Bitacora(this.usuario.getCI_Id(),
						this.usuario.getCVNombre(), this.perfil.getCVRol(), "Crea en Dependiente"));
				return "dependientes/create_dependiente";
			} else {
				return "SinAcceso";
			}

		} catch (Exception e) {
			return "SinAcceso";
		}
	}

	@PostMapping("/dependientes")
	public String saveDependiente(@ModelAttribute("dependiente") Dependiente dependiente,
			@RequestParam("victima") Integer idVictima,
			Model model) {
				

		Victima victima = victimaService.getVictimaById(idVictima);

		if (victima != null) {

			dependienteService.saveDependiente(dependiente);

			DependienteVictima dependienteVictima = new DependienteVictima();
			dependienteVictima.setDependiente(dependiente);
			dependienteVictima.setVictima(victima);

			dependienteService.saveDependienteVictima(dependienteVictima);

			bitacoraService.saveBitacora(new Bitacora(this.usuario.getCI_Id(),
					this.usuario.getCVNombre(), this.perfil.getCVRol(), "Crea Dependiente con asociación a víctima"));

		} else {
			model.addAttribute("error", "La víctima seleccionada no es válida.");
			return "dependientes/create_dependiente";
		}

		return "redirect:/dependientes";
	}

	@GetMapping("/dependientes/{Id}")
	public String deleteDependiente(@PathVariable Integer Id) {

		try {
			this.validarPerfil();
			if (!this.perfil.getCVRol().equals("Consulta")) {

				String descripcion = "Elimino un dependiente(familiar)";
				Bitacora bitacora = new Bitacora(this.usuario.getCI_Id(), this.usuario.getCVNombre(), descripcion,
						this.perfil.getCVRol());
				bitacoraService.saveBitacora(bitacora);

				dependienteVictimaService.deleteByDependienteId(Id);

				dependienteService.deleteDependienteById(Id);

				bitacoraService.saveBitacora(new Bitacora(this.usuario.getCI_Id(),
						this.usuario.getCVNombre(), this.perfil.getCVRol(), "Eliminó en Denpendiente(familiar)"));
				return "redirect:/dependientes";
			} else {
				return "SinAcceso";
			}

		} catch (Exception e) {
			return "SinAcceso";
		}
	}

	@GetMapping("/dependientes/edit/{id}")
	public String editdependienteForm(@PathVariable Integer id, Model model) {

		try {
			this.validarPerfil();
			if (!this.perfil.getCVRol().equals("Consulta")) {

				model.addAttribute("dependiente", dependienteService.getDependienteById(id));
				
				 // Obtener todas las víctimas y agregarlas al modelo
				List<Victima> victimas = victimaService.getAllVictima();
				model.addAttribute("victimas", victimas);

				modelAttributes(model);
				return "dependientes/edit_dependiente";
			} else {
				return "SinAcceso";
			}

		} catch (Exception e) {
			e.printStackTrace();
    		System.out.println("Error al actualizar: " + e.getMessage());
			return "SinAcceso";
		}
	}

	@PostMapping("/dependientes/{id}")
	public String updatedependiente(@PathVariable Integer id,
			@ModelAttribute("dependiente") Dependiente dependiente,@RequestParam("victima") Integer idVictima,
			Model model) {

		Dependiente existingDependiente = dependienteService.getDependienteById(id);
		existingDependiente.setCI_Codigo(id);
		existingDependiente.setCVDNI(dependiente.getCVDNI());

		dependienteService.updateDependiente(existingDependiente);

		Victima victima = victimaService.getVictimaById(idVictima);

		DependienteVictima existingDependienteVictima = dependienteVictimaService.findBydependiente(existingDependiente).get(0);
		existingDependienteVictima.setDependiente(existingDependiente);
		existingDependienteVictima.setVictima(victima);

		dependienteService.updateDependienteVictima(existingDependienteVictima);

		bitacoraService.saveBitacora(new Bitacora(this.usuario.getCI_Id(),
				this.usuario.getCVNombre(), this.perfil.getCVRol(), "Actualizó en Dependiente"));

		return "redirect:/dependientes";

	}

}
