package es.jbp.sinopticos.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import es.jbp.expresiones.Valor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReglaSinoptico {
    private String path;
    private String idElemento;
    private String atributo;
    private String expresion;

    @JsonIgnore
    private Object valor;
}
