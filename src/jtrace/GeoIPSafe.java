package jtrace;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class GeoIPSafe implements Callable<String>{
	String URL;	public GeoIPSafe(String URL){this.URL = URL;}
    @Override
    public String call() throws Exception{ HttpURLConnection con = (HttpURLConnection) new URL(URL).openConnection();
        try (AutoCloseable conc = () -> con.disconnect()) {
        	if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
        		try(java.util.Scanner s = new java.util.Scanner(con.getInputStream())){return s.useDelimiter("\\A").hasNext() ? s.next() : "";}
        	} else {return "*";}
        }
    }
}