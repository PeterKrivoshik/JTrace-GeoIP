package jtrace;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class GeoHandler{
	ExecutorService executor; HashMap<String, String> GeoDB; // K=NodeIP | V=^LatGPS /n LongGPSv
	public GeoHandler(){executor = Executors.newCachedThreadPool(); GeoDB = new HashMap<>();}
	public String addGeo(String NodeIP, String TermIP){
		return GeoDB.containsKey(NodeIP) ? TermIP + System.lineSeparator() + NodeIP + System.lineSeparator() + GeoDB.get(NodeIP) : TermIP + System.lineSeparator() + callGeo(NodeIP, TermIP); //Replace with futC(String NodeIP, String TermIP)
	}
	public String callGeo(String NodeIP, String TermIP){
		//System.out.println("\n!Had to Call!");
		Future<String> futureCall = executor.submit(new GeoIP(NodeIP, TermIP));
	    try{
	    	String[] resultParse = futureCall.get(45,TimeUnit.SECONDS).split(System.lineSeparator()); 
	    	if(resultParse.length<3){
	    		System.out.println("Too Short"); 
	    		return TermIP + System.lineSeparator() + NodeIP + System.lineSeparator() + "*";} else {
	    	GeoDB.put(resultParse[1], resultParse[2] + System.lineSeparator() + resultParse[3]); 
	    	return resultParse[1] + System.lineSeparator() + resultParse[2] + System.lineSeparator() + resultParse[3]; 
	    	} 
	    } catch(Exception E) {E.printStackTrace(); futureCall.cancel(true); return TermIP + System.lineSeparator() + NodeIP + System.lineSeparator() + "**";}}	
}