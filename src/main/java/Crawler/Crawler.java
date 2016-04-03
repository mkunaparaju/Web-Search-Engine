package Crawler;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.*;
import java.net.*;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class Crawler {
	
private static final int MAX_PAGES = 50;
public static final int MAXSIZE = 50000;
private final String AGENT = "User-agent: *";
private final String DISALLOW = "Disallow:";

	
	@Parameter(names={"-url", "-u"},required = true)
	private String startingUrl;
	@Parameter(names={"-query", "-q"},required = true,variableArity = true)
	private List<String> query;
	@Parameter(names={"-docs"},required = true)
	private String docs;
	@Parameter(names={"-maxPages", "-m"})
	private int maxPages = MAX_PAGES;
	@Parameter(names={"-trace", "-t"})
	private boolean debug = false;
	
	private HashMap<URL, Link> seenURL;
	private PriorityQueue<Link> urlQueue;
	private int downloadedPages;
	
	public void initialize(String [] args)
	{
		downloadedPages = 0;
		seenURL = new HashMap<URL,Link>();
		urlQueue = new PriorityQueue<Link>(MAX_PAGES, new ScoreComparator());
		URL url;
		Link link;
		try
		{
			url = new URL(startingUrl);	
			link = new Link(url);
			seenURL.put(url, link);
			urlQueue.add(link);
			System.out.println("Starting search: Initial URL " + url.toString()); 
		}
		catch (MalformedURLException e) 
		{
			System.out.println("Invalid starting URL " + args[0]);
		    return;
		}		
	}
	
	
	public void run(String [] args) throws IOException
	{
		initialize(args);
		while((urlQueue.size() != 0) && (downloadedPages < MAX_PAGES)) 
		{
			Link newLink = urlQueue.poll();
			URL newURL = newLink.getURL();			
			
			if(isRobotSafe(newURL))
			{
				if(debug) System.out.println("Downloading the page : "+ newURL);
				String content = getPage(newLink, MAXSIZE);
				String filename = docs + "/"+ newURL.getHost() + newURL.getPath();
				
				//System.out.println(filename);
				boolean success = savePage(docs + "/"+ newURL.getHost() + newURL.getPath(),content);
				downloadedPages++;
				
				if(success) System.out.println("Downloaded to file : " + newURL);
				
				if (downloadedPages >= maxPages) break; 
				//check if by giving the whole file name the file will open or not
				if (content.length() != 0) processPage(filename,newURL);	
			}		
		}
	}
	
	public String getPage(Link link, int maxSize) throws IOException
	{
		URL url = link.getURL();
		
	    try {
	      // try opening the URL
	      URLConnection urlConnection = url.openConnection();
	      urlConnection.setAllowUserInteraction(false);

	      InputStream urlStream = url.openStream();
	      // search the input stream for links. first, read in the entire URL
	      byte b[] = new byte[1000];
	      int numRead = urlStream.read(b);
	      String content = new String(b, 0, numRead);
	      while ((numRead != -1) && (content.length() < maxSize)) {
	        numRead = urlStream.read(b);
	        if (numRead != -1) {
	          String newContent = new String(b, 0, numRead);
	          content += newContent;
	        }
	      }
	      return content;

	    } catch (IOException e) {
	      System.out.println("ERROR: couldn't open URL " + url);
	      return "";
	    }
	}
	
	
	public boolean savePage(String filename, String content)
	{
		Writer writer = null;
	    File file = new File(filename);
	    File parent = file.getParentFile();

	    if (!parent.exists()) {
	      parent.mkdirs();
	    }

	    try {
	      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
	          file), "utf-8"));
	      writer.write(content);
	    } catch (IOException ex) {
	      return false;
	    } finally {
	      try {
	        writer.close();
	      } catch (Exception ex) {/* ignore */
	      }
	    }
	    return true;
	}
	
	public void processPage(String filename, URL parentURL) throws IOException
	{
		File file = new File(filename);
		Document doc = Jsoup.parse(file,"UTF-8", parentURL.toString());
		Elements links = doc.select("a[href]");
		
		for (Element link : links) 
		{
			
			String linkHref = link.attr("href");
			String anchor = link.text();

			URL childUrl = new URL(parentURL,linkHref);
			Link newLink = new Link(childUrl);
			newLink.setAnchor(anchor);
			int score = scoring(file,link,parentURL);
			if (downloadedPages == maxPages)
			{
				break;
			}
			
				addnewurl(newLink, childUrl,anchor, score);
		}
		
	}
	
	public void addnewurl(Link newLink, URL childUrl, String anchor, int score)
	{

		if(!seenURL.containsKey(childUrl)){
			seenURL.put(childUrl, newLink);
			if(!urlQueue.contains(newLink)){
				newLink.setScore(score);
				urlQueue.add(newLink);
				if(debug){
					System.out.println("Adding to queue: " + newLink.getURL() + " with score = " + newLink.getScore());
				}
			}
		}
		else{
			Link retrievedLink = seenURL.get(childUrl);
			if(urlQueue.contains(retrievedLink) && score > 0){
				urlQueue.remove(retrievedLink);
				int newScore = score + retrievedLink.getScore();
				retrievedLink.setScore(newScore);
				urlQueue.add(retrievedLink);
				if(debug){
					System.out.println("Adding " + score + " to score of " + retrievedLink.getURL());
				}
			}
		}
	
	}
	public int scoring(File file, Element link, URL url)
	{
		
		return 0;
	}
	
	public boolean isRobotSafe(URL url) throws IOException
	{
	    String strHost = url.getHost();

		// form URL of the robots.txt file
	    String strRobot = "http://" + strHost + "/robots.txt";
	    URL urlRobot;
	    try { urlRobot = new URL(strRobot);
		} catch (MalformedURLException e) {
		    // something weird is happening, so don't trust it
		    return false;
		}

	    if (debug) System.out.println("Checking robot protocol " + 
	                                   urlRobot.toString());
	    String strCommands;
	    try {
	       InputStream urlRobotStream = urlRobot.openStream();

		    // read in entire file
	       byte b[] = new byte[1000];
	       int numRead = urlRobotStream.read(b);
	       strCommands = new String(b, 0, numRead);
	       while (numRead != -1) {
	          numRead = urlRobotStream.read(b);
	          if (numRead != -1) {
	             String newCommands = new String(b, 0, numRead);
		         strCommands += newCommands;
			}
		    }
	       urlRobotStream.close();
		} catch (IOException e) {
		    // if there is no robots.txt file, it is OK to search
		    return true;
		}
	        if (debug) System.out.println(strCommands);

		// assume that this robots.txt refers to us and 
		// search for "Disallow:" commands.
		String strURL = url.getFile();
		int index = 0;
		while ((index = strCommands.indexOf(DISALLOW, index)) != -1) {
		    index += DISALLOW.length();
		    String strPath = strCommands.substring(index);
		    StringTokenizer st = new StringTokenizer(strPath);

		    if (!st.hasMoreTokens())
			break;
		    
		    String strBadPath = st.nextToken();

		    // if the URL starts with a disallowed path, it is not safe
		    if (strURL.indexOf(strBadPath) == 0)
			return false;
		}

		return true;
		
	}

	public static void main(String[] args) throws IOException
	{
		// TODO Auto-generated method stub
		Crawler crawler = new Crawler();
		new JCommander(crawler, args);
		crawler.run(args);
		
	}

}

	
class Link {

	URL url;
	int score;
	String anchor;
	
	Link(URL url)
	{
		this.url  = url;
		score =0;
		anchor = "";
	}
	
	public URL getURL()
	{
		return this.url;
	}
	public void setURL(URL url)
	{
		this.url = url;
	}
	
	public int getScore()
	{
		return this.score;
	}
	public void setScore(int score)
	{
		this.score = score;
	}
	
	public String getAnchor()
	{
		return this.anchor;
	}
	public void setAnchor(String anchor)
	{
		this.anchor = anchor;
	}
}



class ScoreComparator implements Comparator<Link>
{
	public int compare(Link link1, Link link2)
	{
		
		return 0;
	}
}