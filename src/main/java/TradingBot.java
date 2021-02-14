import appconfig.Property;
import org.json.JSONException;
import org.json.JSONObject;
import websocketclient.BUXWebsocketClient;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

public class TradingBot {

    private TradeService tradeService;
    private BUXWebsocketClient buxWebsocketClient;

    public TradingBot(TradeService tradeService, BUXWebsocketClient buxWebsocketClient) {
        this.tradeService = tradeService;
        this.buxWebsocketClient = buxWebsocketClient;
    }

    public void connectToWebsocket(BUXWebsocketClient BUXWebsocketClient) {
        WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
        try {
            webSocketContainer.connectToServer(BUXWebsocketClient, new URI(Property.getProperty("websocket.url")));
            //wait till websocket connection is open
            BUXWebsocketClient.getLatch().await();
        } catch (DeploymentException | IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run(String productId) throws InterruptedException, IOException {
        CountDownLatch stopProgramLatch = new CountDownLatch(1);

        tradeService.setOnCompleteListener(() -> stopProgramLatch.countDown());

        buxWebsocketClient.setOnConnectedListener(() -> buxWebsocketClient.sendMessage("subscribeTo", productId));

        buxWebsocketClient.setMessageListener(message -> {
            try {
                JSONObject jsonMessage = new JSONObject(message);
                if (jsonMessage.get("t").equals("trading.quote")) {
                    JSONObject jsonMessageBody = new JSONObject(jsonMessage.get("body").toString());
                    tradeService.trade(Double.parseDouble(jsonMessageBody.get("currentPrice").toString()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        connectToWebsocket(buxWebsocketClient);
        stopProgramLatch.await();
        buxWebsocketClient.sendMessage("unsubscribeFrom", productId);
        buxWebsocketClient.close();
    }

    public static void main(String[] args) {
        final String productId = "sb26493";
        final double buyPrice = 12150;
        final double upperLimit = buyPrice + 50;
        final double lowerLimit = buyPrice - 50;

        BUXWebsocketClient buxWebsocketClient = new BUXWebsocketClient();
        TradeService tradeService = new TradeService(productId, buyPrice, lowerLimit, upperLimit);

        TradingBot tradingBot = new TradingBot(tradeService, buxWebsocketClient);
        try {
            tradingBot.run(productId);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}