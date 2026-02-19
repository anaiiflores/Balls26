

import java.io.Serializable;

public class MsgDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    public final int code;        // 0=TRANSFER, 1=PING, 2=PONG
    public final int hasObject;    // 1 si payload no es null, 0 si null
    public final Object payload;   // WalkerDTO cuando code=0

    public MsgDTO(int code, int hasObject, Object payload) {
        this.code = code;
        this.hasObject = hasObject;
        this.payload = payload;
    }
}
