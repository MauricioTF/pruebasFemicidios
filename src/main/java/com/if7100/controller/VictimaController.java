/**
 * 
 */
package com.if7100.controller;

import com.if7100.entity.*;
import com.if7100.service.BitacoraService;
import com.if7100.service.IdentidadGeneroService;
import com.if7100.service.NivelEducativoService;
import com.if7100.service.OrganismoService;
import com.if7100.service.OrientacionSexualService;
import com.if7100.service.PaisesService;

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
import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Stream;

import com.if7100.service.PerfilService;

/**
 * @author Hadji
 *
 */


import com.if7100.service.VictimaService;
import com.if7100.repository.UsuarioRepository;

import java.util.List;
import java.util.stream.IntStream;

@Controller
public class VictimaController {
	
	 @Autowired
 private PaisesService paisesService;

	private VictimaService victimaService;
	
	//instancias para control de acceso
    private UsuarioRepository usuarioRepository;
    private Perfil perfil;
    private PerfilService perfilService;
  //instancias para control de bitacora
    private BitacoraService bitacoraService;
    private Usuario usuario;
    //Instancias para el control de organismo
    private Organismo organismo;
    private OrganismoService organismoService;
    //Instancias el control de Orientacion Sexual
    private OrientacionSexualService orientacionSexualService;
    private OrientacionSexual orientacionSexual;
  //Instancias el control de Genero
    private IdentidadGeneroService identidadGeneroService;
    private IdentidadGenero identidadGenero;
  //Instancias el control de Nivel educativo
    private NivelEducativo nivelEducativo;
    private NivelEducativoService nivelEducativoService;

	//Constructor con todos las instancias
	public VictimaController(VictimaService victimaService, UsuarioRepository usuarioRepository,
			PerfilService perfilService, BitacoraService bitacoraService, OrganismoService organismoService,
			OrientacionSexualService orientacionSexualService, IdentidadGeneroService identidadGeneroService,
			NivelEducativoService nivelEducativoService) {
		super();
		this.victimaService = victimaService;
		this.usuarioRepository = usuarioRepository;
		this.perfilService = perfilService;
		this.bitacoraService = bitacoraService;
		this.organismoService = organismoService;
		this.orientacionSexualService = orientacionSexualService;
		this.identidadGeneroService = identidadGeneroService;
		this.nivelEducativoService = nivelEducativoService;
	}


	
	private void validarPerfil() {
    	
		try {
			Usuario usuario=new Usuario();

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		    String username = authentication.getName();
		    this.usuario= new Usuario(usuarioRepository.findByCVCedula(username));

			this.perfil = new Perfil(perfilService.getPerfilById(usuarioRepository.findByCVCedula(username).getCIPerfil()));
			
		}catch (Exception e) {
			// TODO: handle exception
		}
	}

	private Pageable initPages(int pg, int paginasDeseadas, int numeroTotalElementos){
		int numeroPagina = pg-1;
		if (numeroTotalElementos < 10){
			paginasDeseadas = 1;
		}
		if (numeroTotalElementos < 1){
			numeroTotalElementos = 1;
		}
		int tamanoPagina = (int) Math.ceil(numeroTotalElementos / (double) paginasDeseadas);
		return PageRequest.of(numeroPagina, tamanoPagina);
	}

	@GetMapping("/victimas")
	public String listVictimas(Model model) {
		return "redirect:/victima/1";
	}

	@GetMapping("/victima/{pg}")
	public String listVictima(Model model, @PathVariable Integer pg){
		
		this.validarPerfil();

		// Obtener el código de país del usuario logueado
		Integer codigoPaisUsuarioLogueado = this.usuario.getCodigoPais();
		
		// Filtrar Victimas por el código de país del usuario logueado
		//List<Victima> victimasFiltradas = victimaService.findByCodigoPais(codigoPaisUsuarioLogueado);
        List<Victima> victimasFiltradas = victimaService.findVictimasByCodigoPaisHecho(codigoPaisUsuarioLogueado);


		int numeroTotalElementos = victimaService.getAllVictima().size();

		Pageable pageable = initPages(pg, 5, numeroTotalElementos);

		int tamanoPagina = pageable.getPageSize();
        int numeroPagina = pageable.getPageNumber();

		//Page<Victima> victimaPage = victimaService.getAllVictimaPage(pageable);

		/*List<Integer> nPaginas = IntStream.rangeClosed(1, victimaPage.getTotalPages())
				.boxed()
				.toList();*/

		List<Victima> victimasPaginados = victimasFiltradas.stream()
			.skip((long) numeroPagina * tamanoPagina)
			.limit(tamanoPagina)
			.collect(Collectors.toList());

		List<Integer> nPaginas = IntStream.rangeClosed(1, (int) Math.ceil((double) numeroTotalElementos / tamanoPagina))
			.boxed()
			.toList();

		model.addAttribute("PaginaActual", pg);
		model.addAttribute("nPaginas", nPaginas);
		model.addAttribute("victimas", victimasPaginados);
		model.addAttribute("identidadgenero", victimaService.getAllIdentidadGeneros());
		model.addAttribute("orientacionSexual", victimaService.getAllOrientacionSexuales());
		model.addAttribute("nivelEducativo", nivelEducativoService.getAllNivelEducativo());

		return "victimas/victima";
	}
	
	@GetMapping("/victimas/new")
	public String createVictimaForm (Model model) {
		
		try {
			this.validarPerfil();
			if(!this.perfil.getCVRol().equals("Consulta")) {
				
				Victima victima = new Victima();
				
				model.addAttribute("orientacionSexual",orientacionSexualService.getAllOrientacionesSexuales());
				model.addAttribute("identidadGenero",identidadGeneroService.getAllIdentidadGenero());
				model.addAttribute("nivelEducativo",nivelEducativoService.getAllNivelEducativo());
				model.addAttribute("listaOrganismo",organismoService.getAllOrganismos());
				model.addAttribute("victima", victima);
				model.addAttribute("nivelEducativo", nivelEducativoService.getAllNivelEducativo());

				// Obtener lista de países y enviarla al modelo
				//List<Paises> paises = paisesService.getAllPaises();
				//model.addAttribute("paises", paises);
				
				bitacoraService.saveBitacora(new Bitacora(this.usuario.getCI_Id(),
						this.usuario.getCVNombre(),this.perfil.getCVRol(),"Crea en Victima"));
				return "victimas/create_victima";
			}else {
				return "SinAcceso";
			}
			
		}catch (Exception e) {
			return "SinAcceso";
		}
	}
	
	
	@PostMapping("/victimas")
	public String saveVictima (@ModelAttribute("victima") Victima victima) {
		
		victimaService.saveVictima(victima);
		return "redirect:/victimas";
	}
	

	@GetMapping("/victimas/{Id}")
	public String deleteVictima (@PathVariable Integer Id) {
		
		try {
			this.validarPerfil();
			if(!this.perfil.getCVRol().equals("Consulta")) {
				
				String descripcion = "Elimino una victima";
				Bitacora bitacora = new Bitacora(this.usuario.getCI_Id(), this.usuario.getCVNombre(), descripcion, this.perfil.getCVRol());
				bitacoraService.saveBitacora(bitacora);
				
				victimaService.deleteVictimaById(Id);
				bitacoraService.saveBitacora(new Bitacora(this.usuario.getCI_Id(),
						this.usuario.getCVNombre(),this.perfil.getCVRol(),"Eliminó en Victima"));
				return "redirect:/victimas";
			}else {
				return "SinAcceso";
			}
			
		}catch (Exception e) {
			return "SinAcceso";
		}
	}
	
	
	@GetMapping("/victimas/edit/{id}")
	public String editVictimaForm (@PathVariable Integer id, Model model) {
		
		try {
			this.validarPerfil();
			if(!this.perfil.getCVRol().equals("Consulta")) {
				
				//List<Paises> paises = paisesService.getAllPaises();  // Obtiene la lista de países
				//model.addAttribute("paises", paises);  // Envía la lista de países al modelo

				//model.addAttribute("orientacionSexual",orientacionSexualService.getAllOrientacionesSexuales());
				model.addAttribute("identidadGenero",identidadGeneroService.getAllIdentidadGenero());
				model.addAttribute("nivelEducativo",nivelEducativoService.getAllNivelEducativo());
				model.addAttribute("listaOrganismo",organismoService.getAllOrganismos());
				model.addAttribute("victima", victimaService.getVictimaById(id));
				model.addAttribute("nivelEducativo", nivelEducativoService.getAllNivelEducativo());
				model.addAttribute("orientacionSexual", victimaService.getAllOrientacionSexuales());

				return "victimas/edit_victima";
			}else {
				return "SinAcceso";
			}
			
		}catch (Exception e) {
			return "SinAcceso";
		}	
	}
	
	
	@PostMapping("/victimas/{id}")
	public String updateVictima (@PathVariable Integer id, 
								 @ModelAttribute("victima") Victima victima,
								 Model model) {
		
		Victima existingVictima = victimaService.getVictimaById(id);
		//existingVictima.setCodigoPais(victima.getCodigoPais());//actualiza codigo pais
		existingVictima.setCI_Id(id);
		existingVictima.setCVDNI(victima.getCVDNI());
		existingVictima.setCVNombre(victima.getCVNombre());
		existingVictima.setCVApellidoPaterno(victima.getCVApellidoPaterno());
		existingVictima.setCVApellidoMaterno(victima.getCVApellidoMaterno());
		existingVictima.setCVEdad(victima.getCIEdad());
		existingVictima.setCVGenero(victima.getCVGenero());
		existingVictima.setCVLugarNac(victima.getCVLugarNac());
		existingVictima.setCVOrientaSex(victima.getCVOrientaSex());
		existingVictima.setCVNacionalidad(victima.getCVNacionalidad());
		existingVictima.setCIEducacion(victima.getCIEducacion());
		existingVictima.setCVOcupacion(victima.getCVOcupacion());
		existingVictima.setCVDomicilio(victima.getCVDomicilio());
		existingVictima.setCVLugarResidencia(victima.getCVLugarResidencia());
		existingVictima.setCVDiscapacidad(victima.getCVDiscapacidad());
		existingVictima.setCVCondicionMigratoria(victima.getCVCondicionMigratoria());
		existingVictima.setCVEtnia(victima.getCVEtnia());
		existingVictima.setCVMedidasProteccion(victima.getCVMedidasProteccion());
		existingVictima.setCVDenunciasPrevias(victima.getCVDenunciasPrevias());
		existingVictima.setCIHijos(victima.getCIHijos());
		existingVictima.setCVGenerador(victima.getCVGenerador());
		
		victimaService.updateVictima(existingVictima);
		
		bitacoraService.saveBitacora(new Bitacora(this.usuario.getCI_Id(),
				 this.usuario.getCVNombre(),this.perfil.getCVRol(),"Actualizó en Victima"));
		 
		return "redirect:/victimas";
		
	}
	
}
