package es.jbp.sinopticos.expresiones;

import es.jbp.expresiones.Funcion;
import es.jbp.expresiones.Valor;

import java.util.List;
import java.util.Optional;

public class FuncionCondicional implements Funcion {

    @Override
    public Valor evaluar(List<Valor> listaParametros) {
        Valor condicion = listaParametros.get(0);
        Valor valorTrue = listaParametros.get(1);
        Valor valorFalse = listaParametros.get(2);
        return Optional.ofNullable(condicion)
                .map(c -> c.toBoolean() ? valorTrue : valorFalse)
                .orElse(valorFalse);
    }

    @Override
    public int getNumeroParametros() {
        return 3;
    }
}
