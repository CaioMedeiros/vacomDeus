package rmi.client;

import rmi.common.RemoteObjectRef;
import rmi.common.ReplyMessage;
import rmi.common.RequestMessage;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class CommunicationModule {

    private static final AtomicInteger requestCounter = new AtomicInteger(1);

    public byte[] doOperation(RemoteObjectRef ref, String methodId, byte[] arguments)
            throws IOException, ClassNotFoundException {

        int reqId = requestCounter.getAndIncrement();
        RequestMessage request = new RequestMessage(reqId, ref.getObjectName(), methodId, arguments);

        try (Socket socket = new Socket(ref.getHost(), ref.getPort())) {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            oos.writeObject(request);
            oos.flush();

            ReplyMessage reply = (ReplyMessage) ois.readObject();

            if ("ERROR".equals(reply.getStatus()))
                throw new RuntimeException("Erro no servidor: " + new String(reply.getResult(), "UTF-8"));

            return reply.getResult();
        }
    }
}
