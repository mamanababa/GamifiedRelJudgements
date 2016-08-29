<%-- 
    Document   : index
    Created on : 30-Nov-2015, 15:38:59
    Author     : Debasis
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Document Miner</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <!--link href="http://fonts.googleapis.com/css?family=Open+Sans&subset=latin,cyrillic" rel="stylesheet"
              type="text/css"-->
        <!--link href="http://netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap.min.css" rel="stylesheet">
        <link href='https://fonts.googleapis.com/css?family=Architects+Daughter' rel='stylesheet' type='text/css'-->
        <!--link rel="stylesheet" type="text/css" href="css/stylesheet.css" media="screen"/>
        <link rel="stylesheet" type="text/css" href="css/pygment_trac.css" media="screen"/>
        <link rel="stylesheet" type="text/css" href="css/print.css" media="print"-->
        <link rel="stylesheet" href="css/uilayout.css" />
        <link rel="stylesheet" href="css/displayres.css" />
        <link href="jquery/themes/ui-lightness/jquery-ui.css" rel="stylesheet">

        <script src="jquery/jquery-2.1.4.js"></script>
        <script src="jquery/jquery-ui.js"></script>
        <script type="text/javascript" src="jquery/jquery.layout.js"></script>

        <script>
            //+++Start: Layout code
            var myLayout; // a var is required because this page utilizes: myLayout.allowOverflow() method
            var is2Players = false;
            var name;
            $(document).ready(function () {
                myLayout = $('body').layout({
                    west__size: 500 /* use this to re-size the west pane */
                    , west__spacing_closed: 20
                    , west__togglerLength_closed: 100
                    , west__togglerAlign_closed: "top"
                    , west__togglerContent_closed: "M<BR>E<BR>N<BR>U"
                    , west__togglerTip_closed: "Open & Pin Menu"
                    , west__sliderTip: "Slide Open Menu"
                    , west__slideTrigger_open: "mouseover"
                    , center__maskContents: true // IMPORTANT - enable iframe masking
                });
                //$("#id")
                $("#newgameBttn").button().click(checkLoginState);
                $("#newgameBttn2").button().click(checkLoginState2);
                $("#profileBttn").button().click(userProfile);
                $("#leadingBttn").button().click(leadingB);
                $("#tasksBttn").button().click(tasks);
                $("#helpBttn").button().click(help);
                $("#accordion").accordion({
                    heightStyle: "content"
                }
                );
//                checkLoginState();
//                newGame();
            });
            //---End: Layout code

            //single player
            function newGame() {
                var url = "GameManagerServlet?gameVersion=1&docguessed=none&guessid=-1&username=" + name;
                $("#version").html("Single player version selected");

//                document.getElementById('gameVersion').value = 1;
//                $.ajax({url: 'search.jsp',
//                    type: 'post',
//                    data: {gameVersion: "1"},
//                    success: function () {
//                        alert("posted,1");
//                    }});
                document.getElementById("mainFrame").src = "search.jsp";

                $.ajax({url: url,
                    success: function (jsonResponse) {
                        var jsonObj = jQuery.parseJSON(jsonResponse);
                        var score = jsonObj.score;
                        var words = jsonObj.words;
                        var msg = jsonObj.msg;
                        var terminate = jsonObj.terminate;
                        $("#scoreboard").html(score);
                        $("#msgboard").html(msg);
                        $("#gamepanel").html(words);
                        $("#accordion").accordion("option", "active", 2);
                    }
                });
            }

            //two players
            function newGame2() {
                var url = "GameManagerServlet?gameVersion=2&docguessed=none&guessid=-1&query=none&username=" + name;
                $("#version").html("Two players version selected");

//                document.getElementById('gameVersion').value = 2;
//                $.ajax({url: 'search.jsp',
//                    type: 'post',
//                    data: {gameVersion: "2"},
//                    success: function () {
//                        alert("posted,2");
//                    }});
                document.getElementById("mainFrame").src = "search.jsp";

                $.ajax({url: url,
                    success: function (result) {
//                        var jsonObj = jQuery.parseJSON(jsonResponse);
                        $("#QDlg").html(result);
                        var w = 0.5 * $(window).width();
                        var h = 0.8 * $(window).height();
                        $("#QDlg").dialog({
                            resizable: false,
                            width: w,
                            height: h,
                            modal: true,
                            buttons: {
                                "CLOSE": function () {
                                    $(this).dialog("close");
                                }
                            }
                        });
                    }//end of sucess
                });
            }
//
//            function select(element) {
//                var contents = element.textContent.toString();
//                var url = "TwoPlayersManager?query=" + contents;
//                $.ajax({url: url,
//                    success: function (result) {
//                        $("#QDlg").dialog("close");
//                    }//end of sucess
//                });
//            }

            //record each player's history
            function userProfile() {
                document.getElementById('username').value = name;
//                document.getElementById("mainFrame").src = "profile.jsp";
            }
            //record each player's records
            function leadingB() {
                var url = "LeadBo?flag=1&username=" + name;
                $.ajax({url: url,
                    success: function (result) {
                        $("#leadDlg").html(result);
                        var w = 0.6 * $(window).width();
                        var h = 0.5 * $(window).height();
                        $("#leadDlg").dialog({
                            resizable: false,
                            width: w,
                            height: h,
                            modal: true,
                            buttons: {
                                "CLOSE": function () {
                                    $(this).dialog("close");
                                }
                            }
                        });//end of dialog
                    }
                });//end of ajax
            }

            function tasks() {
                var w = 0.5 * $(window).width();
                var h = 0.5 * $(window).height();
                $("#tasksDlg").dialog({
                    resizable: false,
                    width: w,
                    height: h,
                    modal: true,
                    buttons: {
                        "CLOSE": function () {
                            $(this).dialog("close");
                        }
                    }
                });
            }

            function help() {
                var w = 0.5 * $(window).width();
                var h = 0.5 * $(window).height();
                $("#helpDlg").html("1. Each new game start with 10 socres and shared terms. <br> " +
                        "2. Use terms as keywords to search documents.<br> " +
                        "3. Take a guess which document is the correct one then submit<br>" +
                        "4. If the guessed document is correct you gains 10 points and win this round<br>"+ 
                        "5. If the guessed document is incorrect but relevant to the correct ducument, you gains 2 points and more shared terms<br> " + 
                        "6. If the guessed document is incorrect but irrelevant to the correct ducument, you lose 2 points and more shared terms <br>" +
                        "7. Lose the game when your score reach 0");
                $("#helpDlg").dialog({
                    resizable: false,
                    width: w,
                    height: h,
                    modal: true,
                    buttons: {
                        "CLOSE": function () {
                            $(this).dialog("close");
                        }
                    }
                });
            }

            // initialize facebook api sdk
            window.fbAsyncInit = function () {
                FB.init({
                    appId: '1721284351464016',
                    cookie: true,
                    xfbml: true,
                    version: 'v2.6'
                });
                // check Login Status
                FB.getLoginStatus(function (response) {
                    statusChangeCallback(response);
                });
            };
            // load the sdk asynchronously
            (function (d, s, id) {
                var js, fjs = d.getElementsByTagName(s)[0];
                if (d.getElementById(id))
                    return;
                js = d.createElement(s);
                js.id = id;
                js.src = "//connect.facebook.net/en_US/sdk.js";
                fjs.parentNode.insertBefore(js, fjs);
            }(document, 'script', 'facebook-jssdk'));


            // check 3 statues from FB.getLoginStatus().
            function statusChangeCallback(response) {
                console.log('statusChangeCallback');
                console.log(response);
                // response object is returned with a login status
                if (response.status === 'connected') {
                    // Logged into the game and Facebook.
                    gameLogin();
                } else if (response.status === 'not_authorized') {
                    // logged into Facebook, but not the game.
                    document.getElementById('status').innerHTML = 'Please log ' + 'into this game.';
                    $("#leadDlg").html("please login to game");
                    $("#tasksDlg").html("please login to game");
                } else {
                    // The person is not logged into Facebook, can only play the game but cannot be recorded 
                    document.getElementById('status').innerHTML = 'Please log ' + 'into Facebook.';
                    $("#leadDlg").html("please login to facebook");
                    $("#tasksDlg").html("please login to facebook");
                }
            }

            // called when someone finishes with the Login
            function checkLoginState() {
                is2Players = false;
                FB.getLoginStatus(function (response) {
                    statusChangeCallback(response);
                });
            }
            function checkLoginState2() {
                is2Players = true;
                FB.getLoginStatus(function (response) {
                    statusChangeCallback(response);
                });
            }


            // logged into the game and facebook, then can start to record the game
            function gameLogin() {
                var usernameToPass;
                console.log('Fetching information from facebool.... ');
                FB.api('/me', function (response) {
                    usernameToPass = response.name;
                    console.log('Successful login for: ' + usernameToPass);
                    document.getElementById('status').innerHTML = 'Thanks for logging in, ' + usernameToPass + '!';
                    name = response.name;
                    if (!is2Players)
                        newGame();
                    else if (is2Players)
                        newGame2();
                });
            }
        </script>

    </head>
    <body> 
        <iframe id="mainFrame" name="mainFrame" class="ui-layout-center"
                width="100%" height="600" frameborder="0" scrolling="auto">            
        </iframe>
        <!--src="search.jsp"-->
        <div class="ui-layout-west">
            <center>
                <table>
                    <tr>
                        <td>
                            <img src="images/detective.png" alt="ADAPT Centre, DCU"
                                 border="0" style="max-width: 100px; max-height:100px;">
                        </td>
                        <td>
                            <h2> Document Miner </h2>
                        </td>
                    </tr>
                </table>
                <!--table>
                <tr><td-->
                <!--input type="button" value="New Game" id="newgame" name="newgame" onclick="newGame()"-->
                <!--<form name="myForm" id="myForm" action="search.jsp" target="mainFrame">--> 
                <!--<input type="hidden" id="gameVersion1" name="gameVersion1" value="1"/>-->
                <button id="newgameBttn">New Game(P1)</button> 
                <!--</form>-->
                <!--<form name="myForm2" id="myForm2" action="search.jsp" target="mainFrame">--> 
                <!--<input type="hidden" id="gameVersion2" name="gameVersion2" value="2"/>-->
                <br>
                <button id="newgameBttn2">New Game(P2)</button> 
                <br>
                <!--</form>-->
                <button id="leadingBttn">Leaderboard</button> 
                <form name="myForm3" id="myForm3" action="profile.jsp" target="mainFrame"> 
                    <input type="hidden" id="username" name="username"/>
                    <button id="profileBttn">Profile</button> 
                </form>
                
                <!--<button id="tasksBttn">Tasks</button>--> 
                
                <button id="helpBttn">Help</button> 
                <br><br>
                <!-- login  -->
                <fb:login-button scope="public_profile,email" onlogin="checkLoginState();" data-auto-logout-link="true"></fb:login-button>
                <div id="status"></div>
                <br><div id="version">Please select Single or Two player(s) version</div>
                <div id="haha"></div>
                <!--/td><td>
                <div id="scoreboard"></div>
                </td></tr>
                </table-->
            </center>
            <br><br>
            <br>

            <div id="accordion">
                <h5>Score: </h5>
                <div id="scoreboard">
                </div>
                <h5> Status Message: </h5>
                <div id="msgboard">
                </div>
                <h5> Query Shared: </h5>
                <div id="gamepanel">
                </div>    
            </div>
        </div>

        <!-- all queries dialog box -->
        <div id="QDlg" title="P1 choose a query to share with P2">
        </div>

        <!-- Document Viewer dialog box -->
        <div id="gameTerminationDlg">
        </div>

        <!-- leading board dialog box -->
        <div id="leadDlg" title="Leading Board">
        </div>                

        <!-- Tasks dialog box -->
        <div id="tasksDlg" title="Tasks">
        </div>        

        <!-- help dialog box -->
        <div id="helpDlg" title="Game Rules">
        </div>        
    </body>

</html>
