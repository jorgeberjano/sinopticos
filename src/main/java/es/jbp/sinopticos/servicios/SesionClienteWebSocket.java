package es.jbp.sinopticos.servicios;

import java.io.IOException;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 *
 * @author jorge
 */
public class SesionClienteWebSocket {

    private WebSocketSession session;
    private String path;

    public SesionClienteWebSocket(WebSocketSession session) {
        this.session = session;
        if (session.getUri() != null) {
            path = session.getUri().getPath();
        }        
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }

    public void procesarEvento(String path, String payload) throws IOException {
        if (this.path != null && this.path.equals("/" + path)) {
            session.sendMessage(new TextMessage(payload));
        }
    }
}
