package server;

public class NoSuchKeyException extends  RuntimeException {

    public NoSuchKeyException() {

    }

    public NoSuchKeyException(String msg) {
        super(msg);
    }

    public NoSuchKeyException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public NoSuchKeyException(Throwable cause) {
        super(cause);
    }

}
