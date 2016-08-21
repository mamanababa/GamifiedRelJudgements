/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import game.AllRelRcds;
import game.GameForP2;
import game.GameState;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.lucene.index.IndexReader;
import retriever.TrecDocRetriever;

/**
 *
 * @author Debasis
 */
public class GameManagerServlet extends HttpServlet {

    String propFileName;
    TrecDocRetriever retriever;
    AllRelRcds rels;
    IndexReader reader;
    static String username;

    static final String GAME_INFO_PARAM_NAME = "gameinfo";

    @Override
    public void init(ServletConfig config) throws ServletException {
        propFileName = config.getInitParameter("configFile");
        try {
            retriever = new TrecDocRetriever(propFileName);
            Properties prop = retriever.getProperties();
            rels = new AllRelRcds(prop.getProperty("qrels.file"));
            reader = retriever.getReader();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //
    GameState initiateNewGame1(HttpSession session) {
        session.removeAttribute(GAME_INFO_PARAM_NAME); // terms already shared
        GameState gameState = new GameState(retriever, rels, session.getId());
        session.setAttribute(GAME_INFO_PARAM_NAME, gameState);
        return gameState;
    }

    GameForP2 initiateNewGame2(HttpSession session) {
        session.removeAttribute(GAME_INFO_PARAM_NAME); // terms already shared
        GameForP2 gameP2 = new GameForP2(retriever, session.getId());
        session.setAttribute(GAME_INFO_PARAM_NAME, gameP2);
        return gameP2;
    }

    //
    protected boolean isGameOver(GameState gameState, String submittedDoc) {
        if (submittedDoc.equalsIgnoreCase(gameState.getDocToGuess())) {
            // user guessed correctly
            return true;
        }
        return false;
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession();
        int gameVersion = Integer.parseInt(request.getParameter("gameVersion"));
        int guessedDocId = Integer.parseInt(request.getParameter("guessid"));
        String submittedDoc = request.getParameter("docguessed");
        String query = request.getParameter("query");
        String newUsername = request.getParameter("username");
        if (newUsername != null) {
            username = newUsername;
        }

        if (submittedDoc.equals("none")) {
            // start a new game... delete the previous one saved in session
            session.removeAttribute(GAME_INFO_PARAM_NAME);
        }

        if (gameVersion == 1) {
            GameState gameState = (GameState) session.getAttribute(GAME_INFO_PARAM_NAME);
            if (gameState == null) {
                gameState = initiateNewGame1(session);
            }
            gameState.resumeUser(username);
            //Update the game state based on user move
//        gameState.update(guessedDocId, submittedDoc, query);
            gameState.update(guessedDocId, submittedDoc, query, username);

            // Construct a JSON response to send out to the client...
            String json = gameState.buildJSON();
            try (PrintWriter out = response.getWriter()) {
                out.println(json);
                System.out.println("json response: " + json);
            }
        } //2players version
        else if (gameVersion == 2) {
            GameForP2 gameP2 = (GameForP2) session.getAttribute(GAME_INFO_PARAM_NAME);
            if (gameP2 == null) {
                gameP2 = initiateNewGame2(session);
            }
            
//            gameP2.resumeGameState(username);
           
            String allQueries = gameP2.selectQuery();
            try (PrintWriter out = response.getWriter()) {
                System.out.println(username + ": 2 players version");
                out.println(allQueries);
            }

            //get P1 selected query 
            String querySelected;
            querySelected = request.getParameter("query");
            if (querySelected != null && !querySelected.equals("none")) {
                int queryId = Integer.valueOf(querySelected.substring(0, 3));
                String queryTitle = querySelected.substring(4);
//                System.out.println(querySelected);
                System.out.println("queryId: " + queryId + ",queryTitle: " + queryTitle);
            }
        } else {
            System.out.println("cannot get game version");
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
