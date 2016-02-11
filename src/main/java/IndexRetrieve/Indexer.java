package IndexRetrieve;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Date;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import HTMLParser.JTidyHTMLHandler;

public class Indexer {
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			throw new Exception("Usage: java " + Indexer.class.getName()+ " <index dir> <data dir>");
			}
		Path indexPath = FileSystems.getDefault().getPath(args[0]);
		//indexdir is the dire when we want to store lucene index
		Directory indexDir = FSDirectory.open(indexPath);
		//datadir is the directory containing the files we want to index.. this should be the output of the html parser
		String dataDir = args[1];
		
		long start = new Date().getTime();
		int numIndexed = index(indexDir, dataDir);
		long end = new Date().getTime();	
		System.out.println("Indexing " + numIndexed + " files took "+ (end - start) + " milliseconds");
		}
	
	
// open an index and start file directory traversal
	public static int index(Directory indexDir, String dataDir)throws IOException 
	{
		
		File dataFile = new File(dataDir);
		if (!dataFile.exists()) 
		{
			throw new IOException(dataDir + " does not exist");
		}
		
		IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());		
		IndexWriter writer = new IndexWriter(indexDir, iwc);
		//writer.setUseCompoundFile(false);
		indexDirectory(writer, dataFile);
		int numIndexed = writer.numDocs();
		//writer.optimize();
		writer.close();
		return numIndexed;
	}
	
	
		// recursive method that calls itself when it finds a directory
	private static void indexDirectory(IndexWriter writer, File dir)throws IOException 
	{
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) 
			{
				File f = files[i];
				if (f.isDirectory()) 
				{
					indexDirectory(writer, f);
				} 
				else if (f.getName().endsWith(".html")) 
				{
					indexFile(writer, f);
				}
			
			}
	}
	private static void indexFile(IndexWriter writer, File f)throws IOException 
			{
				if (f.isHidden() || !f.exists() || !f.canRead()) 
				{
					return;
				}
				System.out.println("Indexing " + f.getCanonicalPath());
				Document doc = new JTidyHTMLHandler().getDocument(new FileInputStream(f));
				
				System.out.println(doc.toString());
				
				//doc.add(new TextField("contents", new FileReader(f)));
				//passing jtidy parser here to the string field
				doc.add(new StringField("filename", f.getCanonicalPath(), Field.Store.YES));
				writer.addDocument(doc);
			}
		}



