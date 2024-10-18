package com.if7100.controller;

import com.if7100.entity.Hecho;
import com.if7100.entity.Paises;
import com.if7100.entity.Perfil;
import com.if7100.entity.ProcesoJudicial;
import com.if7100.entity.Usuario;
import com.if7100.entity.Victima;
import com.if7100.entity.Bitacora;
import com.if7100.repository.UsuarioRepository;
import com.if7100.service.*;
import com.itextpdf.io.IOException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

import java.util.stream.Collectors;

@Controller
public class HechoController {

    private HechoService hechoService;

    private PaisesService paisesService;
    private ModalidadService modalidadService;
    private TipoVictimaService tipoVictimaService;
    private TipoRelacionService tipoRelacionService;

    private VictimaService victimaService;

    private ProcesoJudicialService procesoJudicialService;

    private OrganismoService organismoService;

  //instancias para control de acceso
    private UsuarioRepository usuarioRepository;
    private Perfil perfil;
    private PerfilService perfilService;
  //instancias para control de bitacora
    private BitacoraService bitacoraService;
    private Usuario usuario;


//    public HechoController(HechoService hechoService) {
//        super();
//        this.hechoService = hechoService;
//    }

    public HechoController(HechoService hechoService,PaisesService paisesService, ModalidadService modalidadService,
                           TipoVictimaService tipoVictimaService, TipoRelacionService tipoRelacionService,
                           VictimaService victimaService, ProcesoJudicialService procesoJudicialService, OrganismoService organismoService, PerfilService perfilService, UsuarioRepository usuarioRepository,
                           BitacoraService bitacoraService)
{
        super();
        this.hechoService = hechoService;
        this.paisesService = paisesService;
        this.modalidadService = modalidadService;
        this.tipoVictimaService = tipoVictimaService;
        this.tipoRelacionService = tipoRelacionService;
        this.victimaService = victimaService;
        this.procesoJudicialService = procesoJudicialService;
        this.organismoService = organismoService;
        this.perfilService = perfilService;
        this.usuarioRepository = usuarioRepository;
        this.bitacoraService= bitacoraService;
    }


    //Para generar pdf
    @GetMapping("/hechos/pdf")
public void exportToPDF(HttpServletResponse response) throws IOException, java.io.IOException {
    response.setContentType("application/pdf");
    String headerKey = "Content-Disposition";
    String headerValue = "attachment; filename=hechos.pdf";
    response.setHeader(headerKey, headerValue);

    // Crear el documento PDF usando iText 7
    PdfWriter pdfWriter = new PdfWriter(response.getOutputStream());
    PdfDocument pdfDoc = new PdfDocument(pdfWriter);
    Document document = new Document(pdfDoc);

    // Agregar título o encabezado al documento
    document.add(new Paragraph("Lista de Hechos"));

    // Obtener la lista de hechos
    List<Hecho> hechos = hechoService.getAllHechos();

    // Recorrer los hechos y agregar la información al documento
    for (Hecho hecho : hechos) {
        document.add(new Paragraph("ID: " + hecho.getCI_Id()));
        document.add(new Paragraph("Tipo de Víctima: " + hecho.getCITipoVictima()));
        document.add(new Paragraph("País: " + hecho.getCodigoPais()));
        document.add(new Paragraph("Provincia: " + hecho.getCVProvincia()));
        document.add(new Paragraph("Canton: " + hecho.getCVCanton()));
        document.add(new Paragraph("Distrito: " + hecho.getCVDistrito()));
        document.add(new Paragraph("Fecha: " + hecho.getCDFecha()));
        document.add(new Paragraph("Observaciones: " + hecho.getCVDetalles()));
        document.add(new Paragraph("--------------------------------------------------"));
    }

    // Cerrar el documento
    document.close();
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

    @GetMapping("/hechos")
    public String listHechos(Model model){
        return "redirect:/hecho/1";
    }

    @GetMapping("/hecho/{pg}")
    public String listHecho(Model model, @PathVariable Integer pg){

        this.validarPerfil();
        
        Integer codigoPaisUsuario = this.usuario.getCodigoPais();
        List<Hecho> hechosFiltrados = hechoService.findByCodigoPais(codigoPaisUsuario);

        int numeroTotalElementos = hechosFiltrados.size();

        Pageable pageable = initPages(pg, 5, numeroTotalElementos);

        int tamanoPagina = pageable.getPageSize();
        int numeroPagina = pageable.getPageNumber();

        List<Hecho> hechosPaginados = hechosFiltrados.stream()
			.skip((long) numeroPagina * tamanoPagina)
			.limit(tamanoPagina)
			.collect(Collectors.toList());

		List<Integer> nPaginas = IntStream.rangeClosed(1, (int) Math.ceil((double) numeroTotalElementos / tamanoPagina))
			.boxed()
			.toList();

        model.addAttribute("PaginaActual", pg);
        model.addAttribute("nPaginas", nPaginas);
        model.addAttribute("hechos", hechosPaginados);
        model.addAttribute("paises", hechoService.getAllPaisesPage(pageable));
        model.addAttribute("modalidades", hechoService.getAllModalidadesPage(pageable));
        model.addAttribute("organismos", hechoService.getAllOrganismosPage(pageable));
        model.addAttribute("tipoVictimas", hechoService.getAllTipoVictimasPage(pageable));
        model.addAttribute("tipoRelaciones", hechoService.getAllTipoRelacionesPage(pageable));
        model.addAttribute("victimas", hechoService.getAllVictimasPage(pageable));
        model.addAttribute("procesosJudiciales", hechoService.getAllProcesosJudicialesPage(pageable));
        model.addAttribute("hechos", hechosPaginados);
        return "hechos/hechos";
    }

    @GetMapping("/hechos/new")
    public String createHechoForm(Model model){
    	
    	try {
			this.validarPerfil();
			if(!this.perfil.getCVRol().equals("Consulta")) {
				Hecho hecho = new Hecho();
		        model.addAttribute("hecho", hecho);
                modelAttributes(model);

                // Obtener lista de países y enviarla al modelo
				List<Paises> paises = paisesService.getAllPaises();
				model.addAttribute("paises", paises);

                return "hechos/create_hecho";
			}else {
				return "SinAcceso";
			}
			
		}catch (Exception e) {
			return "SinAcceso";
		}
    }

    private void modelAttributes(Model model) {
        model.addAttribute("modalidad", modalidadService.getAllModalidades());
        model.addAttribute("tipoVictima", tipoVictimaService.getAllTipoVictimas());
        model.addAttribute("tipoRelacion", tipoRelacionService.getAllTipoRelaciones());
        model.addAttribute("victima", victimaService.getAllVictima());
        model.addAttribute("ProcesoJudicial", procesoJudicialService.getAllProcesosJudiciales());
        model.addAttribute("organismo", organismoService.getAllOrganismos());
        model.addAttribute("paises", paisesService.getAllPaises());
    }

    @PostMapping("/hechos")
    public String saveHecho(@ModelAttribute("hecho") Hecho hecho, Model model){
        try {
            hechoService.saveHecho(hecho);
            
        	String descripcion="Creo en Hechos: " + hecho.getCI_Id();
            Bitacora bitacora = new Bitacora(this.usuario.getCI_Id(), this.usuario.getCVNombre(), this.perfil.getCVRol(), descripcion);
            bitacoraService.saveBitacora(bitacora);
            return "redirect:/hechos";
        } catch (DataIntegrityViolationException e){
            String mensaje = "No se puede guardar el hecho debido a un error de integridad de datos.";
            model.addAttribute("error_message", mensaje);
            model.addAttribute("error", true);
            return createHechoForm(model);
        }
    }

    @GetMapping("/hechos/{id}")
    public String deleteHecho(@PathVariable Integer id, Model model){
    	
    	try {
			this.validarPerfil();
			if(!this.perfil.getCVRol().equals("Consulta")) {
				
				try {
                    hechoService.deleteHechoById(id);
					String descripcion="Elimino en Hechos: " + id;
                    Bitacora bitacora = new Bitacora(this.usuario.getCI_Id(), this.usuario.getCVNombre(), this.perfil.getCVRol(), descripcion);
                    bitacoraService.saveBitacora(bitacora);
		        } catch (DataIntegrityViolationException e) {

		            String mensaje = "Error, No se puede eliminar un hecho si tiene un lugar registrado";
                    model.addAttribute("error_message", mensaje);
                    model.addAttribute("error", true);
                    return listHecho(model, 1);
		        }
		        return "redirect:/hechos";
			}else {
				return "SinAcceso";
			}
			
		}catch (Exception e) {
			return "SinAcceso";
		}
    }

    @GetMapping("/hechos/edit/{id}")
    public String editHechoForm(@PathVariable Integer id, Model model){
    	
    	try {
			this.validarPerfil();
			if(!this.perfil.getCVRol().equals("Consulta")) {
				

                model.addAttribute("victima", victimaService.getAllVictima());
                model.addAttribute("ProcesoJudicial", procesoJudicialService.getAllProcesosJudiciales());
        
                
                model.addAttribute("hecho", hechoService.getHechoById(id));
                modelAttributes(model);
                return "hechos/edit_hecho";
			}else {
				return "SinAcceso";
			}
			
		}catch (Exception e) {
			return "SinAcceso";
		}
    }

    @PostMapping("/hechos/{id}")
    public String updateHecho(@PathVariable Integer id, @ModelAttribute("hecho") Hecho hecho, Model model){
        try {
            Hecho existingHecho = hechoService.getHechoById(id);
            String descripcion="Actualizo en Hechos, de: " + existingHecho.getCI_Id() + " | a: " + id;
            existingHecho.setCI_Id(id);
            existingHecho.setCodigoPais(hecho.getCodigoPais());
            existingHecho.setCVProvincia(hecho.getCVProvincia());
            existingHecho.setCVCanton(hecho.getCVCanton());
            existingHecho.setCVDistrito(hecho.getCVDistrito());
            existingHecho.setCITipoVictima(hecho.getCITipoVictima());
            existingHecho.setCITipoRelacion(hecho.getCITipoRelacion());
            existingHecho.setCIModalidad(hecho.getCIModalidad());

            existingHecho.setProcesoJudicial(hecho.getProcesoJudicial());
            existingHecho.setVictima(hecho.getVictima());   
            //existingHecho.setCIIdVictima(hecho.getCIIdVictima());
            //existingHecho.setCIIdProceso(hecho.getCIIdProceso());
            existingHecho.setCIIdGenerador(hecho.getCIIdGenerador());
            existingHecho.setCVAgresionSexual(hecho.getCVAgresionSexual());
            existingHecho.setCVDenunciaPrevia(hecho.getCVDenunciaPrevia());
            existingHecho.setCDFecha(hecho.getCDFecha());
            existingHecho.setCVDetalles(hecho.getCVDetalles());

            hechoService.updateHecho(existingHecho);

            Bitacora bitacora = new Bitacora(this.usuario.getCI_Id(), this.usuario.getCVNombre(), this.perfil.getCVRol(), descripcion);
            bitacoraService.saveBitacora(bitacora);
            return "redirect:/hechos";
        } catch (DataIntegrityViolationException e){
            String mensaje = "No se puede guardar el hecho debido a un error de integridad de datos.";
            model.addAttribute("error_message", mensaje);
            model.addAttribute("error", true);
            return editHechoForm(id, model);
        }
    }







    


    
}
