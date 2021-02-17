import config.ConfigProperties;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Objects;

import eventlistener.OnFinishedListener;

/**
 * This is the class where buy and sell http request being called.
 */
public class TradeService {

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();
    private final String productId;
    private final double buyPrice;
    private final double lowSellPrice;
    private final double highSellPrice;
    private String positionId;
    private OnFinishedListener onFinishedListener;
    private boolean isBought = false;

    public TradeService(String productId, double buyPrice, double lowSellPrice, double highSellPrice) {
        this.productId = productId;
        this.buyPrice = buyPrice;
        this.lowSellPrice = lowSellPrice;
        this.highSellPrice = highSellPrice;
    }

    public void trade(double currentPrice) {
        if (!isBought) {
            if (currentPrice <= lowSellPrice) {
                System.out.println("The current price is lower than the lowSellPrice. The bot terminates.");
                this.onFinishedListener.onFinished();
            } else if (currentPrice <= buyPrice) {
                try {
                    System.out.println("Bought at: " + currentPrice);
                    doPostRequest();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (currentPrice <= lowSellPrice || currentPrice >= highSellPrice) {
                try {
                    System.out.println("Sold at: " + currentPrice);
                    doDeleteRequest();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private static Headers createHeaders() {
        Headers.Builder builder = new Headers.Builder();
        builder.add("Authorization", ConfigProperties.getProperty("authorization"))
                .add("Accept-Language", ConfigProperties.getProperty("accept-language"))
                .add("Content-Type", ConfigProperties.getProperty("content-type"))
                .add("Accept", ConfigProperties.getProperty("accept"));
        return builder.build();
    }

    public Response doPostRequest() throws IOException, JSONException {
        JSONObject investingAmount = new JSONObject();
        investingAmount.put("currency", "BUX")
                .put("decimals", 2)
                .put("amount", 10.00);
        JSONObject source = new JSONObject();
        source.put("sourceType", "OTHER");
        JSONObject openPositionJson = new JSONObject();
        openPositionJson.put("productId", this.productId)
                .put("investingAmount", investingAmount)
                .put("leverage", 2)
                .put("direction", "BUY")
                .put("source", source);

        RequestBody body = RequestBody.create(openPositionJson.toString(), JSON);
        Request request = new Request.Builder()
                .headers(createHeaders())
                .url(ConfigProperties.getProperty("buy.order.url"))
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        String responseBodyString = Objects.requireNonNull(response.body()).string();
        if (response.isSuccessful()) {
            System.out.println(responseBodyString);
            JSONObject responseBody = new JSONObject(responseBodyString);
            this.positionId = responseBody.get("positionId").toString();
            this.isBought = true;
        }
        return response;
    }

    public Response doDeleteRequest() throws IOException {
        Request request = new Request.Builder()
                .headers(createHeaders())
                .url(ConfigProperties.getProperty("sell.order.url")+ positionId)
                .delete()
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            System.out.println(Objects.requireNonNull(response.body()).string());
            onFinishedListener.onFinished();
        }
        return response;
    }

    public void setOnFinishedListener(OnFinishedListener onFinishedListener) {
        this.onFinishedListener = onFinishedListener;
    }

    public void setIsBought(boolean isBought) {
        this.isBought = isBought;
    }

    public boolean getIsBought() {
        return isBought;
    }
}
