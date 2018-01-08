package co.peaku.automessenger;

/**
 * All constant values global to the app are inside this class.
 */
public final class Cts {

    public static final String URL = "url";
    public static final String JSON = "json";


    /**
     * HTTP CLIENT
     */
    public static class RESTCts{
        public final static String COMMAND           = "command";
        public final static String RECEIVER          = "receiver";
        public final static String GET               = "GET";

        public final static int COMMAND_GET_CONTACTS = 1;
    }


    /**
     * HTTP status codes.
     */
    public static class HTTPCts {

        public static final int FIRST_CODE = 0;
        public static final int OK = 200;
    }

    private Cts() {}
}
