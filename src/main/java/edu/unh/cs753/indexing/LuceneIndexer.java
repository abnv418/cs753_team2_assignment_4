package edu.unh.cs753.indexing;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs753.utils.IndexUtils;
import edu.unh.cs753.utils.SearchUtils;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LuceneIndexer {
    private final IndexWriter writer;

    public LuceneIndexer(String indexLoc) {
        writer = IndexUtils.createIndexWriter(indexLoc);
    }

    public void doIndex(String cborLoc) throws IOException {
        int counter = 0;
        for (Data.Paragraph p : IndexUtils.createParagraphIterator(cborLoc)) {
            Document doc = new Document();
            doc.add(new StringField("id", p.getParaId(), Field.Store.YES));

            // Get the tokens using the English analyzer
            List<String> tokens1 = SearchUtils.createTokenList(p.getTextOnly(), new EnglishAnalyzer());
            String unigram = String.join(" " + tokens1);

            // Concatenate tokens together to get "text" field
            doc.add(new TextField("text", unigram, Field.Store.YES));

            // Run bigram method on tokens just obtained and get back bigram tokens.
            // Concatenate them together and store as field.
            if (tokens1.size() != 0) {
                List<String> tokens2 = getBigram(tokens1);
                String bigram = String.join(" " , tokens2);
                doc.add(new TextField("bigram", bigram, Field.Store.YES));
            }

            writer.addDocument(doc);
            counter++;
            if (counter % 50 == 0) {
                System.out.println("Commited: " + counter + " paragraphs so far.");
                writer.commit();
            }
        }

        writer.commit();
        writer.close();
    }

    public ArrayList<String> getBigram(List<String> tokens) throws IOException {

        // Make a list of bigram Strings
        ArrayList<String> bigrams = new ArrayList<>();
        for (int i = 0; i < tokens.size() - 1; i++) {
            String bigram = tokens.get(i) + tokens.get(i + 1);
            bigrams.add(bigram);
        }
        return bigrams;
    }

    public static void main (String [] args) throws IOException {
        String path = "/home/rachel/ir/test200/test200-train/train.pages.cbor-paragraphs.cbor";
        LuceneIndexer indexer  = new LuceneIndexer("bigramParagraphs"); // The directory that will be made
        indexer.doIndex(path);
    }


}
