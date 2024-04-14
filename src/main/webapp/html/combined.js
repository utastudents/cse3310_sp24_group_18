document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    initializeLogin();
    initializeChat();
    initializeLeaderboard();
    initializeLobby();
    setupWebSocket();
}

function initializeLogin() {
    const loginButton = document.getElementById('enterLobbyBtn'); // Corrected ID from 'login-button'

    if (loginButton) {
        loginButton.addEventListener('click', function() {
            const usernameInput = document.getElementById('username');
            const username = usernameInput ? usernameInput.value.trim() : '';
            if (username) {
                saveNewPlayer(username); // Save the new pxlayer and move to lobby
            } else {
                alert('Please enter a username.');
            }
        });
    } else {
        console.error('Enter Lobby button not found');
    }
}


function saveNewPlayer(username) {
    const colorSelector = document.getElementById('colorSelector');
    const color = colorSelector ? colorSelector.options[colorSelector.selectedIndex].value : 'cyan'; // default color

    const newPlayer = {
        PlayerUsername: username,
        GridColorChoice: color,
        Online: true,
        GameWon: 0,
        GameLost: 0,
        InGamePoints: 0,
        OpponentUsername: null
    };

    // Use WebSocket to send player data
    const socket = new WebSocket('ws://localhost:9180/websocket');
    socket.onopen = function() {
        console.log('WebSocket connection established');
        socket.send(JSON.stringify(newPlayer)); // Send the new player data as a JSON string
    };

    socket.onmessage = function(event) {
        console.log('Message from server:', event.data);
        if (event.data === 'Player saved') {
            showSection('lobby-section'); // Navigate to lobby after confirmation from server
        }
    };

    socket.onerror = function(error) {
        console.error('WebSocket error:', error);
    };

    socket.onclose = function() {
        console.log('WebSocket connection closed');
    };
}


function initializeChat() {
    const messageInput = document.getElementById('message-input');
    const sendBtn = document.getElementById('send-btn');

    sendBtn.addEventListener('click', function() {
        const message = messageInput.value.trim();
        if (message) {
            sendMessage(message);
            messageInput.value = '';
        }
    });
}

function initializeLeaderboard() {
    const refreshBtn = document.getElementById('refreshBtn');
    refreshBtn.addEventListener('click', function() {
        loadPlayerData('playerData'); // Load player data into the leaderboard
    });
}

function initializeLobby() {
    // Assuming similar structure to the leaderboard
    // Load initial data for the lobby
    loadPlayerData('lobbyPlayerData'); // Ensure this ID is correct in your HTML
}

function setupWebSocket() {
    const socket = new WebSocket(`ws://${window.location.host}/ws`);

    socket.onopen = function() {
        console.log('WebSocket connection established');
    };

    socket.onmessage = function(event) {
        console.log('Message received:', event.data);
        handleWebSocketMessage(event.data);
    };

    socket.onerror = function(event) {
        console.error('WebSocket error:', event);
    };

    socket.onclose = function(event) {
        console.log('WebSocket connection closed:', event);
    };

    window.sendMessage = function(message) {
        if (socket.readyState === WebSocket.OPEN) {
            socket.send(message);
        } else {
            console.error('WebSocket is not open.');
        }
    };
}

function handleWebSocketMessage(message) {
    // Handle different message types here
    console.log('Processing message:', message);
}

function loadPlayerData(tableId) {
    fetch('src/main/java/uta/cse3310/players.json')
        .then(response => response.json())
        .then(data => {
            displayPlayerData(data, tableId);
        })
        .catch(error => console.error('Error loading player data:', error));
}

function displayPlayerData(players, tableId) {
    const playerData = document.getElementById(tableId).querySelector('tbody');
    playerData.innerHTML = ''; // Clear existing data
    players.forEach(player => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${player.PlayerUsername}</td>
            <td>${player.Online ? 'Yes' : 'No'}</td>
            <td>${player.GameWon}</td>
            <td>${player.GameLost}</td>
            <td>${player.InGamePoints}</td>
            <td>${player.OpponentUsername || 'None'}</td>
            <td>
                <span style="display:inline-block;width:20px;height:20px;background-color:${player.GridColorChoice};"></span>
            </td>
            <td>
                <button class="${player.Online && player.OpponentUsername ? 'enabled' : 'disabled'}">
                    Create Game
                </button>
            </td>
        `;
        playerData.appendChild(tr);
    });
}

function showSection(sectionId) {
    // Hide all sections
    document.querySelectorAll('.section').forEach(section => {
        section.style.display = 'none';
    });

    // Show the requested section
    document.getElementById(sectionId).style.display = 'block';
}
