package es.jbp.sinopticos.servicios;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 *
 * @author jorge
 */
@Component
public class ServicioWebSocket extends TextWebSocketHandler {

    private final List<SesionClienteWebSocket> listaSesiones = new ArrayList<>();

    @Setter
    private ListenerConexion listenerConexion;

    public interface ListenerConexion {
        void conectado(SesionClienteWebSocket sesion);
    }

    @Override
    public synchronized void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Conexión establecida");
        SesionClienteWebSocket sesionCliente = new SesionClienteWebSocket(session);
        listaSesiones.add(sesionCliente);
        if (listenerConexion != null) {
            listenerConexion.conectado(sesionCliente);
        }
    }

    @Override
    public synchronized void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Conexión cerrada");
        Optional<SesionClienteWebSocket> optionalSesion = listaSesiones.stream().filter(s -> s.getSession() == session).findFirst();
        if (optionalSesion.isPresent()) {
            listaSesiones.remove(optionalSesion.get());
        }
    }

    public synchronized void enviarMensaje(String path, String payload) throws IOException {
        for (SesionClienteWebSocket sesion : listaSesiones) {
            sesion.procesarEvento(path, payload);
        }
    }

    public synchronized void enviarMensaje(SesionClienteWebSocket sesion, String path, String payload) throws IOException {
        sesion.procesarEvento(path, payload);
    }

//    private SesionClienteWebSocket buscarSesion(WebSocketSession session) {
//        return listaSesiones.stream()
//                .filter(s -> s.getSession() == session)
//                .findFirst()
//                .orElse(null);
//    }

//    private SesionClienteWebSocket buscarSesion(String path) {
//        if (StringUtils.isBlank(path)) {
//            return null;
//        }
//        for (SesionClienteWebSocket sesion : listaSesiones) {
//            if (path.equals(sesion.getPath())) {
//                return sesion;
//            }
//        }
//        return null;
//    }
}
