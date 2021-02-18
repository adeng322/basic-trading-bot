# Bux Basic Trading Bot
## Architecture Design
![Screenshot](design.png)

## Run
You can run this maven project on any IDE
or from command line

    mvn clean install
    mvn exec:java "-Dexec.mainClass=TradingBot"

## Input/Output
The bot needs 4 inputs: productId, buyPrice, lowerLimit and higherLimit. These four inputs are defined in *resources/application.properties* file.
In order for you to visualize what have happened, I print out everything
on the console, including response to show a successful buy and sell order.
For example: with inputs

    productId=sb26493 buy.price=13945 upper.limit=13950 lower.limit=13940 
The program would give following output:

    Connected to server
    Current price:13947.0
    Current price:13949.0
    Current price:13948.0
    ......
    Current price:13945.0
    Bought at: 13945.0
    {"id":"a54f15d5-7900-4b55-88c8-209078ed4c35","positionId":"aea505b3-...
    Current price:13944.5
    Current price:13944.5
    Current price:13944.5    
    ......
    Current price:13938.0
    Sold at: 13938.0
    {"id":"ccd3e117-5d86-4f97-be29-bf000d3f9263","positionId":"aea505b3-...
    Closing a WebSocket due to Disconnected

Worth to notice: when the bot starts, if the current price is lower than the lower limit price,
the bot will printout a message and terminate the connection.
    
    Connected to server
    Current price: 13920.0
    The current price is lower than the lower-limit price. The bot terminates.
    Closing a WebSocket due to Disconnected

Due to time limit, I have not handled any invalid inputs, so please make sure inputs are valid.

## Test
I have unit tested most of the logic functionalities that is not related to external library.
For example, in the **tradeService** class, there is a method called *doPostRequest()* which just simply call a httprequest
to make the buy order. I would not test it because I assume it would work. Also, I do not want to make a real request in unit resting.

## Workarounds
There are things I could not do but definitely want to do if I have more time:
1. Logging
2. Error handling

I would love to discuss them with you. I have enjoyed the assignment. I tried to stick with
K.I.S.S(keep it simple and stupid) and D.R.Y(do not repeat yourself) principles in both of my design and code, mainly because it is a
basic trading bot. Many thanks to you for reading this!