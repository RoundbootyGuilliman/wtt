package telemetry;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TelemetryApp extends Application {

    public static void main(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException, InterruptedException {



        VBox root = new VBox();
        Scene scene = new Scene(root);
        root.setMouseTransparent(true);

        primaryStage.setAlwaysOnTop(true);
        primaryStage.setTitle("TelemetryApp");
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(200);
        primaryStage.setMaxHeight(400);
        primaryStage.setMaxWidth(200);
        primaryStage.setScene(scene);


        Text ias = new Text();
        ias.setFont(Font.font(20));
        ias.setFill(Color.BLACK);

        Text spd = new Text();
        spd.setFont(Font.font(20));
        spd.setFill(Color.BLACK);

        Text thrust = new Text();
        thrust.setFont(Font.font(20));
        thrust.setFill(Color.BLACK);

        Text power = new Text();
        power.setFont(Font.font(20));
        power.setFill(Color.BLACK);

        Text alt = new Text();
        alt.setFont(Font.font(20));
        alt.setFill(Color.BLACK);

        root.getChildren().addAll(ias, spd, thrust, power, alt);

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

                    Platform.runLater(() -> {
                        ias.setText("IAS: " + parsedData.get("IAS km/h"));
                        spd.setText("SPD: " + parsedData.get("TAS km/h"));
                        thrust.setText("Thrust 1: " + parsedData.get("thrust 1 kgs"));
                        power.setText("Power 1: " + parsedData.get("power 1 hp"));
                        alt.setText("Altitude: " + parsedData.get("H m"));
                    });

                    Thread.sleep(10);
                }
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();

        primaryStage.show();
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