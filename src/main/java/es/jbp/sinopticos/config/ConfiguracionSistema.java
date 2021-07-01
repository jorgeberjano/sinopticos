package es.jbp.sinopticos.config;

import lombok.Data;

import java.util.Map;

@Data
public class ConfiguracionSistema {
    private String clase;
    private Map<String, Object> configuracion;
}
