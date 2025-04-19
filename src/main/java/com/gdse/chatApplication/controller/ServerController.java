package com.gdse.chatApplication.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ServerController implements Initializable {

    @FXML
    private Button btnSend;

    @FXML
    private Button btnUpload;

    @FXML
    private ImageView imgView;

    @FXML
    private TextArea txtChat;

    @FXML
    private TextField txtInput;

    String sMessage;
    ServerSocket serverSocket;
    Socket socket;
    DataOutputStream dataOutputStream;
    DataInputStream dataInputStream;

//    @FXML
//    void btnUploadOnClick(ActionEvent event) {
//        FileChooser fileChooser = new FileChooser();
//        File file = fileChooser.showOpenDialog(new Stage());
//        if (file != null) {
//            new Thread(() -> {
//                try {
//                    FileInputStream fileInputStream = new FileInputStream(file);
//
//                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
//                    dataOutputStream.flush();
//
//                } catch (FileNotFoundException e) {
//                    throw new RuntimeException(e);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }).start();
//        }
//    }
    @FXML
    void btnUploadOnClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
//        fileChooser.getExtensionFilters().add(
//                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
//        );
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            new Thread(() -> {
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    // Send marker to indicate image sending
                    dataOutputStream.writeUTF("IMAGE");
                    dataOutputStream.writeUTF(file.getName());
                    dataOutputStream.writeLong(file.length());

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        dataOutputStream.write(buffer, 0, bytesRead);
                    }

                    dataOutputStream.flush();
                    fileInputStream.close();

                    System.out.println("Image sent successfully!");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @FXML
    void btnSendOnClick(ActionEvent event) throws IOException {
        sMessage = txtInput.getText();
//        txtChat.appendText("Server's Messege : " + sMessage + "\n");
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeUTF(sMessage);
        dataOutputStream.flush();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(5000);
                txtChat.appendText("Server starting...\n");
                socket = serverSocket.accept();
                txtChat.appendText("Client connected\n");
                dataInputStream = new DataInputStream(socket.getInputStream());
//                String uMessege;
//                do {
//                    uMessege = dataInputStream.readUTF();
//                    txtChat.appendText("User's Messege -> " + uMessege + "\n");
//                } while (!uMessege.equals("bye"));

                while (true) {
                    String header = dataInputStream.readUTF();

                    if (header.equals("IMAGE")) {
                        // Read image name and size
                        String fileName = dataInputStream.readUTF();
                        long fileSize = dataInputStream.readLong();

                        // Save to a temporary file
                        File receivedFile = new File("received_" + fileName);
                        FileOutputStream fileOutputStream = new FileOutputStream(receivedFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        long totalRead = 0;

                        while (totalRead < fileSize && (bytesRead = dataInputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                            totalRead += bytesRead;
                        }

                        fileOutputStream.close();

                        Platform.runLater(() -> {
                            imgView.setImage(new javafx.scene.image.Image(receivedFile.toURI().toString()));
                            txtChat.appendText("📷 Image received: " + fileName + "\n");
                        });

                    } else {
                        // Treat as normal text message
                        String message = header;
                        Platform.runLater(() -> txtChat.appendText("User's Message -> " + message + "\n"));
                    }
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

    }
}
