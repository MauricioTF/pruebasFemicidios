package com.if7100.controller;

import com.if7100.entity.*;
import com.if7100.service.*;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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

import com.if7100.repository.UsuarioRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ImputadoController {
	


 private ImputadoService imputadoService;

 @Autowired
 private PaisesService paisesService;
 @Autowired
 private UsuarioRepository usuarioRepository;


 private UsuarioService usuarioService;

//instancias para control de acceso
 private Perfil perfil;
 private PerfilService perfilService;
//instancias para control de bitacora
private BitacoraService bitacoraService;
private Usuario usuario;

private OrientacionSexualService orientacionSexualService;

private OrientacionSexual orientacionSexual;
private IdentidadGeneroService identidadGeneroService;

private IdentidadGenero identidadGenero;

private NivelEducativo nivelEducativo;

private NivelEducativoService nivelEducativoService;

private SituacionJuridica situacionJuridica;

private SituacionJuridicaService situacionJuridicaService;

private Organismo organismo;

private OrganismoService organismoService;

 
 public ImputadoController (BitacoraService bitacoraService, PaisesService paisesService,
ImputadoService imputadoService, PerfilService perfilService, UsuarioRepository usuarioRepository,
							IdentidadGeneroService identidadGeneroService,OrientacionSexualService orientacionSexualService,
							NivelEducativoService nivelEducativoService,SituacionJuridicaService situacionJuridicaService,
							OrganismoService organismoService) {
	 super();
	 this.imputadoService=imputadoService;
	 this.paisesService = paisesService;
	 this.perfilService = perfilService;
     this.usuarioRepository = usuarioRepository;
     this.bitacoraService= bitacoraService;
	 this.identidadGeneroService= identidadGeneroService;
	 this.orientacionSexualService= orientacionSexualService;
	 this.nivelEducativoService=nivelEducativoService;
	 this.situacionJuridicaService=situacionJuridicaService;
	 this.organismoService=organismoService;
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
 
 @GetMapping("/imputados")
 public String ListImputados(Model model) {

	 return "redirect:/imputado/1";
 }


 @GetMapping("/imputado/{pg}")
 public String listImputado(Model model,@PathVariable Integer pg){
	this.validarPerfil();

	 // Obtener el código de país del usuario logueado
	 	Integer codigoPaisUsuarioLogueado = this.usuario.getCodigoPais();
		
	  // Filtrar imputados por el código de país del usuario logueado
	  List<Imputado> imputadosFiltrados = imputadoService.findByCodigoPais(codigoPaisUsuarioLogueado);


	  int numeroTotalElementos = imputadosFiltrados.size();

	  Pageable pageable = initPages(pg, 5, numeroTotalElementos);
	  
	  int tamanoPagina = pageable.getPageSize();
      int numeroPagina = pageable.getPageNumber();

	  List<Imputado> imputadosPaginados = imputadosFiltrados.stream()
                    .skip((long) numeroPagina * tamanoPagina)
                    .limit(tamanoPagina)
                    .collect(Collectors.toList());

					List<Integer> nPaginas = IntStream.rangeClosed(1, (int) Math.ceil((double) numeroTotalElementos / tamanoPagina))
                    .boxed()
                    .toList();

	  // Enviar datos al modelo
	  model.addAttribute("PaginaActual", pg);
	  model.addAttribute("nPaginas", nPaginas);
	  model.addAttribute("imputados", imputadosPaginados);

	 return "imputados/imputados";
 }

 
 @GetMapping("/imputados/new")
 public String CreateUsuarioForm(Model model) {

	 try {
			this.validarPerfil();
			if(!this.perfil.getCVRol().equals("Consulta")) {
//				String descripcion="Elimino en XXX/Crea en XXX/ Actualiza en XXX";
//				Bitacora bitacora = new Bitacora(this.usuario.getCI_Id(), this.usuario.getCVNombre(),this.perfil.getCVRol(),descripcion);
//				bitacoraService.saveBitacora(bitacora);
				model.addAttribute("paises", paisesService.getAllPaises());
				model.addAttribute("orientacionSexual",orientacionSexualService.getAllOrientacionesSexuales());
				model.addAttribute("identidadGenero",identidadGeneroService.getAllIdentidadGenero());
				model.addAttribute("nivelEducativo",nivelEducativoService.getAllNivelEducativo());
				model.addAttribute("situacionJuridica",situacionJuridicaService.getAllSituacionJuridica());
				model.addAttribute("listaOrganismo",organismoService.getAllOrganismos());
				model.addAttribute("imputado",new Imputado());

				// Obtener lista de países y enviarla al modelo
				List<Paises> paises = paisesService.getAllPaises();
				model.addAttribute("paises", paises);

				return "imputados/create_imputado";

			}else {
				return "SinAcceso";
			}
			
		}catch (Exception e) {
			return "SinAcceso";
		}
 }
 
 @PostMapping("/imputados")
 public String SaveImputado(@ModelAttribute("imputado") Imputado imputado, Model model) {
	 try {
		 imputadoService.saveImputado(imputado);
		 bitacoraService.saveBitacora(new Bitacora(this.usuario.getCI_Id(),
				 this.usuario.getCVNombre(),this.perfil.getCVRol(),"Crea en Imputado"));
		 return "redirect:/imputados";
	 }catch (DataIntegrityViolationException e){
		 String mensaje = "No se puede guardar el imputado debido a un error de integridad de datos.";
		 model.addAttribute("error_message", mensaje);
		 model.addAttribute("error", true);
		 return CreateUsuarioForm(model);
	 }
 }
 
 @GetMapping("/imputados/{id}")
 public String deleteUsuario(@PathVariable int id) {
	 
	 try {
			this.validarPerfil();
			if(!this.perfil.getCVRol().equals("Consulta")) {
				

				imputadoService.deleteImputadoById(id);
				bitacoraService.saveBitacora(new Bitacora(this.usuario.getCI_Id(),
						this.usuario.getCVNombre(),this.perfil.getCVRol(),"Eliminó en Imputado"));
				 return "redirect:/imputados";
			}else {
				return "SinAcceso";
			}
			
		}catch (Exception e) {
			return "SinAcceso";
		}
 }
 
 @GetMapping("/imputados/edit/{id}")
 public String editImputadosForm(HttpServletResponse response, Model model,@PathVariable int id) {
	 
	 try {
			this.validarPerfil();
			    // Evitar que el navegador cachee la página de edición
				response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
				response.setHeader("Pragma", "no-cache");
				response.setHeader("Expires", "0");

			if(!this.perfil.getCVRol().equals("Consulta")) {

				List<Paises> paises = paisesService.getAllPaises();  // Obtiene la lista de países
				model.addAttribute("paises", paises);  // Envía la lista de países al modelo

				model.addAttribute("paises", paisesService.getAllPaises());
				model.addAttribute("orientacionSexual",orientacionSexualService.getAllOrientacionesSexuales());
				model.addAttribute("identidadGenero",identidadGeneroService.getAllIdentidadGenero());
				model.addAttribute("nivelEducativo",nivelEducativoService.getAllNivelEducativo());
				model.addAttribute("situacionJuridica",situacionJuridicaService.getAllSituacionJuridica());
				model.addAttribute("listaOrganismo",organismoService.getAllOrganismos());
				model.addAttribute("imputado", imputadoService.getImputadoById(id));

				return "imputados/edit_imputado";
			}else {
				return "SinAcceso";
			}
			
		}catch (Exception e) {
			return "SinAcceso";
		}
	 
 }
 
 @PostMapping("/imputados/{id}")
 public String updateUsuario(@PathVariable int id, @ModelAttribute("imputado") Imputado imputado, Model model) {
	 Imputado existingImputado=imputadoService.getImputadoById(id);

	 existingImputado.setCodigoPais(imputado.getCodigoPais());//actualiza codigo pais
	 existingImputado.setCVDni(imputado.getCVDni());
	 existingImputado.setCVGenero(imputado.getCVGenero());
	 existingImputado.setCVNombre(imputado.getCVNombre());
	 existingImputado.setCVOrientacionSexual(imputado.getCVOrientacionSexual());
	 existingImputado.setCVSexo(imputado.getCVSexo());
	 existingImputado.setCVLugarNacimiento(imputado.getCVLugarNacimiento());
	 existingImputado.setCIEdad(imputado.getCIEdad());
	 existingImputado.setCVNacionalidad(imputado.getCVNacionalidad());
	 existingImputado.setCVAntecedentes(imputado.getCVAntecedentes());
	 existingImputado.setCVEducacion(imputado.getCVEducacion());
	 existingImputado.setCVEstadoConyugal(imputado.getCVEstadoConyugal());
	 existingImputado.setCVLugarResidencia(imputado.getCVLugarResidencia());
	 existingImputado.setCVCondicionMigratoria(imputado.getCVCondicionMigratoria());
	 existingImputado.setCVEtnia(imputado.getCVEtnia());
	 existingImputado.setCVGenerador(imputado.getCVGenerador());
	 existingImputado.setCVOcupacion(imputado.getCVOcupacion());
	 existingImputado.setCVSituacionJuridica(imputado.getCVSituacionJuridica());
	 existingImputado.setCVPertenenciaFuerzaSeguridad(imputado.getCVPertenenciaFuerzaSeguridad());
	 existingImputado.setCVPermisoPortacionArmas(imputado.getCVPermisoPortacionArmas());
	 existingImputado.setCVSuicidio(imputado.getCVSuicidio());
	 existingImputado.setCVDomicilio(imputado.getCVDomicilio());
	 
	 imputadoService.updateImputado(existingImputado);
	 bitacoraService.saveBitacora(new Bitacora(this.usuario.getCI_Id(),
			 this.usuario.getCVNombre(),this.perfil.getCVRol(),"Actualizó en Imputado"));
	 return "redirect:/imputados";
 }
 
 
 
 
}
