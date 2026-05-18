package rmi.common;

import java.io.Serializable;

public class RequestMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int TYPE_REQUEST = 0;

    private int    messageType;
    private int    requestId;
    private String objectReference;
    private String methodId;
    private byte[] arguments;

    public RequestMessage(int requestId, String objectReference,
                          String methodId, byte[] arguments) {
        this.messageType     = TYPE_REQUEST;
        this.requestId       = requestId;
        this.objectReference = objectReference;
        this.methodId        = methodId;
        this.arguments       = arguments;
    }

    public int    getMessageType()     { return messageType; }
    public int    getRequestId()       { return requestId; }
    public String getObjectReference() { return objectReference; }
    public String getMethodId()        { return methodId; }
    public byte[] getArguments()       { return arguments; }
}
