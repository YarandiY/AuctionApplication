var stompClient = null;
var auctionId = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    } else {
        $("#conversation").hide();
    }
    $("#chats").html("");
}


function connect() {
    var socket = new SockJS('/socket',null , {auth : "Bearer " + $("#token").val()});
    //if you connect through WebSocket (without SockJS)
    // var socket = new WebSocket('/socket');
    stompClient = Stomp.over(socket);
    stompClient.connect({auth: "Bearer " + $("#token").val(), deviceID: $("#deviceId").val()}, function (frame) {
        console.log(frame);
        setConnected(true);
        console.log('Connected: ' + frame);
    },function (error) {
        console.log(JSON.stringify(error));
    });
}

function initSubscribe() {
    console.log("try to subscribe /app");
    stompClient.subscribe('/user/app/all', function (auctionId) {
        console.log("new message");
        showChat(auctionId.body);
    },function (error) {
        console.log("error -_-");
        console.log(JSON.stringify(error));
        console.log(error);
    });
    stompClient.subscribe('/app/all', function (auctionId) {
        console.log("new message");
        showChat(auctionId.body);
    },function (error) {
        console.log("error -_-");
        console.log(JSON.stringify(error));
        console.log(error);
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

var lastSubId;

function join() {
    auctionId = $("#auctionId").val();
    $("#chatHeader").append(auctionId + " ");
    lastSubId = stompClient.subscribe('/auction/id/' + auctionId, function (greeting) {
        console.log("get somethingggggggggggggggg");
        showChat(greeting.body);
    },function (error) {
        console.log(JSON.stringify(error));
    }).id;
    console.log(lastSubId);
}

function disjoin() {
    auctionId = $("#auctionId").val();
    $("#chatHeader").append(' Exit from' + auctionId + "! ");
    stompClient.unsubscribe(lastSubId);
    // auctionId = null;
}

function send() {
    stompClient.send("/app/bid", {}, JSON.stringify({
        'price': $("#message").val(),
        'auctionId': auctionId
    }));
}

function showChat(message) {
    console.log(message);
    $("#chats").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#connect").click(function () {
        connect();
    });
    $("#disconnect").click(function () {
        disconnect();
    });
    $("#join").click(function () {
        join();
    });
    $("#disjoin").click(function () {
        disjoin();
    });
    $("#init").click(function () {
        initSubscribe();
    });
    $("#send").click(function () {
        send();
    });
});