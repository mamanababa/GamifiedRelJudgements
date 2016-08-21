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
import java.util.HashMap;
import java.util.List;
import org.apache.lucene.document.Document;
import retriever.TrecDocRetriever;
import trec.TRECQuery;

/**
 *
 * @author lizeyuan
 */
public class GameForP2 {

    String sessionId;
    String qid;    // the query id
    TRECQuery query;

//    String docIdToGuess;  // the doc id which the user needs to guess
    TrecDocRetriever retriever;

    int luceneDocIdToGuess;
    Document docToGuess; // guessed doc obj
    String contentOfDocToGuess;

//    int numTermsToShare; // number of terms to share in each round
    int score;
    boolean startState;
//    int numTermsShared;//number of terms already shared
    List<UserSubmitInfo> submitInfos;

    // Instantaneous state variables
    String lastDocumentSubmitted;
    String lastUserQuery;

    String wordsSharedNow; // words just shared to the player
    boolean judged = false;//doc submitted by P2 is judged by P1
    boolean correctGuess;
    boolean relGuess;
    int terminateCode;

    String username = "default";
    int winningRounds = 0;
    int totalRounds = 1;
    int totalSocreAfterthisRound = 0;

    // Termination Codes
    static final int GAME_TO_CONTINUE = 0;
    static final int CORRECT_GUESS_FOUND = 1;
    static final int SCORE_REACHED_MIN_THRESH = 2;
    static final int XXX = 3;
    static final int SAME_DOC_SUBMITTED = 4;

    // SCORE UPDATES...
    static final int INIT_SCORE = 10;
    static final int GAME_TERMINATION_SCORE = 0; // stop the game if this score is reached
    static final int SCORE_INCREMENET_FOR_CORRECT_GUESS = 20;
    static final int SCORE_INCREMENET_FOR_CORRECT_REL = 5;
    static final int SCORE_INCREMENET_FOR_INCORRECT_REL = -2;

    public GameForP2(TrecDocRetriever retriever, String sessionId) {
        this.sessionId = sessionId;
        this.retriever = retriever;
        startState = true;
    }

    public void updateState(int rel, int guessedDocId, String guessedDocName, String query) {
        lastUserQuery = query;
        lastDocumentSubmitted = guessedDocName;
        terminateCode = GAME_TO_CONTINUE;

        //correct
        if (rel == 0) {
            correctGuess = true;
            score += SCORE_INCREMENET_FOR_CORRECT_GUESS;
            terminateCode = CORRECT_GUESS_FOUND;
            winningRounds++;
            totalSocreAfterthisRound += score;
        }//relevant
        else if (rel == 1) {
            relGuess = true;
            score += SCORE_INCREMENET_FOR_CORRECT_REL;
        } //irrelevant 
        else if (rel == 2) {
            relGuess = false;
            score += SCORE_INCREMENET_FOR_INCORRECT_REL;
        } // if new game starting
        else if (this.startState) {
            score = INIT_SCORE;
        }

        //when SCORE =0 GAMEOVER
        if (score == GAME_TERMINATION_SCORE) { //if reach 0 score, game over
            terminateCode = SCORE_REACHED_MIN_THRESH;
            totalSocreAfterthisRound += score;
        } //P1 shares more words
        else {
//            submitInfos.add(new UserSubmitInfo(wordsSharedNow, guessedDocId, guessedDocName, relGuess));
        }
        saveGameState();

    }

    public void saveGameState() {
        try {
//            FileWriter fw = new FileWriter(logFileName, true);
            FileWriter fw = new FileWriter("/Users/lizeyuan/Google/prac/IR/GamifiedRelJudgements/userLogs2/" + username + ".txt", true);
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
                .append("");
        return buff.toString();
    }

    public void resumeGameState(String name) {
        this.username = name;
        BufferedReader reader = null;
        File folder;
        try {
            String lastRecord = "";
            folder = new File("/Users/lizeyuan/Google/prac/IR/GamifiedRelJudgements/userLogs2");
            File[] allLogs = folder.listFiles();
            // find user  all users logs
            for (File log : allLogs) {
                if (log.getName().equals(name + ".txt")) {
                    reader = new BufferedReader(new FileReader(log));
                    String currentRecord = "";
                    while ((currentRecord = reader.readLine()) != null) {
                        lastRecord = currentRecord;
                    }
                    String[] a = lastRecord.split(",");
//                    totalRounds = Integer.valueOf(a[0].split(":")[1]);
                    totalSocreAfterthisRound = Integer.valueOf(a[0].split(":")[1]);
                    winningRounds = Integer.valueOf(a[1].split(":")[1]);
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

    public String selectQuery() {
        //<query id, query>
        HashMap<String, TRECQuery> queryMap = retriever.getQueryMap();

        StringBuffer buff = new StringBuffer();
        buff
                .append("<script type=\"text/javascript\">\n"
                        + "	function select(element) { var contents = element.textContent;\n"
                        //                        + "                var url = \"TwoPlayersManager?docguessed=none&guessedid=-1&query=\" + contents;\n"
                        + "                var url = \"GameManagerServlet?gameVersion=2&docguessed=none&guessid=-1&query=\" + contents;\n"
                        + "                $.ajax({url: url,\n"
                        + "                    success: function (result) {\n"
                        + "                       $(\"#QDlg\").dialog(\"close\");\n"
                        + "                       $(\"#gamepanel\").html(contents);   "
                        + "                    }\n"
                        + "                });}\n"
                        + "	function mouseOn(element) { element.style.backgroundColor = \"gray\";}\n"
                        + "	function mouseOut(element) { element.style.backgroundColor = \"white\";}\n</script>")
                .append("<center><form><table border=\"1\"><tr><td>Query ID</td><td>Title</td></tr>");
        for (String id : queryMap.keySet()) {
            buff.append("<tr  onclick=\"select(this)\" onmouseover=\"mouseOn(this)\" onmouseout=\"mouseOut(this)\"><td>"
                    + id + "</td><td>" + queryMap.get(id).title + "</td></tr>");
        }
        buff.append("</table></center></form>");
        return buff.toString();
    }

    public String jsonP2() {
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

//        String msg = correctGuess
//                ? "You WIN!! You have guessed the document " + this.docIdToGuess + " correctly."
//                : relGuess
//                        ? "Congratulations!! You have hit a relevant document."
//                        : wordsSharedNow == query.title
//                                ? "Shared the query to start with."
//                                : "Oops!! Wrong guess and no hit on a relevant document.";
//        buff.append("\"msg\": \"").append(msg).append("\", ");
        buff.append("\"is2Player\": \"").append(is2Player).append("\"");
        buff.append("}");
        return buff.toString();
    }
}
