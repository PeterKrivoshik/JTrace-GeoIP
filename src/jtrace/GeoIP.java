package jtrace;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class GeoIP implements Callable<String>{
	String NodeIP, TermIP, URL;	public GeoIP(String NodeIP, String TermIP){this.NodeIP = NodeIP; this.TermIP = TermIP; URL = "http://freegeoip.net/csv/"+NodeIP;}
    @Override
    public String call() throws Exception{
    	HttpURLConnection con = (HttpURLConnection) new URL(URL).openConnection();
    	return con.getResponseCode() == HttpURLConnection.HTTP_OK ? extract(parse(con.getInputStream())) : "*" + String.valueOf(con.getResponseCode());} //"*";
    String extract(String data){
    	List<String> items = Arrays.asList(data.split("\\s*,\\s*"));
    	String _data = TermIP + System.lineSeparator() + NodeIP + System.lineSeparator() + items.get(8) + System.lineSeparator() + items.get(9);
    	return _data;}
    String parse(InputStream inputStream){ try(Scanner s = new Scanner(inputStream)){return s.useDelimiter("\\A").hasNext() ? s.next() : "";}}}