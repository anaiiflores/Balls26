import java.io.Serializable;

public class MsgDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // code: 0=TRANSFER, 1=PING, 2=PONG
    public final int code;

    // hasObject: 1 = payload es un objeto, 0 = no hay payload
    public final int hasObject;

    // payload: por ejemplo WalkerDTO cuando code=0
    public final Object payload;

    public MsgDTO(int code, int hasObject, Object payload) {
        this.code = code;
        this.hasObject = hasObject;
        this.payload = payload;
    }
}
