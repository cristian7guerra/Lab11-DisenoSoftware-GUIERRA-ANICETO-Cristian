package pe.edu.continental.teleatencion.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pe.edu.continental.teleatencion.aplicacion.puertos.salida.IPacienteRepository;
import pe.edu.continental.teleatencion.aplicacion.puertos.salida.IReniecPort;
import pe.edu.continental.teleatencion.aplicacion.servicios.ValidarIdentidadUseCase;
import pe.edu.continental.teleatencion.dominio.Paciente;
import pe.edu.continental.teleatencion.dominio.excepciones.DniNoEncontradoException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el Caso de Uso de Autenticación de DNI (HU-01)
 * Aplica la estructura AAA (Arrange-Act-Assert) y dobles de prueba (Mocks).
 */
class ValidarIdentidadUseCaseTest {

    // Dobles de prueba (Mocks) para aislar la infraestructura externa
    private IReniecPort reniecPortMock = mock(IReniecPort.class);
    private IPacienteRepository pacienteRepoMock = mock(IPacienteRepository.class);

    private ValidarIdentidadUseCase servicio;

    @BeforeEach
    void setUp() {
        // Inyección de dependencias por constructor
        servicio = new ValidarIdentidadUseCase(reniecPortMock, pacienteRepoMock);
    }

    @Test
    @DisplayName("CP-01: Debe validar exitosamente el DNI y guardar el paciente registrado")
    void debeValidarDniExitosamente() {
        // ARRANGE (Preparar)
        String dniInput = "76543210";
        Paciente pacienteReniec = new Paciente(dniInput, "Juan", "Pérez Soler");

        // when/thenReturn fija la respuesta predefinida del doble de prueba
        when(reniecPortMock.consultarDni(dniInput)).thenReturn(pacienteReniec);
        when(pacienteRepoMock.guardar(any(Paciente.class))).thenReturn(pacienteReniec);

        // ACT (Ejecutar)
        Paciente resultado = servicio.ejecutar(dniInput);

        // ASSERT (Verificar)
        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombres());
        assertEquals("Pérez Soler", resultado.getApellidos());

        // verify comprueba la interacción con los puertos de salida
        verify(reniecPortMock, times(1)).consultarDni(dniInput);
        verify(pacienteRepoMock, times(1)).guardar(any(Paciente.class));
    }

    @Test
    @DisplayName("CP-02: No guarda el paciente si el DNI no es encontrado en RENIEC")
    void noGuardaElPacienteSiDniNoExiste() {
        // ARRANGE
        String dniInexistente = "00000000";
        when(reniecPortMock.consultarDni(dniInexistente)).thenReturn(null);

        // ACT + ASSERT
        DniNoEncontradoException excepcion = assertThrows(
            DniNoEncontradoException.class,
            () -> servicio.ejecutar(dniInexistente)
        );

        assertEquals("DNI no encontrado en RENIEC, por favor verifique e intente nuevamente", excepcion.getMessage());

        // Confirmar con never() que jamás se llamó a la base de datos ante un fallo de validación
        verify(pacienteRepoMock, never()).guardar(any());
    }
}