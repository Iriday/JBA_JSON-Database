package client;

import com.beust.jcommander.Parameter;
import com.google.gson.Gson;

public class ClientArgs {
    @Parameter(names = {"-t"}, description = "request type", required = true, order = 1)
    public String type;
    @Parameter(names = {"-k"}, description = "key", required = true, order = 2)
    public String key;
    @Parameter(names = {"-v"}, description = "data to add to db (if type=set)", order = 3)
    public String value;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
