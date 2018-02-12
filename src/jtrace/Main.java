package jtrace;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.exec.*;
import org.apache.commons.exec.DefaultExecutor;

public class Main
{
	static String LOGPATH = "NOLOG";
	static String GoogleMapsHardCode;
	public static void main(String[] args) throws InterruptedException, IOException 
	{	
		if(args.length==1 && args[0].equalsIgnoreCase("debug")){LOGPATH = "C:/Logs/GeoIP.txt";System.out.println(logShort("Debug Mode Enabled. Using Default PATH To C:/Logs/GeoIP.txt"));}
		if(args.length==2) {System.out.println(logShort("Debug Mode Enabled. Checking Provided Path..."));if(isValidPath(args[1])){LOGPATH = args[1];System.out.println(logShort("Path Is Good, Saving Logs To " + args[1]));}else{LOGPATH = "C:/Logs/GeoIP.txt";System.out.println(logShort("Path Format Invalid... Using Default PATH To C:/Logs/GeoIP.txt"));}}
				
		String line = "netstat -n";
		CommandLine cmdLine = CommandLine.parse(line);
		DefaultExecutor executor = new DefaultExecutor();
		int[] values = null;
		executor.setExitValues(values);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PumpStreamHandler PSH = new PumpStreamHandler(os);
		
		executor.setStreamHandler(PSH);
		PSH.start();
		System.out.println(logShort("Now Executing netstat In Apache-Commons Command Exec Shell. If Error Occurs Here, Check PATH Variables & UAC / Permissions For JRE"));
		executor.execute(cmdLine);
		
		String aString = new String(os.toByteArray(),"UTF-8");
		List<String> items = Arrays.asList(aString.split("\\s+"));
		ArrayList<String> ALitems = new ArrayList<>(items);
		TreeSet<String> TSForeign = new TreeSet<>();
		
		//Bisect Elements of HTTP Request and Create Separate Strings For Zoom, Size, Type and Marker Design
		GoogleMapsHardCode = "https://maps.googleapis.com/maps/api/staticmap?size=1200x1200&maptype=satellite" + 
				"&markers=color:blue%7Clabel:TCP";
		
		PSH.stop();
		System.out.println(ALitems);
		System.out.println();
		System.out.println(logShort("Network List Produced, Now Parsing Will Start To Extract Local IPv4 Address & Foreign IPv4 Address"));
		int LocIP = 10; int ForIP = 11;
		while(LocIP < ALitems.size())
		{
			String Local = ALitems.get(LocIP);
			Local = Local.substring(0,Local.lastIndexOf(':'));
			String Foreign = ALitems.get(ForIP);
			Foreign = Foreign.substring(0,Foreign.lastIndexOf(':'));
			System.out.println("[Loc]-" + Local + " [For]-" + Foreign);
			logShort("\n[Loc]-" + Local + " [For]-" + Foreign);
			LocIP += 4; ForIP += 4;
			TSForeign.add(Foreign);
		}
		logShort("-----------------------------------------------------------------------");
		System.out.println();
		System.out.println(logShort(TSForeign.toString()));
		logShort("-----------------------------------------------------------------------");
		System.out.println();

		System.out.println(logShort("Foreign IPv4 Address TreeSet Built, Instantiating GeoHandler Now"));
		GeoHandler GH = new GeoHandler();
		String ForeignIP = "";

		System.out.println(logShort("GeoHandler Instantiated, Now Beginning Iteration Through addGeo() Method Calls"));
		for (String strFIP : TSForeign){ForeignIP = "";ForeignIP = GH.addGeo(strFIP, strFIP);System.out.println(log(ForeignIP));}
		System.out.println(logShort("Finished Iteration of addGeo() Method Calls, Generating Google Maps Static API HTTP-GET Url Key..."));
		
		System.out.println("\n\n" + GoogleMapsHardCode + "&key=APIKEY");logShort("\n\n" + GoogleMapsHardCode + "&key=APIKEY");
		final String URL = GoogleMapsHardCode + "&key=APIKEY";
		HttpURLConnection con = (HttpURLConnection) new URL(URL).openConnection();
		BufferedImage BI = ImageIO.read(con.getInputStream());
		JFrame f = new JFrame();
	    f.setLayout(new BorderLayout());
	    final JPanel p = new JPanel();
	    p.add(new JLabel(new ImageIcon(BI)));
	    f.add(p, BorderLayout.CENTER);
	    f.pack();
	    f.setVisible(true);
		
		System.out.println(logShort("One Last Thing... Making Sure Executor.newCachedThreadPool() Is Properly Shutdown"));
		try{GH.executor.shutdown();}catch(Exception E){System.out.println(logShort(E.toString()));}
		System.out.println(logShort("Are All Threads In Cached Pool Terminated? " + GH.executor.isTerminated() + " Is Executor Shutdown? " + GH.executor.isShutdown()));
		System.out.println(logShort("Current Thread Count After Shutdown Command (From ManagementFactory => Not Specific To Thread Groups) " + ManagementFactory.getThreadMXBean().getThreadCount()));
		try{if (!GH.executor.awaitTermination(60, TimeUnit.SECONDS)){
			System.out.println(logShort("Not All Cached Threads Are Terminated. Awaiting Termination For 60 Seconds, Then Second Attempt To Shutdown Will Begin..."));	 
			GH.executor.shutdownNow();
	    	if (!GH.executor.awaitTermination(60, TimeUnit.SECONDS))
	    		System.out.println(logShort("Waited 120 Seconds Total For Terminations, Shutdown Unsuccessful. Try Checking HTTP Ports / Traffic To See If HTTP-Connections Closed or Not..."));
		    }} catch (InterruptedException ie){GH.executor.shutdownNow();}
	}
	private static String log(String message){
		if(LOGPATH.equals("NOLOG")){return "";}
		else { try {
			String[] LOGparse = message.split(System.lineSeparator());
    		String logMSG = System.nanoTime() + " : [EndPointIP] " + LOGparse[0] + "\t\t [NodeIP] " + LOGparse [1];
    		if (LOGparse.length != 4){logMSG += "\t\t [GPS_NA] *" + System.lineSeparator();} else {GoogleMapsHardCode += '|' + LOGparse[2] + "," + LOGparse[3];
    			logMSG = logMSG + "\t\t [Latitude] " + LOGparse[2] + "\t\t [Longitude] " + LOGparse[3] + System.lineSeparator();}
    		Files.write(Paths.get(LOGPATH), (logMSG).getBytes(),StandardOpenOption.valueOf("CREATE"),StandardOpenOption.valueOf("APPEND"));
    		return logMSG;
    	} catch(Exception E) {E.printStackTrace();}}
		return "";}
	private static String logShort(String message){
		if(LOGPATH.equals("NOLOG")){return "";}
		else { try {
    		String logMSG = System.nanoTime() + " : " + message;
    		Files.write(Paths.get(LOGPATH), (logMSG).getBytes(),StandardOpenOption.valueOf("CREATE"),StandardOpenOption.valueOf("APPEND"));
    		return logMSG;
    	} catch(Exception E) {E.printStackTrace();}}
		return "";}
	public static boolean isValidPath(String path) {try{Paths.get(path);} catch (InvalidPathException | NullPointerException ex) {return false;}return true;}
}