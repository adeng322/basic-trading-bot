import eventlistener.OnFinishedListener;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TradeServiceTest {

    private TradeService tradeService;
    private OnFinishedListener onFinishedListener;
    /* Redirect System.out to buffer */
    private ByteArrayOutputStream byteArrayOutputStream;

    @BeforeEach
    public void setUp() {
        tradeService = new TradeService("sb26493", 14000, 13900, 14100);
        onFinishedListener = mock(OnFinishedListener.class);
        tradeService.setOnFinishedListener(onFinishedListener);
        byteArrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(byteArrayOutputStream));
    }

    @Test
    public void trade_StartWithCurrentPriceLowerThanLowLimit() throws IOException {
        assertFalse(tradeService.getIsBought());
        tradeService.trade(13500);
        byteArrayOutputStream.flush();
        verify(onFinishedListener, times(1)).onFinished();
        assertTrue(byteArrayOutputStream.toString().contains("The current price is lower than the lower-limit price. The bot terminates."));
    }

    @Test
    public void trade_OpenAPosition() throws IOException, JSONException {
        TradeService spyTradeService = spy(tradeService);
        assertFalse(spyTradeService.getIsBought());
        Mockito.doReturn(null).when(spyTradeService).doPostRequest();
        spyTradeService.trade(13999);
        byteArrayOutputStream.flush();
        verify(spyTradeService, times(1)).doPostRequest();
        verify(spyTradeService, times(0)).doDeleteRequest();
        assertTrue(byteArrayOutputStream.toString().contains("Bought at: 13999"));
    }

    @Test
    public void trade_ClosePositionWhenHigherLimitHit() throws IOException, JSONException {
        TradeService spyTradeService = spy(tradeService);
        assertFalse(spyTradeService.getIsBought());
        spyTradeService.setIsBought(true);
        assertTrue(spyTradeService.getIsBought());
        Mockito.doReturn(null).when(spyTradeService).doDeleteRequest();
        spyTradeService.trade(14105);
        byteArrayOutputStream.flush();
        verify(spyTradeService, times(1)).doDeleteRequest();
        verify(spyTradeService, times(0)).doPostRequest();
        assertTrue(byteArrayOutputStream.toString().contains("Sold at: 14105"));
    }
}
