// ACE.js initialization
var editor = ace.edit("editor");
editor.setTheme("ace/theme/eclipse");
editor.getSession().setMode("ace/mode/java");
var generateLineNum = -1;
editor.$blockScrolling = Infinity;
editor.setOptions(// Auto completion
    {
        enableBasicAutocompletion:false,
        enableSnippets: true,
        enableLiveAutocompletion:true
    }
);
var languageTools = ace.require("ace/ext/language_tools");
languageTools.addCompleter({
    getCompletions: function(editor, session, pos, prefix, callback) {
        callback(null,  [
            {
                name : "Hole",
                value : "$hole$",
                caption: "Hole",
                meta: "Hole",
                type: "tool",
                score : 1000
            }
        ]);
    }
});

// Init the dashboard
function initDashBoard() {
    $("#intro0").text("Predicting...");
    $("#recmdlist").html("");
    $("#paramlist").html("");
}

// Select the statement
var lastImportLine = -1;
function selectStatement(d, index) {
    d = d.replace(/<br>/g,"\n");
    var row = editor.getSelectionRange().start.row;
    var stmt = editor.getSession().getLine(row).trim();
    stmt = d;
    console.log("line: " + stmt);
    var tokenBefore = editor.getSelectedText();
    console.log("token before: " + tokenBefore);

    // Insert the choice
    editor.insert(d);
    editor.gotoLine(generateLineNum + 1);
    editor.focus(editor.find(d));

    $.ajax({
        //url: "http://localhost:8080/CodeRecommendation/Graph2SequenceServlet",
        url: "http://10.131.253.117:8089/CodeRecommendation/MultiRecPluginServlet",
        //url: "http://10.131.253.117:8089/CodeRecommendation/GGNNCodeRecommendationServlet",
        //url: "http://106.14.16.245:8089/CodeRecommendation/GGNNCodeRecommendationServlet",
        //url: "http://localhost:8080/CodeRecommendation/GGNNCodeRecommendationServlet",
        type: "post",
        dataType: "jsonp",
        jsonp: "callback",
        data: {
            operation: "selectStatement",
            statement: d,
            index: index
        },
        success: function (data) {
            var addInfo = data.importinfo;

            // 找到import的插入位置
            editor.gotoLine(0);
            var lastInfo = editor.find('^import(.*);',{
                regExp: true,backwards: true
            });
            console.log(lastInfo);

            var importLineNum = 0;
            if(lastInfo !== undefined) {
                // // 清除之前的导入
                // if(lastImportLine < 0) {
                //     lastImportLine = lastInfo.end.row+2;
                // }
                // else {
                //     var k;
                //     for (k = lastImportLine; k < lastInfo.end.row + 2; k++) {
                //         editor.gotoLine(k);
                //         var buff = editor.getSession().getLine(k-1) + "\n";
                //         var range = editor.find(buff, {regExp: false, backwards: false});
                //         editor.remove(range);
                //     }
                // }

                // 仅添加包信息
                lastImportLine = lastInfo.end.row+2;

                var i;
                for (i = 0; i < addInfo.length; i++) {
                    if(addInfo[i] !== "") {// 过滤空导入
                        addInfo[i] = "import " + addInfo[i] + ";";
                        // 如果有先前选择插入的import信息，忽略不计
                        if (editor.findAll('^' + addInfo[i], {regExp: true, backwards: true}) === 0) {
                            var insertLineNum = lastImportLine + importLineNum;
                            editor.gotoLine(insertLineNum);
                            // console.log("inserLineNum:" + insertLineNum);
                            editor.insert(addInfo[i] + "\n");
                            importLineNum += 1;
                        }
                    }
                }
            }

            if(importLineNum === 0){//过滤空导入
                lastImportLine = -1;
            }
            // console.log("last import line now: " + lastImportLine);
            // 结束后，返回到插入的statement位置
            editor.gotoLine(generateLineNum + 1 + importLineNum);
            editor.focus(editor.find(d,{
                regExp: false,backwards: false
            }));
        }
    });
}

// Select the parameter
function selectParameter(d) {
    menu.style.display = "none";
    // Get info before
    var row = editor.getSelectionRange().start.row;
    var stmt = editor.getSession().getLine(row).trim();
    // console.log("line: " + stmt);
    var tokenBefore = editor.getSelectedText();
    // console.log("token before: " + tokenBefore);

    // Insert the choice
    editor.insert(d);
    //initDashBoard()

    $.ajax({
        //url: "http://localhost:8080/CodeRecommendation/Graph2SequenceServlet",
        url: "http://10.131.253.117:8089/CodeRecommendation/MultiRecPluginServlet",
        //url: "http://10.131.253.117:8089/CodeRecommendation/GGNNCodeRecommendationServlet",
        //url: "http://106.14.16.245:8089/CodeRecommendation/GGNNCodeRecommendationServlet",
        //url: "http://localhost:8080/CodeRecommendation/GGNNCodeRecommendationServlet",
        type: "post",
        dataType: "jsonp",
        jsonp: "callback",
        data: {
            operation: "selectParameter",
            line: stmt,
            tokenBefore: tokenBefore,
            lineAfter: editor.getSession().getLine(row).trim(),
            parameter: d
        },
        success: function (data) {
            var code = data;   //json字符串转换为Object
            // console.log(code.content);
            // $("#intro0").text("Select the statement...");
        }
    });
}

var ERR_MSG = "The code to be completed may have some syntax errors. Please check your code.";
// Submit Predict request
function submit(){
    initDashBoard();

    var rawcode = editor.getValue();
    $("#intro0").text("Predicting...");
    // $("#recmdlist").html("");

    var spacenum = 0;

    if(editor.findAll("$hole$") === 1) {
        editor.focus(editor.find("$hole$"));
        spacenum = editor.selection.getCursor().column - 6;
        generateLineNum = editor.getSelectionRange().start.row;
        // console.log("generate line: " + generateLineNum);

        if (rawcode !== "") {
            console.log("predict");
            $.ajax({
                //url: "http://localhost:8080/CodeRecommendation/Graph2SequenceServlet",
                url: "http://10.131.253.117:8089/CodeRecommendation/MultiRecPluginServlet",
                //url: "http://10.131.253.117:8089/CodeRecommendation/GGNNCodeRecommendationServlet",
                //url: "http://106.14.16.245:8089/CodeRecommendation/GGNNCodeRecommendationServlet",
                //url: "http://localhost:8080/CodeRecommendation/GGNNCodeRecommendationServlet",
                type: "post",
                dataType: "jsonp",
                jsonp: "callback",
                data: {
                    operation: "predict",
                    rawcode: rawcode,
                    spacenum: spacenum
                },
                success: function (data) {
                     console.log(data);
                    var statements = data.statements;   //json字符串转换为Object
                    if(statements === undefined){
                        alert(ERR_MSG);
                    }
                    else if(statements.length === 0){
                        alert("Sorry, there is no recommendations for this piece of code.");
                    }
                    else {
                        // console.log(statements);
                        $("#intro0").text("Select the statement...");

                        var lines = "";
                        var i = 0;
                        for (i = 0; i < statements.length; i++) {
                            lines = lines + "<li><a class=\"list-item\" onclick=\"selectStatement(this.text,"+i+")\">" + statements[i] + "</a></li>";
                        }

                        $("#recmdlist").html(lines);

                        editor.focus(editor.find("$hole$"));
                        selectStatement(statements[0],0);
                        lastImportLine = -1;
                    }
                }
            });
        }
    }
    else{
        editor.insert("$hole$");
        rawcode = editor.getValue();

        editor.focus(editor.find("$hole$"));
        spacenum = editor.selection.getCursor().column - 6;

        generateLineNum = editor.getSelectionRange().start.row;
        // console.log("generate line: " + generateLineNum);

        if (rawcode !== "") {
            $.ajax({
                //url: "http://localhost:8080/CodeRecommendation/Graph2SequenceServlet",
                url: "http://10.131.253.117:8089/CodeRecommendation/MultiRecPluginServlet",
                //url: "http://10.131.253.117:8089/CodeRecommendation/GGNNCodeRecommendationServlet",
                //url: "http://106.14.16.245:8089/CodeRecommendation/GGNNCodeRecommendationServlet",
                //url: "http://localhost:8080/CodeRecommendation/GGNNCodeRecommendationServlet",
                type: "post",
                dataType: "jsonp",
                jsonp: "callback",
                data: {
                    operation: "predict",
                    rawcode: rawcode,
                    spacenum:spacenum
                },
                success: function (data) {
                    // console.log(data);
                    var statements = data.statements;   //json字符串转换为Object
                    // console.log(statements);
                    if(statements === undefined){
                        alert(ERR_MSG);
                    }
                    else if(statements.length === 0){
                        alert("Sorry, there is no recommendations for this piece of code.");
                    }
                    else {
                        $("#intro0").text("Select the statement...");

                        var lines = "";
                        var i = 0;
                        for (i = 0; i < statements.length; i++) {
                            lines = lines + "<li><a class=\"list-item\" onclick=\"selectStatement(this.text," + i + ")\">" + statements[i] + "</a></li>";
                        }

                        $("#recmdlist").html(lines);

                        editor.focus(editor.find("$hole$"));
                        selectStatement(statements[0], 0);
                        lastImportLine = -1;
                    }
                },
                // error: function(){
                //     alert(ERR_MSG);
                // }
            });
        }
    }
}

// // Get parameter list
// function modify(){
//     $("#paramlist").html("");
//
//     var row = editor.getSelectionRange().start.row;
//     var stmt = editor.getSession().getLine(row).trim();
//     console.log("line: " + stmt);
//     var tokenBefore = editor.getSelectedText();
//     console.log("token before: " + tokenBefore);
//
//     // todo: get the parameter according to the line and the token before.
//     $.ajax({
//         url: "http://10.131.253.117:8089/CodeRecommendation/CodeRecommendationServlet",
//         type: "post",
//         dataType: "jsonp",
//         jsonp: "callback",
//         data: {
//             operation: "getParameter",
//             line: stmt,
//             tokenBefore: tokenBefore
//         },
//         success: function (data) {
//             var parameters = data ;   //json字符串转换为Object
//             console.log(parameters.parameters);
//
//             var lines = "";
//             var i = 0;
//             for(i = 0; i < parameters.parameters.length; i++){
//                 lines = lines + "<li><a class=\"list-item\" onclick=\"selectParameter(this.text)\">"+parameters.parameters[i]+"</a></li>";
//             }
//
//             $("#paramlist").html(lines);
//         }
//     });
// }

// var menu = document.getElementById("menu");

// document.oncontextmenu = function(e) {
//     var e = e || window.event;
//     // Position of cursor click
//     var oX = e.clientX;
//     var oY = e.clientY;
//     // Display
//     menu.style.display = "block";
//     menu.style.left = oX + "px";
//     menu.style.top = oY + "px";
//     // Modify parameter
//     modify();
//     // Prevent default event
//     return false;
// };
//
// document.onclick = function(e) {
//     var e = e || window.event;
//     menu.style.display = "none"
// };
//
// menu.onclick = function(e) {
//     var e = e || window.event;
//     e.cancelBubble = true;
// };

$(document).keydown(function (e) {
    if (e.ctrlKey && e.keyCode === 13)
    {
        submit();
    }
});