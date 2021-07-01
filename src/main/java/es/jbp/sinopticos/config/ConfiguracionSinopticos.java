package es.jbp.sinopticos.config;

import es.jbp.sinopticos.dto.ReglaSinoptico;
import lombok.Data;

import java.util.List;

@Data
public class ConfiguracionSinopticos {
    private List<ConfiguracionSistema> sistemas;
    private List<ReglaSinoptico> reglas;
}
