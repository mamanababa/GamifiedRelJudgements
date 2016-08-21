/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import game.GameForP2;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import retriever.TrecDocRetriever;
import trec.TRECQuery;

/**
 *
 * @author lizeyuan
 */
public class TwoPlayersManager extends HttpServlet {

    String propFileName;
    TrecDocRetriever retriever;

    static final String GAME_INFO_PARAM = "gameinfor";

    @Override
    public void init(ServletConfig config) throws ServletException {
        propFileName = config.getInitParameter("configFile");
        try {
            retriever = new TrecDocRetriever(propFileName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

        int guessedDocId = Integer.parseInt(request.getParameter("guessid"));
        int rel = Integer.parseInt(request.getParameter("rel"));
        String submittedDoc = request.getParameter("docguessed");
        String query = request.getParameter("query");

        GameForP2 gameP2 = new GameForP2(retriever, session.getId());
//        gameP2.updateState(rel, guessedDocId, submittedDoc, query);
        String json = gameP2.jsonP2();
        try (PrintWriter out = response.getWriter()) {
            out.println(json);
            System.out.println("P2 json: " + json);
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
