/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author lizeyuan
 */
public class LeaderBoard {

    String username;

    public LeaderBoard(String name) {
        this.username = name;
    }

    //read all users' log file then create leader board
    public TreeMap[] updateLeaderB() {
        BufferedReader reader = null;
        File folder;
        HashMap<String, Integer> unsortedPlayers = new HashMap<String, Integer>();
        TreeMap<String, Integer> playersRounds = new TreeMap<String, Integer>();
        TreeMap<String, Integer> sortedplayers = new TreeMap<String, Integer>(new Compare(unsortedPlayers));
        TreeMap[] players = new TreeMap[2];
        try {
            int winningRounds = 0;
            int totalScores = 0;
            String lastRecord = "";
            folder = new File("/Users/lizeyuan/Google/prac/IR/GamifiedRelJudgements/userLogs1");
            File[] allLogs = folder.listFiles();
            // read all users logs
            for (File log : allLogs) {
                if (log.isFile() && log.getName().endsWith(".txt")) {
                    reader = new BufferedReader(new FileReader(log));
                    String currentRecord = "";
                    while ((currentRecord = reader.readLine()) != null) {
                        lastRecord = currentRecord;
                    }
                    String[] a = lastRecord.split(";");
                    System.out.println("read last line for leaderBoard: " + lastRecord + "\nsize of string: " + a.length);
                    totalScores = Integer.valueOf(a[2].split(":")[1].trim());
                    winningRounds = Integer.valueOf(a[1].split(":")[1].trim());
                    // username and socres
                    String name = log.getName().replace(".txt", "");
                    unsortedPlayers.put(name, totalScores);
                    playersRounds.put(name, winningRounds);
                }
            }
            sortedplayers.putAll(unsortedPlayers);
            players[0] = sortedplayers;
            players[1] = playersRounds;
//            System.out.println(players);
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
        return players;
    }//end of readLog()

    //for profile.jsp, information of each round
    public String history() {
        BufferedReader reader = null;
        StringBuffer buff = new StringBuffer("");
        File folder;
        double totalRounds = 0;
        double winningRounds = 0;
        int totalSocreAfterthisRound = 0;
//        ArrayList<Integer> roundSocre = new ArrayList<Integer>();
        double winningRate = 0;

        boolean found = false;
        try {
            ArrayList<String> allRounds = new ArrayList<String>();
            folder = new File("/Users/lizeyuan/Google/prac/IR/GamifiedRelJudgements/userLogs1");
            File[] allLogs = folder.listFiles();
            // find user  all users logs
            for (File log : allLogs) {
                if (log.getName().equals(username + ".txt")) {
                    found = true;
                    reader = new BufferedReader(new FileReader(log));
                    String currentRecord;
                    while ((currentRecord = reader.readLine()) != null) {
                        if (currentRecord.length() > 1) {
                            allRounds.add(currentRecord);
                        }
                    }
//                    System.out.print("records size: " + allRounds.size());
                    String[] b = allRounds.get(allRounds.size() - 1).split(";");
                    totalRounds = Double.valueOf(b[0].split(":")[1].trim());
                    winningRounds = Double.valueOf(b[1].split(":")[1].trim());
                    totalSocreAfterthisRound = Integer.valueOf(b[2].split(":")[1].trim());
                    winningRate = winningRounds / totalRounds * 100;
//                    System.out.print("WINNING RATE = " + winningRounds + "/" + totalRounds + "=" + winningRate);

                    //table 1
                    buff.append("<table border=\"1\">"
                            + "<tr><th>TOTAL ROUNDs</th><th>TOTAL SOCRE</th><th>WINNING ROUNDs</th><th>WINNING RATE</th></tr>"
                            + "<tr><td>" + (int) totalRounds + "</td><td>" + totalSocreAfterthisRound + "</td><td>" + (int) winningRounds + "</td><td>" + winningRate + "%</td></tr>"
                            + "</table><br><br>");

                    //table 2
                    buff.append("<table border=\"1\">"
                            + "<tr><th>ROUND</th><th>SOCRE</th><th>QUERIES EXCUTED</th><th>NUMBER OF SUBMITTED DOC</th><th>RELEVENT DOCS FOUND</th></tr>");

                    for (int i = 1; i <= allRounds.size(); i++) {
                        String[] a = allRounds.get(i - 1).split(";");

                        int roundSocre = Integer.valueOf(a[3].split(":")[1].trim());
                        String roundDoc = a[4].split(":")[1];
                        String[] subDocs = a[5].split(":")[1].split(",");
                        String[] isRel = a[6].split(":")[1].split(",");
                        String[] roundQ = a[7].split(":")[1].split("&");

                        Map<String, String> subDocMap = new HashMap();
                        for (int k = 0; k < subDocs.length; k++) {
                            if (subDocs[k].length() > 1 && isRel[k].length() > 1) {
                                subDocMap.put(subDocs[k], isRel[k]);
                            }
                        }

                        buff.append("<tr><td>" + i + "</td><td>" + roundSocre + "</td><td>");
//                        System.out.print("roundQ: ");
                        for (int k = 0; k < roundQ.length; k++) {
//                            System.out.print(roundQ[k]);
                            if (roundQ[k].length() > 1) {
                                buff.append((k + 1) + "." + roundQ[k] + "<br>");
                            }
                        }
                        buff.append("</td><td>" + (subDocs.length - 1) + "</td><td>");
                        for (String docID : subDocMap.keySet()) {
                            if (subDocMap.get(docID).trim().equals("true")) {
                                buff.append(docID + "<br>");
                            }
                        }
                    }//end of for 
                    buff.append("</td></tr></table>");
                    break;
                }//end of if find the player file 
            }//end of for files
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
            if (!found) {
                return "you havent played yet";
            } else {
                return buff.toString();
            }
        }
    }

}

class Compare implements Comparator<String> {

    Map<String, Integer> name;

    public Compare(Map<String, Integer> name) {
        this.name = name;
    }

    public int compare(String a, String b) {
        if (name.get(a) >= name.get(b)) {
            return -1;
        } else {
            return 1;
        }
    }
}
