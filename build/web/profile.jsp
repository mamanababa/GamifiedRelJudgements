<%-- 
    Document   : profile
    Created on : Aug 01, 2016, 9:42:22 PM
    Author     : lizeyuan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import = "java.util.*"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Profile</title>
        <!--profile contents playing history
                total points
                total round
                winning rounds
                winning rate
        
                in each round
                    score
                    shared query
                    how many doc submitted
                    which doc submitted whether relevant
        -->
        <link href="jquery/themes/ui-lightness/jquery-ui.css" rel="stylesheet">

        <script src="jquery/jquery-2.1.4.js"></script>
        <script src="jquery/jquery-ui.js"></script>
        <script src="jquery/spin.js"></script>
        <script src="jquery/jquery.twbsPagination.js" type="text/javascript"></script>
        <script>
            $(document).ready(function () {
//                $("#status").html("bgin");

//                $("#history").button().click(userHistory);
//                function userHistory() {
                var url = "LeadBo?flag=0&username=<%=request.getParameter("username")%>";
                $.ajax({url: url,
                    success: function (result) {
                        $("#status").html(result);
                    }
                });//end of ajax
//                }
            });
        </script>
    </head>
    <body>
        <h1>Hello <%=request.getParameter("username")%></h1>
        <!--<button id="history">History</button>--> 
        <div id="status"></div>
    </body>
</html>
