package client;

import com.beust.jcommander.JCommander;

public class Main {

    public static void main(String[] args) {

        Args commands = new Args();

        JCommander.newBuilder()
                .addObject(commands)
                .build()
                .parse(args);

        Client client = new Client();
        client.start(commands);
    }
}
