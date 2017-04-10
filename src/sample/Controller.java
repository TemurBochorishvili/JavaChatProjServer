package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;


public class Controller implements Initializable{

    private Stage primaryStage;
    private Thread thread;
    private ServerSocket serverSocket;
    private ObservableList<String> ips = FXCollections.observableArrayList();
    private ArrayList<ConnectToClient> clientList;

    @FXML
    private ListView listView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listView.setItems(ips);

        clientList = new ArrayList<>();

        try {
            serverSocket = new ServerSocket(5555);
        } catch (IOException e) {
            e.printStackTrace();
        }

        thread = new Thread(() -> {
            try {
                while(true){
                    Socket socket = serverSocket.accept();

                    ips.add(socket.getRemoteSocketAddress().toString());

                    clientList.add(new ConnectToClient(socket));


                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }

    private class ConnectToClient{

        Scanner in;
        PrintWriter out;
        Socket socket;


        ConnectToClient(Socket socket) throws IOException {
            this.socket = socket;

            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(),true);


            Thread read = new Thread(() -> {
                while(true){

                        while(in.hasNextLine()){
                            sendToAll(socket.getInetAddress() + " " + in.nextLine());
                        }
                }
            });

            read.start();
        }

        public void write(String text) {
                out.println(text);
                out.flush();
        }

    }

    public void sendToAll(String text){
        for(ConnectToClient client : clientList)
            client.write(text);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;

        primaryStage.setOnHidden(event -> {
            System.exit(0);
        });
    }


}

