package websocketclient;

import eventlistener.OnConnectedListener;
import eventlistener.MessageListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javax.websocket.*;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * This is the class where websocket communication is handled.
 */
@ClientEndpoint(configurator = ClientSocketConfig.class)
public class BuxWebsocketClient {

    private CountDownLatch latch = new CountDownLatch(1);
    private Session session;
    private boolean isConnected = false;
    private MessageListener messageListener;
    private OnConnectedListener onConnectedListener;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        latch.countDown();
    }

    @OnMessage
    public void onText(String message) throws JSONException {
        JSONObject jsonMessage = new JSONObject(message);
        if (!isConnected) {
            if (jsonMessage.get("t").equals("connect.connected")) {
                isConnected = true;
                onConnectedListener.onConnected();
                System.out.println("Connected to server");
            } else {
                System.out.println("Unable to connect to server");
            }
        } else if (jsonMessage.get("t").equals("trading.quote")) {
            JSONObject jsonMessageBody = new JSONObject(jsonMessage.get("body").toString());
            System.out.println("Current price: " + Double.parseDouble(jsonMessageBody.get("currentPrice").toString()));
            messageListener.handleMessage(message);
        }
    }

    @OnClose
    public void onClose(CloseReason reason, Session session) {
        System.out.println("Closing a WebSocket due to " + reason.getReasonPhrase());
    }

    public void close() {
        try {
            this.session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void sendMessage(String instruction, String productId) {
        try {
            JSONArray jsonTradingProductArray = new JSONArray();
            jsonTradingProductArray.put("trading.product." + productId);
            JSONObject jsonSubscribeToObject = new JSONObject();
            jsonSubscribeToObject.put(instruction, jsonTradingProductArray);
            session.getBasicRemote().sendText(jsonSubscribeToObject.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void setOnConnectedListener(OnConnectedListener onConnectedListener) {
        this.onConnectedListener = onConnectedListener;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean getConnected() {
        return isConnected;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
