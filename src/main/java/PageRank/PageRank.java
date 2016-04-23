package PageRank;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;


public class PageRank {

	@Parameter(names={"-docs"},required = true)
	private String docs;
	@Parameter(names={"-f"})
	private double fValue;
	//private int numOfFiles=0;
	
	private double epsilon;
	private List<Page> pageList = new ArrayList<Page>();
	private Map<String, Page> pageMap = new HashMap<String, Page>();
	private double weight [][] ; 
	private double normalizedWeight [][];
	
	public void init()throws IOException
	{
		Path docPath = FileSystems.getDefault().getPath(docs);
		File [] files = docPath.toFile().listFiles();
		int numOfFiles = files.length;
		System.out.println("NNumber of files " + numOfFiles);
		epsilon = 0.01/numOfFiles;
		weight = new double[numOfFiles][numOfFiles];
		normalizedWeight = new double[numOfFiles][numOfFiles];
		setScores(files);
//		calculateLinkWeight();
		
	}
	
	public void setScores(File[] files)throws IOException
	{
		double sum =0;
		//calculating sum
		for(File f : files)
		{
			Page page = new Page(f);
			sum = sum + page.getBase();
			//System.out.println("base " + page.getBase());
			pageList.add(page);
			pageMap.put(f.getName(), page);
		}
		//System.out.println("Sum " + sum);
		//setting score
		for(Page p : pageList)
		{
			double pBase = p.getBase();
			double score = pBase/sum;
			p.setScore(score);
			System.out.println("Score " + score);
		}
	}
	
	public void calculateLinkWeight()
	{
		for(Page p: pageList)
		{
			int P = p.getId() -1;
			if(!(p.hasOutlinks()))
			{
				for (Page q : pageList)
				{
					int Q = q.getId() - 1;
					weight[Q][P] = q.getScore();
				}
			}
			else
			{
				Map<String, Integer> outboundlinks = p.getOutlinks();  
				Iterator entries = outboundlinks.entrySet().iterator();
				
				
				while(entries.hasNext())
				{
					 Entry outlink = (Entry) entries.next();
					 Page q = pageMap.get((String)outlink.getKey());
					 int Q = q.getId() - 1;
					 int calcWeight = (Integer)outlink.getValue();
					 //System.out.println("calculated Weight " + calcWeight);
					 weight[Q][P]= calcWeight;
					 //System.out.print(p.getTitle() + " --> " + q.getTitle() + "    " + weight[Q][P]);
					// System.out.println("\n");
				}
				
				
				double sumWeight =0;
				for(int i = 0; i<weight.length; i++)
				{
					sumWeight = sumWeight + weight[i][P];
				
				}
				
				entries = outboundlinks.entrySet().iterator();
				
				while(entries.hasNext())
				{
					 Entry outlink = (Entry) entries.next();
					 Page q = pageMap.get((String)outlink.getKey());
					 int Q = q.getId() -1;
					 normalizedWeight[Q][P] = weight[Q][P]/sumWeight;
					 System.out.print(p.getTitle() + " --> " + q.getTitle() + "    " + weight[Q][P] + "        " + normalizedWeight[Q][P] + "\n") ;				 
				}							
			}
		}
	}
	
	public void calcNewScore()
	{
		boolean changed = true;
		while(changed)
		{
			for (Page page : pageList)
			{
				int P = page.getId() - 1;
				double normalizedScore = 0.0;
				
				for (Page qPage : pageList)
					
				{
					int Q  = qPage.getId() - 1 ;
			        normalizedScore += (qPage.getScore() * normalizedWeight[P][Q]);          
				}
			        double newScore = ((1.0 - fValue) * page.getBase()) + (fValue * normalizedScore);
			        page.setNewScore(newScore);

			          if(Math.abs(page.getNewScore() - page.getScore()) > epsilon) 
			          {
			            changed = true;
			          }
			          else
			          {
			        	  changed = false;
			          }
				}
			        for(Page page : pageList) 
			        {
			        	page.setScore(page.getNewScore());
			        }
			} //while
		}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		PageRank pr = new PageRank();
		new JCommander(pr, args);
		pr.init();
		//pr.setScores(files);
		pr.calculateLinkWeight();
		pr.calcNewScore();
	}

}
