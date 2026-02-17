
import java.io.IOException;
import java.io.Serializable;


public class DataFrame implements Serializable {
    private static final long serialVersionUID = 1L;


    public final DataFrameType type;
    public final Object payload;

    public DataFrame(DataFrameType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }
    public void sendTransfer(Channel ch, WalkerDTO dto) throws IOException {
        ch.sendMsg(new MsgDTO(0, 1, dto)); // code=0, hasObject=1, payload=dto
    }

}