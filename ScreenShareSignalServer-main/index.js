const http = require("http")
const socket = require("websocket").server
const server = http.createServer(() => {
})

server.listen(3000, () => {

})

const users = []

const Types = {
    SignIn: "SignIn",
    StartStreaming: "StartStreaming",
    UserFoundSuccessfully: "UserFoundSuccessfully",
    Offer: "Offer",
    Answer: "Answer",
    IceCandidates: "IceCandidates",
    EndCall: "EndCall",
    RemoteControl: "RemoteControl",
    AccessibilityAccept: "AccessibilityAccept",
    AccessibilityReject: " AccessibilityReject",
    
}

const webSocket = new socket({httpServer: server})


webSocket.on('request', (req) => {
    const connection = req.accept();

    connection.on('message', (message) => {
        try {
            const data = JSON.parse(message.utf8Data);
            const currentUser = findUser(data.username)
            const userToReceive = findUser(data.target)
            console.log(data)

            switch (data.type) {
                case Types.SignIn:
                    if (currentUser) {
                        return
                    }

                    users.push({username: data.username, conn: connection, password: data.data})
                    break
                case Types.StartStreaming :
                    if (userToReceive) {
                            sendToConnection(userToReceive.conn, {
                                type: Types.StartStreaming,
                                username: currentUser.username,
                                target: userToReceive.username
                            })
                    }
                    break
                case Types.Offer :
                    if (userToReceive) {
                        sendToConnection(userToReceive.conn, {
                            type: Types.Offer, username: data.username, data: data.data
                        })
                    }
                    break
                case Types.Answer :
                    if (userToReceive) {
                        sendToConnection(userToReceive.conn, {
                            type: Types.Answer, username: data.username, data: data.data
                        })
                    }
                    break
                case Types.IceCandidates:
                    if (userToReceive) {
                        sendToConnection(userToReceive.conn, {
                            type: Types.IceCandidates, username: data.username, data: data.data
                        })
                    }
                    break
                case Types.EndCall:
                    if (userToReceive) {
                        sendToConnection(userToReceive.conn, {
                            type: Types.EndCall, username: data.username
                        })
                    }
                    break

                case Types.RemoteControl:
                    if (userToReceive) {
                        sendToConnection(userToReceive.conn, {
                            type: Types.RemoteControl, username: data.username
                        })
                    }
                    break
                case Types.AccessibilityAccept:
                    if (userToReceive) {
                        sendToConnection(userToReceive.conn, {
                            type: Types.AccessibilityAccept, username: data.username
                        })
                    }
                case Types.AccessibilityReject:
                    console.log('AccessibilityReject : ' +  data.username)

                    if (userToReceive) {
                        sendToConnection(userToReceive.conn, {
                            type: Types.AccessibilityReject, username: data.username
                        })
                    }
            }
        } catch (e) {
            console.log(e.message)
        }

    });
    connection.on('close', () => {
        users.forEach(user => {
            if (user.conn === connection) {
                users.splice(users.indexOf(user), 1)
            }
        })
    })
});


const sendToConnection = (connection, message) => {
    connection.send(JSON.stringify(message))
}

const findUser = username => {
    for (let i = 0; i < users.length; i++) {
        if (users[i].username === username) return users[i]
    }
}
