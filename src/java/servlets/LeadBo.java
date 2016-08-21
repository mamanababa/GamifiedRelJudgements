/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import game.LeaderBoard;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author lizeyuan
 */
public class LeadBo extends HttpServlet {

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

        int flag = Integer.valueOf(request.getParameter("flag"));
        String username = request.getParameter("username");
        
        LeaderBoard lb = new LeaderBoard(username);
        System.out.print("username in LeaderB: " + username);

        //profile
        if (flag == 0) {
            try (PrintWriter out = response.getWriter()) {
                out.println(lb.history());
//                System.out.println("history: " + lb.history());
            }
        } else {//leader board
            TreeMap[] playerMaps = lb.updateLeaderB();
            TreeMap<String, Integer> players = playerMaps[0];
            TreeMap<String, Integer> playersRounds = playerMaps[1];

            try (PrintWriter out = response.getWriter()) {

                /* TODO output your page here. You may use following sample code. */
                out.println("<script src=\"jquery/sorttable.js\"></script>"
                        + "<table align=\"left\">"
                        + "<th>RANK</th>");
                for (int k = 1; k <= players.size(); k++) {
                    out.println("<tr><td>" + k + "</td></tr>");
                }
                out.println("</table>"
                        + "<table class=\"sortable\">"
                        + "<tr>"
                        + "<th class=\"sorttable_nosort\">NAME</th>"
                        + "<th>SOCRE</th>"
                        + "<th>WINNING ROUNDS</th>"
                        + "</tr>");
                int i = 0;
                for (String name : players.keySet()) {
                    if (username.trim().toLowerCase().equals(name.trim().toLowerCase())) {
                        out.println("<tr  bgcolor='yellow'><td>" + name + " <- YOU ARE HERE");
//                        System.out.print(username + "," + name);
                    } else {
                        out.println("<tr><td>" + name);
                    }
                    out.println("</td><td>"
                            + players.values().toArray()[i] + "</td><td>"
                            + playersRounds.get(name) + "</td></tr>");
                    i++;
                }//end of for
                out.println("</table>");

            }//end of try
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
