/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.BytesRef;
import retriever.TrecDocRetriever;
import static retriever.TrecDocRetriever.FIELD_ANALYZED_CONTENT;
import trec.TRECQuery;

/**
 *
 * @author Debasis
 */
// called in TermFreqComparator_Freq and loadTfVec()
class TermFreq implements Comparable<TermFreq> {

    Term term;
    String termStr;
    float tf;  // term freq, document component
    float wt;  // word freq

    public TermFreq(Term term, String termStr, int tf) {
        this.term = term;
        this.tf = tf;
        this.termStr = termStr;
    }

    @Override
    public int compareTo(TermFreq t) {
        // descending
        return -1 * new Float(wt).compareTo(t.wt);
    }

    @Override
    public String toString() {
        return "(" + termStr + ", " + wt + ")";
    }
}

//
class TermFreqComparator_Freq implements Comparator<TermFreq> {

    @Override
    public int compare(TermFreq a, TermFreq b) {
        Integer aLen = (int) a.tf;
        Integer bLen = (int) b.tf;
        return -1 * aLen.compareTo(bLen);  // decreasing
    }
}

//submitted information 
class UserSubmitInfo {

    String wordsShared;
    int luceneDocId; //searched doc id
    String docSubmitted;
    boolean relGuess;//if it's relevant 

    public UserSubmitInfo(String wordsShared, int luceneDocId, String docSubmitted, boolean relGuess) {
        this.wordsShared = wordsShared;
        this.docSubmitted = docSubmitted;
        this.luceneDocId = luceneDocId;
        this.relGuess = relGuess;
    }

    @Override
    public String toString() {
        String className = relGuess ? "rel" : "nrel";
        StringBuffer buff = new StringBuffer();
        buff.append("<td>");

        if (luceneDocId >= 0) {
            buff.append("<a id='")
                    .append(luceneDocId)
                    .append("' name='")
                    .append(docSubmitted)
                    .append("' class='")
                    .append(className)
                    .append("'>")
                    .append(docSubmitted)
                    .append("</a>");
        }

        buff.append("</td>")
                .append("<td>")
                .append(wordsShared)
                .append("</td>");

        return buff.toString();
    }
}

public class GameState {

    String sessionId;
    String qid;    // the query id
    TRECQuery query;
    long startingEpochs;

    String docIdToGuess;  // the doc id which the user needs to guess
    AllRelRcds rels;
    TrecDocRetriever retriever;

    Document docToGuess; // guessed doc obj
    int luceneDocIdToGuess;
    String contentOfDocToGuess;

    int numTermsToShare; // number of terms to share in each round

    int score;
    boolean startState;
    int numTermsShared;//number of terms already shared
    List<UserSubmitInfo> submitInfos;
    List<String> subDocs = new ArrayList<String>();

    // Instantaneous state variables
//    String lastDocumentSubmitted;
//    String lastUserQuery=" ";
    List<String> queryList = new ArrayList<String>();
    String wordsSharedNow; // words just shared to the player
    boolean correctGuess;
    boolean relGuess;
    int terminateCode;

    String username = "default";
    int winningRounds = 0;
    int totalRounds = 0;
    int totalSocreAfterthisRound = 0;
//    boolean roundFinished = false;
    Map<String, Boolean> subDocMap = new LinkedHashMap();
    

    String logFileName;

    List<TermFreq> tfvec;  // term freq vec of the doc to be guessed
    TermFreqComparator_Freq tfcomp_freq;

    //called in loadTfVec()
    static final float LAMBDA = 0.6f;
    static final float ONE_MINUS_LAMBDA = 1 - LAMBDA;

    // Termination Codes
    static final int GAME_TO_CONTINUE = 0;
    static final int CORRECT_GUESS_FOUND = 1;//game end
    static final int SCORE_REACHED_MIN_THRESH = 2;//game end
    static final int XXX = 3;
    static final int SAME_DOC_SUBMITTED = 4;

    // SCORE UPDATES...
    static final int INIT_SCORE = 10;
    static final int GAME_TERMINATION_SCORE = 0; // stop the game if this score is reached
    static final int SCORE_INCREMENET_FOR_CORRECT_GUESS = 20;
    static final int SCORE_INCREMENET_FOR_CORRECT_REL = 2;
    static final int SCORE_INCREMENET_FOR_INCORRECT_REL = -2;

    //constructor
    public GameState(TrecDocRetriever retriever, AllRelRcds rels, String sessionId) {
        this.sessionId = sessionId;
        this.retriever = retriever;
        this.rels = rels;//AllRelRcds
        startingEpochs = System.currentTimeMillis();
        logFileName = retriever.getProperties().getProperty("gamelog.file");

        // Pick one random query id
        qid = rels.selectRandomQuery();
        // Pick one random relevant document for this query
        docIdToGuess = rels.selectRandomRelDoc(qid);

        // Load the Lucene document object for the doc to be guessed...
        // for selecting random terms from this doc...
        try {
            loadDoc();//guessed doc obj,id and content
            loadTfVec();//??
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Properties prop = retriever.getProperties();
        numTermsToShare = Integer.parseInt(prop.getProperty("game.numterms", "3"));//in each round
        //get query by the ramdom query id
        this.query = retriever.getQuery(qid);

        numTermsShared = 0;
        startState = true;

        tfcomp_freq = new TermFreqComparator_Freq();
        submitInfos = new ArrayList<>();
    }//end of constructor

    //
    void loadTfVec() throws Exception {
        IndexReader reader = retriever.getReader();
        long sumDf = reader.getSumDocFreq(TrecDocRetriever.FIELD_ANALYZED_CONTENT);
        Terms terms = reader.getTermVector(luceneDocIdToGuess, FIELD_ANALYZED_CONTENT);
        if (terms == null || terms.size() == 0) {
            return;
        }

        TermsEnum termsEnum;
        BytesRef term;
        tfvec = new ArrayList<>();

        // Construct the normalized tf vector
        termsEnum = terms.iterator(null); // access the terms for this field
        int doclen = 0;
        while ((term = termsEnum.next()) != null) { // explore the terms for this field
            String termStr = term.utf8ToString();
            String stem = retriever.analyze(termStr);
            DocsEnum docsEnum = termsEnum.docs(null, null); // enumerate through documents, in this case only one
            while (docsEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
                //get the term frequency in the document
                int tf = docsEnum.freq();
                TermFreq tfq = new TermFreq(
                        new Term(TrecDocRetriever.FIELD_ANALYZED_CONTENT, term),
                        termStr,
                        tf);
                tfvec.add(tfq);
                doclen += tf;
            }
        }
        for (TermFreq tf : tfvec) {
            tf.tf = tf.tf / (float) doclen; // normalize by len
            float idf = sumDf / reader.docFreq(tf.term);
            tf.wt = (float) (Math.log(1 + LAMBDA / (ONE_MINUS_LAMBDA) * tf.tf * idf));
        }
        Collections.sort(tfvec);
    }

    //load guessed doc id, obj and content
    void loadDoc() throws Exception {
        IndexReader reader = retriever.getReader();
        IndexSearcher searcher = retriever.getSearcher();

        Term docIdTerm = new Term(TrecDocRetriever.FIELD_ID, this.docIdToGuess);
        TermQuery tq = new TermQuery(docIdTerm);

        TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
        searcher.search(tq, collector);
        this.luceneDocIdToGuess = collector.topDocs().scoreDocs[0].doc;
        this.docToGuess = reader.document(luceneDocIdToGuess);
        this.contentOfDocToGuess = docToGuess.get(FIELD_ANALYZED_CONTENT);
    }

    // take the most frequent destem
    String deStemWord(String word) {
        Pattern p = Pattern.compile(word + "\\S*");
        Matcher matcher = p.matcher(contentOfDocToGuess);

        if (matcher.find()) {
            String matched = matcher.group();
            int len = matched.length();
            char lastChar = matched.charAt(len - 1);
            if (!Character.isLetter(lastChar)) {
                matched = matched.substring(0, len - 1);
            }
            return matched;
        }
        return word;
    }

    public void selectWords() {
        if (startState) {
            startState = false;
            wordsSharedNow = query.title;
            return;
        }

        StringBuffer buff = new StringBuffer();
        int start = numTermsShared;
        int end = Math.min(start + numTermsToShare, tfvec.size());

        if (start >= end) {
            wordsSharedNow = "No terms left to share!";
            return;
        }

        for (int i = start; i < end; i++) {
            String stemmedWord = tfvec.get(i).termStr;
            String deStemmed = deStemWord(stemmedWord); // take the most frequent destem
            buff.append(deStemmed).append(" ");
        }

        numTermsShared = end;
        wordsSharedNow = buff.toString();
    }

    public String getDocToGuess() {
        return this.docIdToGuess;
    }

    //save game state to log file after every submit
    public void logGameState() {
        try {
//            FileWriter fw = new FileWriter(logFileName, true);
            FileWriter fw = new FileWriter("/Users/lizeyuan/Google/prac/IR/GamifiedRelJudgements/userLogs1/" + username + ".txt", true);
            synchronized (this) {
                // Save the game state
                fw.write(this.toString() + "\n");
            }
            fw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff
                .append("\nRound:" + totalRounds) //0
                .append(";")
                .append("WinningRounds:" + winningRounds) //1
                .append(";")
                .append("TotalScore:" + totalSocreAfterthisRound) //2
                .append(";")
                .append("RoundSocre:" + this.score) //3
                .append(";")
                .append("CorrectDOC:" + this.docIdToGuess) //4
                .append(";")
                .append("SubmittedDOCs:");
        for (String doc : subDocMap.keySet()) {
            buff.append(doc + ",");         //5
        }
        buff
                .append(";")
                .append("IsRelevant:");
        for (String doc : subDocMap.keySet()) {
            buff.append(subDocMap.get(doc) + ",");  //6
        }
        buff
                .append(";")
                .append("QueriesExecuted:");
        for (String q : queryList) {
            buff.append(q + "&");       //7
        }

        //                .append("sessionID:" + this.sessionId)
        //                .append("; ")
        //                .append("startingEpochs:" + this.startingEpochs)
        //                .append(", ")
        //                .append("User:" + this.username)
        //                .append(";")
        //                .append("\n\nRound:" + totalRounds) //0
//                .append(" ; ")
//                .append("winningRounds:" + winningRounds) //1
//                .append(" ; ")
//                .append("totalScore:" + totalSocreAfterthisRound) //2
//                .append(" ; ")
//                .append("queryID:" + this.qid) //3
//                .append(" ; ")
//                .append("correctDOC:" + this.docIdToGuess) //4
//                .append(" ; ")
//                .append("SubmittedDOC:" + this.lastDocumentSubmitted) //5
//                .append(" ; ")
//                .append("wordsSharedNow:" + this.wordsSharedNow) //6
//                .append(" ; ")
//                .append("lastQuery:" + this.lastUserQuery) //7
//                .append(" ; ")
//                .append("isRelevant:" + this.relGuess) //8
//                .append(" ; ")
//                .append("isCorrect:" + this.correctGuess) //9
//                .append(" ; ")
//                .append("roundFinished:" + this.roundFinished) //10
//                .append(" ; ")
//                .append("score:" + this.score); //11
        return buff.toString();
    }
    // Update the game state based on user move... 
    // User submits a document as a guess...
    //    public void update(int guessedDocId, String guessedDocName, String query) {

    public void update(int guessedDocId, String guessedDocName, String query, String name) {
        //if a new user
        if (!name.equals(username)) {
            username = name;
        }

//        lastUserQuery += query;
        if (query != null && !queryList.contains(query)) {
            queryList.add(query);
        }
//        lastDocumentSubmitted = guessedDocName;
        terminateCode = GAME_TO_CONTINUE;
        if (subDocs.contains(guessedDocName)) {
            terminateCode = SAME_DOC_SUBMITTED;
            return;
        } else if (guessedDocName.equalsIgnoreCase(docIdToGuess)) {// if guessed doc is correct, game over
            correctGuess = true;
            relGuess = true;
            score += SCORE_INCREMENET_FOR_CORRECT_GUESS;
            terminateCode = CORRECT_GUESS_FOUND;
            winningRounds++;
            totalSocreAfterthisRound += score;

        } else if (this.rels.isRel(qid, guessedDocName)) {// if guessed doc is relevant 
            relGuess = true;
            score += SCORE_INCREMENET_FOR_CORRECT_REL;

        } else if (this.startState) {// if new game starting
            score = INIT_SCORE;

        } else {//if unrelevant doc 
            relGuess = false;
            score += SCORE_INCREMENET_FOR_INCORRECT_REL;
        }
        subDocs.add(guessedDocName);

        if (score <= GAME_TERMINATION_SCORE) { //if reach 0 score, game over
            terminateCode = SCORE_REACHED_MIN_THRESH;
            totalSocreAfterthisRound += score;
        } else {
            // Got to share words with the player...
            selectWords();
            submitInfos.add(new UserSubmitInfo(wordsSharedNow, guessedDocId, guessedDocName, relGuess));
        }

        if (guessedDocName != null && guessedDocName != "none" && !guessedDocName.equals(null) && !guessedDocName.equals("none")) {
            subDocMap.put(guessedDocName, relGuess);
        }

        //save game state after finishing a round
        if (terminateCode == CORRECT_GUESS_FOUND || terminateCode == SCORE_REACHED_MIN_THRESH) {
//            roundFinished = true;
            totalRounds++;
            logGameState();
        } else {

        }
    }

    public void resumeUser(String name) {
        BufferedReader reader = null;
        File folder;
        try {
            String lastRecord = "";
            folder = new File("/Users/lizeyuan/Google/prac/IR/GamifiedRelJudgements/userLogs1");
            File[] allLogs = folder.listFiles();
            // find user  all users logs
            for (File log : allLogs) {
                if (log.getName().equals(name + ".txt")) {
                    reader = new BufferedReader(new FileReader(log));
                    String currentRecord;
                    while ((currentRecord = reader.readLine()) != null) {
                        lastRecord = currentRecord;
                    }

                    String[] a = lastRecord.split(";");
                    System.out.println("read last line for resumeUser: " + lastRecord + "\nsize of string: " + a.length);
                    totalRounds = Integer.valueOf(a[0].split(":")[1].trim());
                    totalSocreAfterthisRound = Integer.valueOf(a[2].split(":")[1].trim());
                    winningRounds = Integer.valueOf(a[1].split(":")[1].trim());
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

//called in GameManagerServlet for single player
    public String buildJSON() {
        StringBuffer buff = new StringBuffer("{");
        boolean is2Player = false;
        buff.append("\"score\": \"").append(score).append("\", ");

        /* Return all the words everytime so that the client side
           needn't do anything else but to set the HTML...
         */
        buff.append("\"words\": \"");
        buff.append("<table>");
        for (UserSubmitInfo submitInfo : this.submitInfos) {
            buff
                    .append("<tr>")
                    .append(submitInfo)
                    .append("</tr>");
        }
        buff.append("</table>");
        buff.append("\", ");

        buff.append("\"terminate\": ").append(terminateCode).append(", ");

        String msg = correctGuess
                ? "You WIN!! You have guessed the document " + this.docIdToGuess + " correctly."
                : relGuess
                        ? "Congratulations!! You have hit a relevant document."
                        : wordsSharedNow == query.title
                                ? "Shared the query to start with."
                                : "Oops!! Wrong guess and no hit on a relevant document.";
        buff.append("\"msg\": \"").append(msg).append("\", ");
        buff.append("\"is2Player\": \"").append(is2Player).append("\"");
        buff.append("}");
        return buff.toString();
    }
}
