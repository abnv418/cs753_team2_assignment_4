package edu.unh.cs753.indexing;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs753.utils.IndexUtils;
import edu.unh.cs753.utils.SearchUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import utils.KotlinSearchUtils;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LuceneSearcher {
    public final IndexSearcher searcher;
    public final List<Data.Page> pages;


    public LuceneSearcher(String indexLoc, String queryCborLoc) {
        searcher = SearchUtils.createIndexSearcher(indexLoc);

        // Returning a list now instead of an iterable... because we end up using this more than once!
        pages = KotlinSearchUtils.INSTANCE.getPages(queryCborLoc);
    }

    /**
     * Function: query
     * Desc: Queries Lucene paragraph corpus using a standard similarity function.
     *       Note that this uses the StandardAnalyzer.
     * @param queryString: The query string that will be turned into a boolean query.
     * @param nResults: How many search results should be returned
     * @return TopDocs (ranked results matching query)
     */
    public TopDocs query(String queryString, Integer nResults) {
        Query q = SearchUtils.createStandardBooleanQuery(queryString, "text");
        try {
            return searcher.search(q, nResults);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Function: queryBigrams
     * Desc: Queries Lucene paragraph corpus using bigrams and a standard similarity function.
     *       Note that this uses the EnglishAnalyzer.
     * @param queryString: The query string that will be turned into a boolean query.
     * @param nResults: How many search results should be returned
     * @return TopDocs (ranked results matching query)
     */
    public TopDocs queryBigrams(String queryString, Integer nResults) {
        Query q = SearchUtils.createStandardBooleanQuerywithBigrams(queryString, "bigram");
        try {
            return searcher.search(q, nResults);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<idScore> doSearch(String query) throws IOException {
        Query q = SearchUtils.createStandardBooleanQuery(query, "unigram"); // Need to use EnglishAnalyze now.
//        TopDocs topDocs = query(q, 100);
        ArrayList<idScore> results = doSearch(q);
        if (query.contains("Brush")) { String parId = results.get(0).i; Integer docId = searcher.search(new TermQuery(new Term("id", parId)), 1) .scoreDocs[0].doc; System.out.println(searcher.doc(docId).get("text")); }
        return results;
    }

    public ArrayList<idScore> doBigramsSearch(String query) throws IOException {
        TopDocs topDocs = queryBigrams(query, 100);
        if (query.contains("Brush")) { Integer docId = topDocs.scoreDocs[0].doc; System.out.println(searcher.doc(docId).get("text")); }
        return parseTopDocs(topDocs);
    }

    // Overloaded version that takes a Query instead
    public ArrayList<idScore> doSearch(Query q) throws IOException {
        TopDocs topDocs = searcher.search(q, 100);
        return parseTopDocs(topDocs);
    }

    public ArrayList<idScore> doBigramsSearch(Query q) throws IOException {
        TopDocs topDocs = searcher.search(q, 100);
        return parseTopDocs(topDocs);
    }


    private ArrayList<idScore> parseTopDocs(TopDocs topDocs) throws IOException {
        ArrayList<idScore> al = new ArrayList<>();
        // This is an example of iterating of search results
        for (ScoreDoc sd : topDocs.scoreDocs) {
            Document doc = searcher.doc(sd.doc);
            String paraId = doc.get("id");
            float score = sd.score;
            idScore cur = new idScore(paraId, score);
            al.add(cur);
        }
        return al;
    }



    // Custom class for storing the retrieved data
    public class idScore {
        public String i;
        public float s;

        idScore(String id, float score) {
            i = id;
            s = score;
        }
    }


    public void custom() throws IOException {
        //System.out.println("This is custom Scoring function");
        SimilarityBase mysimilarity= new SimilarityBase() {
            @Override
            protected float score(BasicStats basicStats, float v, float v1) {
                float sum1 = 0.0f;
                sum1 += v;
                return sum1;
            }

            @Override
            public String toString() {
                return null;
            }
        };
        searcher.setSimilarity(mysimilarity);

    }

    public void jm() throws IOException {
        createjelinekmercer();
        FileWriter fstream = new FileWriter("jm.run", false);
        BufferedWriter out = new BufferedWriter(fstream);

        for (Data.Page page : pages) {

            // Id of the page, which is needed when you print out the run file
            String pageId = page.getPageId();

            // This query is the name of the page
            String query = page.getPageName();
            ArrayList<idScore> idSc = doSearch(query);
            int counter = 1;
            for (idScore item : idSc) {
                out.write(pageId + " Q0 " + item.i + " " + counter + " " + item.s + " team2-standard\n");
                counter++;
            }
        }
        out.close();
    }

    public void laplacerun() throws IOException
    {
        createlaplaceSmoothing();
        FileWriter fstream = new FileWriter("laplace_run.run", false);
        BufferedWriter out = new BufferedWriter(fstream);

        for (Data.Page page : pages) {

            // Id of the page, which is needed when you print out the run file
            String pageId = page.getPageId();

            // This query is the name of the page
            String query = page.getPageName();
            ArrayList<idScore> idSc = doSearch(query);
            int counter = 1;
            for (idScore item : idSc) {
                out.write(pageId + " Q0 " + item.i + " " + counter + " " + item.s + " team2-standard\n");
                counter++;
            }
        }
        out.close();
    }

    public void laplacebigramrun() throws IOException
    {
        createlaplaceSmoothing();
        FileWriter fstream = new FileWriter("bigram_run.run", false);
        BufferedWriter out = new BufferedWriter(fstream);

        for (Data.Page page : pages) {

            String pageId = page.getPageId();
            String query = page.getPageName();
            ArrayList<idScore> idSc = doBigramsSearch(query);
            int counter = 1;
            for (idScore item : idSc) {
                out.write(pageId + " Q0 " + item.i + " " + counter + " " + item.s + " team2-english\n");
                counter++;
            }
        }
        out.close();
    }


    public void dirilichtrun() throws IOException
    {
        createdirilicht();
        FileWriter fstream = new FileWriter("dr_run.run", false);
        BufferedWriter out = new BufferedWriter(fstream);

        for (Data.Page page : pages) {

            // Id of the page, which is needed when you print out the run file
            String pageId = page.getPageId();

            // This query is the name of the page
            String query = page.getPageName();
            ArrayList<idScore> idSc = doSearch(query);
            int counter = 1;
            for (idScore item : idSc) {
                out.write(pageId + " Q0 " + item.i + " " + counter + " " + item.s + " team2-standard\n");
                counter++;
            }
        }
        out.close();
    }



    /**
     * Function: queryWithCustomScore
     * Desc: Queries Lucene paragraph corpus using a custom similarity function.
     *       Note that this uses the StandardAnalyzer.
     * @param queryString: The query string that will be turned into a boolean query.
     * @param nResults: How many search results should be returned
     * @return TopDocs (ranked results matching query)
     */
    public TopDocs queryWithCustomScore(String queryString, Integer nResults) {
        Query q = SearchUtils.createStandardBooleanQuery(queryString, "text");
        IndexSearcher customSearcher = new IndexSearcher(searcher.getIndexReader());

        // Declares a custom similarity function for use with a new IndexSearcher
        SimilarityBase similarity = new SimilarityBase() {
            @Override
            protected float score(BasicStats basicStats, float freq, float docLen) {

                // Needs to be filled out
                return freq;
            }

            @Override
            public String toString() {
                return null;
            }

        };

        customSearcher.setSimilarity(similarity);

        try {
            return customSearcher.search(q, nResults);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SimilarityBase createBnnSimilarity() {
        return new SimilarityBase() {
            @Override
            protected float score(BasicStats basicStats, float freq, float docLen) {

                if (docLen == 0) {
                    return 0 * basicStats.getBoost();
                }
                else {
                    return 1 * basicStats.getBoost();
                }
            }

            @Override
            public String toString() {
                return null;
            }
        };

    }

    public SimilarityBase createLncSimilarity() {
        return new SimilarityBase() {
            @Override
            protected float score(BasicStats basicStats, float freq, float docLen) {

                double ans = (Math.log(1 + freq) / Math.sqrt(docLen)) * 1 * (1/Math.sqrt(docLen));
                return (float)ans * basicStats.getBoost();
            }

            @Override
            public String toString() {
                return null;
            }

        };

    }

    public SimilarityBase createAncSimilarity() {
        return new SimilarityBase() {
            @Override
            protected float score(BasicStats basicStats, float freq, float docLen) {

                double ans = 1 * 1/Math.sqrt(docLen);
                return (float)ans * basicStats.getBoost();
            }

            @Override
            public String toString() {
                return null;
            }

        };

    }

    /*
     * Create Unigram Language Model with Laplace Smoothing where alpha = 1
     *
     */
    public void createlaplaceSmoothing(){

        SimilarityBase similarity = new SimilarityBase() {
            @Override
            protected float score(BasicStats basicStats, float freq, float docLen) {

                float laplace= (freq+ 1)/ (2* docLen);

                return (float) Math.log(laplace);

            }

            @Override
            public String toString() {
                return null;
            }
        };

        searcher.setSimilarity(similarity);
    }


    public void createjelinekmercer(){

        SimilarityBase similarity = new SimilarityBase() {
            @Override
            protected float score(BasicStats basicStats, float freq, float docLen) {

                float  corpus= (freq)/(docLen);
                float jelenik= (float) ((corpus*0.1) + (freq*0.9));
                return (float) Math.log(jelenik);
            }
            @Override
            public String toString() {
                return null;
            }
        };
        searcher.setSimilarity(similarity);
    }

    /*
     * Longer documents naturally have better probability estimates. Hence we want to give more weight to those
     * probability estimates.
     *
     */
    public void createdirilicht(){

        SimilarityBase similarity= new SimilarityBase() {
            @Override
            protected float score(BasicStats basicStats, float freq, float docLen) {

                // Add mu items from collection
                float mu = 1000;
                float dr = 0;
                dr = (freq + mu * basicStats.getNumberOfDocuments()) / (docLen + mu);
                return dr;
            }
            @Override
            public String toString() { return null; }
        };

    }



    public static void main (String [] args) throws IOException {
        LuceneSearcher searcher1 = new LuceneSearcher("/home/rachel/ir/P1/paragraphs", "/home/rachel/ir/test200/test200-train/train.pages.cbor-outlines.cbor");
        searcher1.dirilichtrun();

        //LuceneSearcher searcher2=new LuceneSearcher("/Users/abnv/Desktop/indexer2/paragraphs","/Users/abnv/Desktop/train.pages.cbor-outlines.cbor");

        /*searcher2.createlaplaceSmoothing();
        searcher2.laplacerun();
        searcher2.jm();
        LuceneSearcher custom = new LuceneSearcher("/home/rachel/ir/P1/paragraphs", "/home/rachel/ir/test200/test200-train/train.pages.cbor-outlines.cbor");
        custom.custom();
        custom.customRun();
        LuceneIndexer indexer1= new LuceneIndexer("paragraphs");
        indexer1.doIndex("/Users/abnv/Desktop/Indexer/train.pages.cbor-paragraphs.cbor");*/
    }
}
