package tfg.sircc_backend.service.impl;

import tfg.sircc_backend.dto.request.PersonaRequest;
import tfg.sircc_backend.dto.response.PersonaResponse;
import tfg.sircc_backend.model.Caso;
import tfg.sircc_backend.model.CasoPersona;
import tfg.sircc_backend.model.Persona;
import tfg.sircc_backend.repository.CasoPersonaRepository;
import tfg.sircc_backend.repository.CasoRepository;
import tfg.sircc_backend.repository.PersonaRepository;
import tfg.sircc_backend.service.PersonaService;
import tfg.sircc_backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonaServiceImpl implements PersonaService {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private CasoRepository casoRepository;

    @Autowired
    private CasoPersonaRepository casoPersonaRepository;

    @Override
    @Transactional
    public PersonaResponse crearPersona(PersonaRequest request) {
        if (request.getDocumento() != null && personaRepository.existsByDocumento(request.getDocumento())) {
            throw new IllegalArgumentException("Ya existe una persona con el documento: " + request.getDocumento());
        }

        Persona persona = new Persona();
        persona.setNombres(request.getNombres());
        persona.setApellidos(request.getApellidos());
        persona.setFechaNacimiento(request.getFechaNacimiento());
        persona.setDireccion(request.getDireccion());
        persona.setDocumento(request.getDocumento());
        persona.setAlias(request.getAlias());
        persona.setTelefono(request.getTelefono());
        persona.setTipoPersona(request.getTipoPersona());

        Persona savedPersona = personaRepository.save(persona);
        return convertToResponse(savedPersona);
    }

    @Override
    @Transactional
    public PersonaResponse actualizarPersona(Long id, PersonaRequest request) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con ID: " + id));

        persona.setNombres(request.getNombres());
        persona.setApellidos(request.getApellidos());
        persona.setFechaNacimiento(request.getFechaNacimiento());
        persona.setDireccion(request.getDireccion());
        if (request.getDocumento() != null) {
            persona.setDocumento(request.getDocumento());
        }
        persona.setAlias(request.getAlias());
        persona.setTelefono(request.getTelefono());
        persona.setTipoPersona(request.getTipoPersona());

        Persona updatedPersona = personaRepository.save(persona);
        return convertToResponse(updatedPersona);
    }

    @Override
    public PersonaResponse obtenerPersona(Long id) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con ID: " + id));
        return convertToResponse(persona);
    }

    @Override
    public PersonaResponse buscarPorDocumento(String documento) {
        Persona persona = personaRepository.findByDocumento(documento)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con documento: " + documento));
        return convertToResponse(persona);
    }

    @Override
    public Page<PersonaResponse> buscarPorNombre(String nombre, Pageable pageable) {
        Page<Persona> personas = personaRepository.findByNombresContainingOrApellidosContaining(nombre, nombre, pageable);
        return personas.map(this::convertToResponse);
    }

    @Override
    public Page<PersonaResponse> buscarPorAlias(String alias, Pageable pageable) {
        Page<Persona> personas = personaRepository.findByAliasContainingIgnoreCase(alias, pageable);
        return personas.map(this::convertToResponse);
    }

    @Override
    public Page<PersonaResponse> listarTodas(Pageable pageable) {
        Page<Persona> personas = personaRepository.findAll(pageable);
        return personas.map(this::convertToResponse);
    }

    @Override
    @Transactional
    public void eliminarPersona(Long id) {
        if (!personaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Persona no encontrada con ID: " + id);
        }
        personaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public PersonaResponse vincularACaso(Long personaId, Long casoId, String rolEnCaso) {
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada"));

        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado"));

        if (!casoPersonaRepository.existsByCasoIdAndPersonaId(casoId, personaId)) {
            CasoPersona casoPersona = new CasoPersona();
            casoPersona.setCaso(caso);
            casoPersona.setPersona(persona);
            casoPersona.setRolEnCaso(rolEnCaso);
            casoPersona.setFechaAsociacion(LocalDateTime.now());
            casoPersonaRepository.save(casoPersona);
        }

        return convertToResponse(persona);
    }

    private PersonaResponse convertToResponse(Persona persona) {
        PersonaResponse response = new PersonaResponse();
        response.setId(persona.getId());
        response.setNombres(persona.getNombres());
        response.setApellidos(persona.getApellidos());
        response.setNombreCompleto(persona.getNombres() + " " + persona.getApellidos());
        response.setFechaNacimiento(persona.getFechaNacimiento());
        response.setDireccion(persona.getDireccion());
        response.setDocumento(persona.getDocumento());
        response.setAlias(persona.getAlias());
        response.setTelefono(persona.getTelefono());
        response.setFotoUrl(persona.getFotoUrl());
        response.setTipoPersona(persona.getTipoPersona());
        response.setCreatedAt(persona.getCreatedAt());

        // Cargar antecedentes (casos en los que ha participado)
        if (persona.getCasos() != null && !persona.getCasos().isEmpty()) {
            List<PersonaResponse.AntecedenteResponse> antecedentes = persona.getCasos().stream()
                    .map(caso -> {
                        PersonaResponse.AntecedenteResponse ant = new PersonaResponse.AntecedenteResponse();
                        ant.setCasoId(caso.getId());
                        ant.setNumeroCaso(caso.getNumeroCaso());
                        ant.setNombreCaso(caso.getNombre());
                        ant.setFechaApertura(caso.getFechaApertura());

                        // ✅ CORRECTO: Buscar el rol en CasoPersona usando el repositorio
                        casoPersonaRepository.findByCasoIdAndPersonaId(caso.getId(), persona.getId())
                                .ifPresent(cp -> ant.setRolEnCaso(cp.getRolEnCaso()));

                        return ant;
                    })
                    .collect(Collectors.toList());
            response.setAntecedentes(antecedentes);
        }

        return response;
    }
}