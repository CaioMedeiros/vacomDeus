package rmi.server;

import estoque.Estoque;
import rmi.common.ReplyMessage;
import rmi.common.RequestMessage;

import java.net.InetAddress;

public class EstoqueServer {

    public static final int PORT = 5000;

    public static void main(String[] args) throws Exception {
        Estoque estoque      = new Estoque("Estoque Central");
        Dispatcher dispatcher = new Dispatcher(estoque);
        RequestReplyProtocol rrp = new RequestReplyProtocol(PORT);

        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║   SERVIDOR DE ESTOQUE - RMI      ║");
        System.out.println("╚══════════════════════════════════╝");

        while (true) {
            try {
                RequestMessage req    = rrp.getRequest();
                InetAddress clientAddr = rrp.getClientAddress();
                int         clientPort = rrp.getClientPort();

                System.out.printf("[REQ #%d] objeto=%s método=%s de %s:%d%n",
                        req.getRequestId(), req.getObjectReference(),
                        req.getMethodId(), clientAddr.getHostAddress(), clientPort);

                ReplyMessage reply = dispatcher.dispatch(req);
                rrp.sendReply(reply, clientAddr, clientPort);

                System.out.printf("[REP #%d] status=%s%n",
                        reply.getRequestId(), reply.getStatus());

            } catch (Exception e) {
                System.err.println("Erro ao processar requisição: " + e.getMessage());
            }
        }
    }
}
