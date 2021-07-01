package es.jbp.sinopticos.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Especifica un comando sobre un atributo gráfico de un elemento del sinoptico
 * @author jorge
 */
@Data
@Builder
public class ComandoSinopticoDto {
    private String idElemento;
    private String atributo;
    private String valor;
}
