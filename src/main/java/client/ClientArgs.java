package client;

import com.beust.jcommander.Parameter;

public class ClientArgs {
    @Parameter(names = {"-t"}, description = "request type", required = true, order = 1)
    public String type;
    @Parameter(names = {"-i"}, description = "db cell", required = true, order = 2)
    public int cell;
    @Parameter(names = {"-m"}, description = "data to add to db (if type=set)", order = 3)
    public String msg;

    @Override
    public String toString() {
        return type + " " + cell + (msg != null ? " " + msg : "");
    }
}
