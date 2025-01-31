package chat_server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientManager implements Runnable{
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;
    public final static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {

        try {
            this.socket = socket;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            name = bufferedReader.readLine();
            clients.add(this);
            System.out.println(name + " подключился к чату.");
            broadcastMessage("Server: "+name+" подключился к чату.");

            bufferedWriter.write("Server: Чтобы отправить личное сообщение, введите: @Имя сообщение.");
            bufferedWriter.newLine();
            bufferedWriter.flush();

        }
        catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()){
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            } catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }
    private void closeEverything(Socket socket, BufferedReader bufferedReader,
                                 BufferedWriter bufferedWriter) {
        removeClient();

        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public void removeClient(){
        clients.remove(this);
        System.out.println(name + " покинул чат.");
        broadcastMessage("SERVER: "+name+" покинул чат.");
    }
    private void broadcastMessage(String messageToSend) {
        for (ClientManager client: clients) {
            try {

                String secondWord = messageToSend.split(" ")[1]; // Второе слово
                char firstChar = secondWord.charAt(0);

                if (firstChar == '@' ) {
                    String personName = secondWord.substring(1); // Убираем @
                    if (client.name.equals(personName) && (!client.name.equals(name))) {
                        messageToSend = messageToSend.replaceFirst("^(\\S+\\s+)\\S+\\s*", "$1");
                        client.bufferedWriter.write("Персональное сообщение от " + messageToSend);
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                    }
                }
                else {
                        if (!client.name.equals(name)) {
                        client.bufferedWriter.write(messageToSend);
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                        }
                    }
            } catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }
}
