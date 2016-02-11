package IndexRetrieve;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Date;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Retriever 
{
	public static void main(String[] args) throws Exception 
	{
		if (args.length != 2) 
		{
			throw new Exception("Usage: java " + Retriever.class.getName()+ " <index dir> <query>");
		}
		
		//File indexDir = new File(args[0]);
		String indexDir = args[0];
		File indexFile = new File(indexDir);
		String q = args[1];
		if (!indexFile.exists() || !indexFile.isDirectory()) 
		{			
			throw new Exception(indexDir +" does not exist or is not a directory.");
		}
		
		search(indexDir, q);
	}
	
		public static void search(String indexDir, String q)throws Exception 
		{
			Path indexPath = FileSystems.getDefault().getPath(indexDir);
			Directory fsDir = FSDirectory.open(indexPath);
			//Directory fsDir = FSDirectory.getDirectory(indexDir, false);
			IndexReader ir = DirectoryReader.open(fsDir);
			IndexSearcher is = new IndexSearcher(ir);
			QueryParser qp =  new QueryParser("body",new StandardAnalyzer());
			Query query = qp.parse(q);
			
			long start = new Date().getTime();
			TopDocs hits = is.search(query, 10);
			long end = new Date().getTime();
			System.err.println("Found " + hits.totalHits +" document(s) (in " + (end - start) +" milliseconds) that matched query '" +q + "':");
			
			ScoreDoc[] scoreDoc = hits.scoreDocs;
			
			for (int i = 0; i < scoreDoc.length ; i++) 
			{
				ScoreDoc scDocs = hits.scoreDocs[i];
				Document doc = is.doc(scDocs.doc);
				
				System.out.println(doc.get("filename"));
			}
		}
}
