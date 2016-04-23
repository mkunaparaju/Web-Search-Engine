package PageRank;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
public class Page {

	private int wordcount;
	private double base;	
	private double score;
	private double newScore;
	private String title;
	private String text;
	private static int counter;
	private int id;
	private Map<String, Integer> outlinks = new HashMap<String, Integer>();
	
	public Page(File file) throws IOException
	{
		counter++;
		id = counter;
		Document doc = Jsoup.parse(file, "UTF-8");
		title = doc.title();
		text = doc.text();
		wordcount = text.split(" ").length;
		base = Math.log(wordcount)/Math.log(2);
		parseOutlinks(doc);
		
	
	}
	
	public void parseOutlinks(Document doc)
	{
		Elements links = doc.select("a");
		//for each outlink in the document
		for(Element link : links)
		{
			String url = link.attr("href");		
			int score = 1;
			if(outlinks.containsKey(url))
			{
				score = outlinks.get(url) + 1;
			}
			//gets all the parents of the link
			Elements parents = link.parents();
			///checks if any of the parent are the required tag
			for (Element parent: parents)
			{
	
				String nodeName = parent.tagName().toLowerCase();
				if((nodeName.equals("h1")) ||nodeName.equals("h2")||nodeName.equals("h3")||nodeName.equals("em")||nodeName.equals("b"))
				{
					score++ ;
					break;
				}
			}			
			
			//System.out.println(url + " Score "+ score);
				outlinks.put(url, score);
		

		}
		
	}
	public double getBase()
	{
		return this.base;
	}
	
	public double getScore()
	{
		return this.score;
	}
	public void setScore(double score)
	{
		this.score = score;
	}
	public double getNewScore()
	{
		return newScore;
	}
	public void setNewScore(double newScore)
	{
		this.newScore = newScore;
	}
	public String getTitle()
	{
		return title;
	}
	public int getId()
	{
		return id;
	}
	public Map<String,Integer> getOutlinks()
	{
		return outlinks;
	}
	public boolean hasOutlinks()
	{
		return (!outlinks.isEmpty()); 
	}
	
	

}
