package es.jbp.sinopticos.servicios;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface Cliente {
    void setConfiguracion(Map<String, Object> configuracion);
    String getNombreSistema();
    Mono<Object> pedirEstado();


}
