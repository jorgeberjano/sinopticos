package es.jbp.sinopticos.expresiones;

import es.jbp.comun.utiles.conversion.Conversion;
import es.jbp.comun.utiles.reflexion.Reflexion;
import es.jbp.expresiones.Valor;
import es.jbp.expresiones.Variable;
import es.jbp.sinopticos.servicios.Estado;

public class VariableSinoptico implements Variable {
    private String identificadorAtributo;
    private Estado estado;

    public VariableSinoptico(String identificadorAtributo, Estado estado) {
        this.identificadorAtributo = identificadorAtributo;
        this.estado = estado;
    }

    public Valor convertirAValor(Object valorObjeto) {
        if (valorObjeto instanceof Integer || valorObjeto instanceof Long) {
            return new Valor(Conversion.toLong(valorObjeto));
        } else if (valorObjeto instanceof Float || valorObjeto instanceof Double) {
            return new Valor(Conversion.toDouble(valorObjeto));
        } else if (valorObjeto instanceof Boolean) {
            return new Valor(Conversion.toBoolean(valorObjeto));
        } else {
            return new Valor(Conversion.toString(valorObjeto));
        }
    }

    @Override
    public Valor getValor() {
        return convertirAValor(Reflexion.obtenerValorAtributo(estado.getBean(), identificadorAtributo));
    }
}
