
import java.io.Serializable;


public class DataFrame implements Serializable {
    private static final long serialVersionUID = 1L;

    public final DataFrameType type;
    public final Object payload;

    public DataFrame(DataFrameType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }
}