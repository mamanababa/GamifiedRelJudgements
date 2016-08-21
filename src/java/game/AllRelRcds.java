/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Debasis
 */
// 2 hash maps 
// <String doc id , isRelavant> relMap in PerQueryRelDocs class
// <String query id, PerQueryRelDocs obj> perQueryRels
//relevant docs for each query
class PerQueryRelDocs {

    String qid;//query id 
    //<doc id, isRelavant>
    HashMap<String, Integer> relMap;
    //relevant  docs number for this query
    int numRel;

    //constructor 
    public PerQueryRelDocs(String qid) {
        this.qid = qid;
        numRel = 0;
        relMap = new HashMap<>();
    }

    void addTuple(String docId, int rel) {
        if (relMap.get(docId) != null) {
            return;
        }
        //if there's relevant docs , put in relevance map
        if (rel > 0) {
            numRel++;
            relMap.put(docId, rel);
        }
    }
}//end of PerQueryRelDocs

public class AllRelRcds {

    String qrelsFile;
    //query map`
    //<query id, relevant docs obj>
    HashMap<String, PerQueryRelDocs> perQueryRels;
    int totalNumRel;

    public AllRelRcds(String qrelsFile) {
        this.qrelsFile = qrelsFile;
        perQueryRels = new HashMap<>();
        totalNumRel = 0;
        try {
            //read queries file
            load();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //return a radom query id
    public String selectRandomQuery() {
        String qid = null;
        int randIndex = (int) (Math.random() * perQueryRels.size());
        int i = -1;
        //get an random query id from the query,docs obj hashmap
        for (Map.Entry<String, PerQueryRelDocs> e : perQueryRels.entrySet()) {
            i++;
            qid = e.getKey();
            if (i >= randIndex) {
                break;
            }
        }
        return qid;
    }

    //return a random doc id by a query id 
    public String selectRandomRelDoc(String qid) {
        String docId = null;

        PerQueryRelDocs reldocs = perQueryRels.get(qid);
        int randIndex = (int) (Math.random() * reldocs.numRel);
        int i = -1;
        //get an random doc id from the doc,relevant value hashmap
        for (Map.Entry<String, Integer> rel : reldocs.relMap.entrySet()) {
            i++;
            docId = rel.getKey();
            if (i >= randIndex) {
                break;
            }
        }
        return docId;
    }

    //how many PerQueryRelDocs obj in the <query,docs obj> hashmap
    int getTotalNumRel() {
        if (totalNumRel > 0) {
            return totalNumRel;
        }
        for (Map.Entry<String, PerQueryRelDocs> e : perQueryRels.entrySet()) {
            PerQueryRelDocs perQryRelDocs = e.getValue();
            totalNumRel += perQryRelDocs.numRel;
        }
        return totalNumRel;
    }

    //read queries file 
    private void load() throws Exception {
        FileReader fr = new FileReader(qrelsFile);
        BufferedReader br = new BufferedReader(fr);
        String line;

        while ((line = br.readLine()) != null) {
            storeRelRcd(line);
        }
        br.close();
        fr.close();
    }

    //sotre doc into <query id, relevant docs obj> perQueryRels hashmap
    private void storeRelRcd(String line) {
        //4 tokens of each line read from TREC-8 qrels file
        //TOPIC      ITERATION      DOCUMENT#      RELEVANCY 
        String[] tokens = line.split("\\s+");
        //tokens : query id, doc id, relavent value
        String qid = tokens[0];
//        for (int i = 0; i < tokens.length; i++) {
//            System.out.println("toktens:" + i + ":" + tokens[i]);
//        }
        //get values(PerQueryRelDocs obj) from perQueryRels hashmap by query id 
        PerQueryRelDocs relTuple = perQueryRels.get(qid);
        if (relTuple == null) {
            relTuple = new PerQueryRelDocs(qid);
            perQueryRels.put(qid, relTuple);
        }
        //(doc id, isRelavant)
        relTuple.addTuple(tokens[2], Integer.parseInt(tokens[3]));
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        for (Map.Entry<String, PerQueryRelDocs> e : perQueryRels.entrySet()) {
            PerQueryRelDocs perQryRelDocs = e.getValue();
            buff.append(e.getKey()).append("\n");
            for (Map.Entry<String, Integer> rel : perQryRelDocs.relMap.entrySet()) {
                String docName = rel.getKey();
                int relVal = rel.getValue();
                buff.append(docName).append(",").append(relVal).append("\t");
            }
            buff.append("\n");
        }
        return buff.toString();
    }

    //get PerQueryRelDocs obj by query id 
    PerQueryRelDocs getRelInfo(String qid) {
        return perQueryRels.get(qid);
    }

    //get doc obj by query id, then get relevant value by doc id 
    boolean isRel(String qid, String docId) {
        PerQueryRelDocs relDocs = perQueryRels.get(qid);
        Integer rel = relDocs.relMap.get(docId);
        return rel == null ? false : true;
    }
}
