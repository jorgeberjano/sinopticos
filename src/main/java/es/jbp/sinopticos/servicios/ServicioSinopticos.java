package es.jbp.sinopticos.servicios;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.jbp.comun.utiles.recursos.GestorRecursos;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import es.jbp.comun.utiles.reflexion.Reflexion;
import es.jbp.expresiones.*;
import es.jbp.sinopticos.config.ConfiguracionSistema;
import es.jbp.sinopticos.config.ConfiguracionSinopticos;
import es.jbp.sinopticos.dto.ComandoSinopticoDto;
import es.jbp.sinopticos.dto.ReglaSinoptico;
import es.jbp.sinopticos.expresiones.FuncionCondicional;
import es.jbp.sinopticos.expresiones.VariableSinoptico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servicio para obtener los sinopticos
 * @author jberjano
 */
@Service
public class ServicioSinopticos {

    @Autowired
    private ServicioWebSocket servicioWebSocket;

    private CompiladorExpresiones compiladorExpresiones = new CompiladorExpresiones();

    // TODO: prever que pueda ser concurrente
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<ReglaSinoptico> reglas = new ArrayList<>();
    private final Map<ReglaSinoptico, NodoExpresion> mapaExpresiones = new HashMap<>();
    private final Map<String, Estado> estados = new HashMap<>();
    private final Map<String, VariableSinoptico> mapaVariables = new HashMap<>();


    private ObjectMapper mapper = new ObjectMapper();

    public void inicializar() {

        compiladorExpresiones.setFactoriaIdentificadores(new FactoriaIdentificadores() {
            FuncionCondicional funcionCondicional = new FuncionCondicional();
            @Override
            public Variable crearVariable(String nombre) {
                return buscarVariable(nombre);
            }

            @Override
            public Funcion crearFuncion(String nombre) {
                if ("condicional".equals(nombre)) {
                    return funcionCondicional;
                } else {
                    return null;
                }
            }

            @Override
            public Funcion crearOperador(String nombre) {
                return null;
            }
        });

        servicioWebSocket.setListenerConexion(sesion -> actualizarSesion(sesion));
    }

    private void actualizarSesion(SesionClienteWebSocket sesion) {
        for (ReglaSinoptico regla : reglas) {
            try {
                servicioWebSocket.enviarMensaje(sesion, regla.getPath(), crearComandoJson(regla));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private Variable buscarVariable(String nombreVariable) {
        VariableSinoptico variable = mapaVariables.get(nombreVariable);
        if (variable == null) {
            String[] nombreSeparado = nombreVariable.split("\\.", 2);
            String sistema = nombreSeparado[0];
            String identificadorAtributo = nombreSeparado[1];
            Estado estado = estados.get(sistema);
            variable = new VariableSinoptico(identificadorAtributo, estado);
            mapaVariables.put(nombreVariable, variable);
        }
        return variable;
    }

    public String obtenerSinoptico(String idSinoptico) throws IOException {
        return GestorRecursos.getInstancia().getRecursoTexto("/sinopticos/" + idSinoptico + ".svg");
    }

    // TODO: procesar cada cliente con una latencia distinta
    //  @Scheduled(fixedDelay = 2000)
    public void tareaPeriodica() {
        //System.out.println("Tarea periodica");
        clientes.forEach(this::procesar);
        reglas.forEach(this::evaluarRegla);
    }

    private void procesar(Cliente cliente) {
        Object beanEstado = cliente.pedirEstado().block();
        Estado estado = estados.get(cliente.getNombreSistema());
        if (estado == null) {
            estado = new Estado();
            estados.put(cliente.getNombreSistema(), estado);
        }
        estado.setBean(beanEstado);
    }

    private void evaluarRegla(ReglaSinoptico regla) {

        NodoExpresion expresion = mapaExpresiones.get(regla);
        if (expresion == null) {
            expresion = compiladorExpresiones.compilar(regla.getExpresion());
            mapaExpresiones.put(regla, expresion);
        }
        Valor valor = expresion.evaluar();
        if (Objects.equals(valor.getObject(), regla.getValor())) {
            return;
        }
        regla.setValor(valor.getObject());

        try {
            servicioWebSocket.enviarMensaje(regla.getPath(), crearComandoJson(regla));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String crearComandoJson(ReglaSinoptico regla) throws JsonProcessingException {
        ComandoSinopticoDto comando = ComandoSinopticoDto.builder()
                .idElemento(regla.getIdElemento())
                .atributo(regla.getAtributo())
                .valor(Optional.ofNullable(regla.getValor()).map(Object::toString).orElse(null))
                .build();

        return mapper.writeValueAsString(comando);
    }

    public void configurar(InputStream streamRecurso) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        inicializar();

        ConfiguracionSinopticos config = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .readValue(streamRecurso, ConfiguracionSinopticos.class);

        for (ConfiguracionSistema configuracionSistema : config.getSistemas()) {
            registrarCliente(configuracionSistema);
        }
        for (ReglaSinoptico regla : config.getReglas()) {
            registrarRegla(regla);
        }
    }

    private void registrarCliente(ConfiguracionSistema configuracionSistema) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String nombreClase = configuracionSistema.getClase();
        Class clazz = Class.forName(nombreClase);
        Object sistema = Reflexion.crearObjeto(clazz);
        if (sistema instanceof Cliente) {
            Cliente cliente = (Cliente) sistema;
            cliente.setConfiguracion(configuracionSistema.getConfiguracion());
            clientes.add(cliente);
        }
    }

    public void registrarRegla(ReglaSinoptico regla) {
        this.reglas.add(regla);
    }
}
