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
        //associations.put("H m", "ALT  ");
        //associations.put("IAS km/h", "IAS  ");
        associations.put("TAS km/h", "SPD ");
        //associations.put("M", "MACH");
        associations.put("Vy m/s", "CLMB");
        //associations.put("Ny", "TURN");
        //associations.put("Wx deg/s", "ROLL");
        //associations.put("AoA deg", "AOA ");
        //associations.put("AoS deg", "AOS ");
        associations.put("power 1 hp", "PWR1");
        //associations.put("power 2 hp", "PWR2");
        associations.put("thrust 1 kgs", "THR1");
        //associations.put("thrust 2 kgs", "THR2");
        //associations.put("RPM 1", "RPM1");
        //associations.put("RPM 2", "RPM2");
        associations.put("pitch 1 deg", "PROP");
        associations.put("mixture 1 %", "MXTR");
        associations.put("radiator 1 %", "RDTR");
        //associations.put("Mfuel kg", "FUEL");
        //associations.put("Mfuel0 kg", "FLMX");
        //associations.put("manifold pressure 1 atm", "MNFP");
        //associations.put("efficiency 1 %", "EFF ");
    }

    private Text createText(String name) {
        Text text = new Text();
        text.setFont(Font.font("Courier New", 26));
        text.setOpacity(0.9);
        text.setFill(Color.WHITE);
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
        primaryStage.setWidth(400);
        primaryStage.setHeight(400);
        primaryStage.setMinWidth(400);
        primaryStage.setMaxHeight(400);
        primaryStage.setMaxWidth(400);
        primaryStage.setX(30);
        primaryStage.setY(380);
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
                            textMap.get(value).setText(value + "    " + parsedData.get(key) + " " + key.substring(key.lastIndexOf(' ') + 1))));
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