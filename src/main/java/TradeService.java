import appconfig.Property;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import eventlistener.OnFinishedListener;

public class TradeService {

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();
    private final String productId;
    private final double buyPrice;
    private final double lowSellPrice;
    private final double highSellPrice;
    private String positionId;
    public OnFinishedListener onFinishedListener;
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
                    doPostRequest(productId);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (currentPrice <= lowSellPrice || currentPrice >= highSellPrice) {
                try {
                    System.out.println("Sold at: " + currentPrice);
                    System.out.println(doDeleteRequest().body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private static Headers createHeaders() {
        Headers.Builder builder = new Headers.Builder();
        builder.add("Authorization", Property.getProperty("authorization"))
                .add("Accept-Language", Property.getProperty("accept-language"))
                .add("Content-Type", Property.getProperty("content-type"))
                .add("Accept", Property.getProperty("accept"));
        return builder.build();
    }

    public Response doPostRequest(String productId) throws IOException, JSONException {
        JSONObject investingAmount = new JSONObject();
        investingAmount.put("currency", "BUX")
                .put("decimals", 2)
                .put("amount", 10.00);
        JSONObject source = new JSONObject();
        source.put("sourceType", "OTHER");
        JSONObject openPositionJson = new JSONObject();
        openPositionJson.put("productId", productId)
                .put("investingAmount", investingAmount)
                .put("leverage", 2)
                .put("direction", "BUY")
                .put("source", source);

        RequestBody body = RequestBody.create(openPositionJson.toString(), JSON);
        Request request = new Request.Builder()
                .headers(createHeaders())
                .url(Property.getProperty("buy.order.url"))
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        String responseBodyString = response.body().string();
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
                .url(Property.getProperty("sell.order.url")+ positionId)
                .delete()
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            onFinishedListener.onFinished();
        }
        return response;
    }

    public void setOnCompleteListener(OnFinishedListener onFinishedListener) {
        this.onFinishedListener = onFinishedListener;
    }
}