package telemetry;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TelemetryApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private Map<String, Text> textMap = new LinkedHashMap<>();
    private Map<String, String> associations = new LinkedHashMap<>();

    private void initializeAssociations() {
        associations.put("IAS km/h", "IAS");
        associations.put("TAS km/h", "SPD");
        associations.put("H m", "Altitude");
        associations.put("power 1 hp", "Power 1");
        associations.put("thrust 1 kgs", "Thrust 1");
    }

    private Text createText(String name) {
        Text text = new Text();
        text.setFont(Font.font(20));
        text.setFill(Color.BLACK);
        text.setMouseTransparent(true);
        text.setText(name);
        return text;
    }

    @Override
    public void start(Stage primaryStage) {

        initializeAssociations();

        associations.values().forEach(name -> textMap.put(name, createText(name)));

        VBox root = new VBox();
        root.getChildren().addAll(textMap.values());

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        primaryStage.setScene(scene);

        primaryStage.setAlwaysOnTop(true);
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        primaryStage.setTitle("TelemetryApp");
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(200);
        primaryStage.setMaxHeight(400);
        primaryStage.setMaxWidth(200);
        primaryStage.setX(200);
        primaryStage.setY(200);
        primaryStage.show();

        Task<Void> task = new Task<Void>() {

            @Override
            public Void call() throws Exception {
                URL url = new URL("http://localhost:8111/state");

                while (true) {
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    con.disconnect();
                    Map<String, String> parsedData = parse(content.toString());

                    Platform.runLater(() ->
                        associations.forEach((key, value) ->
                            textMap.get(value).setText(value + ": " + parsedData.get(key) + " " + key.substring(key.lastIndexOf(' ')))));
                    Thread.sleep(10);
                }
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    private Map<String, String> parse(String content) {
        content = content.replaceAll("}", "");
        content = content.replaceAll("\\{", "");
        content = content.replaceAll(", ", " ");

        Map<String, String> data = new HashMap<>();

        for (String keyAndValue : content.split(",")) {
            keyAndValue = keyAndValue.replaceAll("\\\"", "");
            String[] split = keyAndValue.split(": ");
            data.put(split[0], split[1]);
        }
        return data;
    }
}