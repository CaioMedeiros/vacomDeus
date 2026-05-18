package rmi.common;

import java.io.Serializable;

public class ReplyMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int TYPE_REPLY = 1;

    private int    messageType;
    private int    requestId;
    private String status;
    private byte[] result;

    public ReplyMessage(int requestId, String status, byte[] result) {
        this.messageType = TYPE_REPLY;
        this.requestId   = requestId;
        this.status      = status;
        this.result      = result;
    }

    public int    getMessageType() { return messageType; }
    public int    getRequestId()   { return requestId; }
    public String getStatus()      { return status; }
    public byte[] getResult()      { return result; }
}
