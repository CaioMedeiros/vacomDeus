package rmi.server;

import rmi.common.ReplyMessage;
import rmi.common.RequestMessage;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class RequestReplyProtocol {

    private ServerSocket serverSocket;
    private Socket currentClient;
    private ObjectOutputStream currentOos;
    private ObjectInputStream  currentOis;

    public RequestReplyProtocol(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        System.out.println("Servidor escutando na porta " + port + "...");
    }

    public RequestMessage getRequest() throws IOException, ClassNotFoundException {
        currentClient = serverSocket.accept();
        currentOos = new ObjectOutputStream(currentClient.getOutputStream());
        currentOos.flush();
        currentOis = new ObjectInputStream(currentClient.getInputStream());
        return (RequestMessage) currentOis.readObject();
    }

    public void sendReply(ReplyMessage reply, InetAddress clientHost, int clientPort)
            throws IOException {
        currentOos.writeObject(reply);
        currentOos.flush();
        currentClient.close();
    }

    public InetAddress getClientAddress() {
        return currentClient != null ? currentClient.getInetAddress() : null;
    }

    public int getClientPort() {
        return currentClient != null ? currentClient.getPort() : 0;
    }

    public void close() throws IOException {
        serverSocket.close();
    }
}
