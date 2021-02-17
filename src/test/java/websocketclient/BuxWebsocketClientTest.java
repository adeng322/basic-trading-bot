package websocketclient;

import eventlistener.MessageListener;
import eventlistener.OnConnectedListener;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class BuxWebsocketClientTest {

    private BuxWebsocketClient buxWebsocketClient;
    private MessageListener messageListener;
    private OnConnectedListener onConnectedListener;
    private Session session;
    /* Redirect System.out to buffer */
    private ByteArrayOutputStream byteArrayOutputStream;

    @BeforeEach
    public void setUp() {
        buxWebsocketClient = new BuxWebsocketClient();
        session = mock(Session.class);
        messageListener = mock(MessageListener.class);
        onConnectedListener = mock(OnConnectedListener.class);
        buxWebsocketClient.setMessageListener(messageListener);
        buxWebsocketClient.setOnConnectedListener(onConnectedListener);
        buxWebsocketClient.setSession(session);
        byteArrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(byteArrayOutputStream));
    }

    @Test
    public void onText_ReceiveConnectedMessage() throws JSONException, IOException {
        assertFalse(buxWebsocketClient.getConnected());
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("t", "connect.connected");
        buxWebsocketClient.onText(jsonMessage.toString());
        byteArrayOutputStream.flush();
        verify(onConnectedListener, times(1)).onConnected();
        assertTrue(byteArrayOutputStream.toString().contains("Connected to server"));
    }

    @Test
    public void onText_ReceiveQuoteMessage() throws JSONException, IOException {
        buxWebsocketClient.setConnected(true);
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("t", "trading.quote")
                .put("body", new JSONObject().put("currentPrice", 14000));
        buxWebsocketClient.onText(jsonMessage.toString());
        byteArrayOutputStream.flush();
        verify(messageListener, times(1)).handleMessage(jsonMessage.toString());
        assertTrue(byteArrayOutputStream.toString().contains("Current price: 14000"));
    }

    @Test
    public void sendMessageTest() {
        String instruction = "subscribeTo";
        String productId = "sb26493";
        when(session.getBasicRemote()).thenReturn(mock(RemoteEndpoint.Basic.class));
        buxWebsocketClient.sendMessage(instruction, productId);
        verify(session, times(1)).getBasicRemote();
    }
}
