package com.adaucu.demo.api;

import com.adaucu.demo.precios.PoliticaNoEncontradaException;
import com.adaucu.demo.seguridad.CredencialesInvalidasException;
import com.adaucu.demo.seguridad.NoAutenticadoException;
import com.adaucu.demo.ventas.AsientoEnConflicto;
import com.adaucu.demo.ventas.ConflictoDeEstadoException;
import com.adaucu.demo.ventas.LimiteExcedidoException;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorGlobalDeErrores {

    private static final String BASE_TIPO_ERROR = "https://adaucu/errores/";

    /**
     * RNF-01: aca se comunica el rechazo detectado por la tactica de
     * timestamp. El cuerpo lleva el detalle de que asientos cambiaron de
     * estado, para que el cliente pueda mostrarle al usuario exactamente que
     * paso con su pedido.
     */
    @ExceptionHandler(ConflictoDeEstadoException.class)
    public ProblemDetail manejarConflictoDeEstado(ConflictoDeEstadoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Otro usuario tomo uno o mas asientos mientras se procesaba la solicitud.");
        problema.setTitle("Reserva rechazada por conflicto de estado");
        problema.setType(URI.create(BASE_TIPO_ERROR + "conflicto-de-estado"));
        problema.setProperty("codigo", "CONFLICTO_DE_ESTADO");
        problema.setProperty("reservaId", ex.getReservaId());
        List<AsientoEnConflicto> conflictos = ex.getAsientosEnConflicto();
        problema.setProperty("asientosEnConflicto", conflictos);
        return problema;
    }

    @ExceptionHandler(LimiteExcedidoException.class)
    public ProblemDetail manejarLimiteExcedido(LimiteExcedidoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problema.setTitle("Limite operativo excedido");
        problema.setType(URI.create(BASE_TIPO_ERROR + "limite-excedido"));
        problema.setProperty("codigo", "LIMITE_EXCEDIDO");
        return problema;
    }

    @ExceptionHandler(EntidadNoEncontradaException.class)
    public ProblemDetail manejarNoEncontrada(EntidadNoEncontradaException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problema.setTitle("Recurso no encontrado");
        problema.setProperty("codigo", "NO_ENCONTRADO");
        return problema;
    }

    @ExceptionHandler(PoliticaNoEncontradaException.class)
    public ProblemDetail manejarPoliticaNoEncontrada(PoliticaNoEncontradaException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problema.setTitle("Politica de precio invalida");
        problema.setProperty("codigo", "POLITICA_NO_ENCONTRADA");
        return problema;
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ProblemDetail manejarAccesoDenegado(AccesoDenegadoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problema.setTitle("Acceso denegado");
        problema.setProperty("codigo", "ACCESO_DENEGADO");
        return problema;
    }

    @ExceptionHandler(NoAutenticadoException.class)
    public ProblemDetail manejarNoAutenticado(NoAutenticadoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problema.setTitle("Autenticacion requerida");
        problema.setProperty("codigo", "NO_AUTENTICADO");
        return problema;
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ProblemDetail manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problema.setTitle("Credenciales invalidas");
        problema.setProperty("codigo", "CREDENCIALES_INVALIDAS");
        return problema;
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ProblemDetail manejarSolicitudInvalida(Exception ex) {
        String detalle = (ex instanceof MethodArgumentNotValidException manve)
                ? manve.getBindingResult().getFieldErrors().stream()
                        .map(err -> err.getField() + ": " + err.getDefaultMessage())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("Solicitud invalida")
                : ex.getMessage();

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalle);
        problema.setTitle("Solicitud invalida");
        problema.setProperty("codigo", "SOLICITUD_INVALIDA");
        return problema;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail manejarEstadoInvalido(IllegalStateException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problema.setTitle("Operacion no valida para el estado actual");
        problema.setProperty("codigo", "ESTADO_INVALIDO");
        return problema;
    }
}
