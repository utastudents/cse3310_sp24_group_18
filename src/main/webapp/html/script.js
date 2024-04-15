document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    initializeLogin();
    if (document.getElementById('lobby-section').style.display !== 'none') {
        initializeLobby();
    }
    setupWebSocket();
}

function initializeLogin() {
    const loginButton = document.getElementById('enterLobbyBtn');
    if (loginButton) {
        loginButton.addEventListener('click', function() {
            const usernameInput = document.getElementById('username');
            const username = usernameInput ? usernameInput.value.trim() : '';
            if (username) {
                saveNewPlayer(username);
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
    const color = colorSelector ? colorSelector.options[colorSelector.selectedIndex].value : 'cyan';
    const newPlayer = {
        PlayerUsername: username,
        GridColorChoice: color,
        Online: true,
        GameWon: 0,
        GameLost: 0,
        InGamePoints: 0,
        OpponentUsername: null
    };

    const socket = new WebSocket('ws://localhost:9180/websocket');
    socket.onopen = function() {
        console.log('WebSocket connection established');
        socket.send(JSON.stringify(newPlayer));
    };

    socket.onmessage = function(event) {
        console.log('Message from server:', event.data);
        if (event.data === 'Player saved') {
            showSection('lobby-section');
        }
    };

    socket.onerror = function(error) {
        console.error('WebSocket error:', error);
    };

    socket.onclose = function() {
        console.log('WebSocket connection closed');
    };
}

function initializeLobby() {
    const refreshBtn = document.getElementById('refreshBtn');
    refreshBtn.addEventListener('click', function() {
        loadPlayerData('lobbyPlayerData');
    });
    loadPlayerData('lobbyPlayerData');
}

function setupWebSocket() {
    const socket = new WebSocket('ws://localhost:9180/websocket');
    socket.onopen = function() {
        console.log('WebSocket connection established');
    };

    socket.onmessage = function(event) {
        console.log('Message received:', event.data);
        const data = JSON.parse(event.data);
        if (data.type === 'playerDataUpdate') {
            loadPlayerData('lobbyPlayerData');
        }
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
            console.log('Message sent:', message);
        } else {
            console.error('WebSocket is not open.');
        }
    };
}

function loadPlayerData(tableId) {
    console.log('Attempting to fetch player data');
    fetch('players.json') // Adjust the path if needed
        .then(response => response.json())
        .then(data => {
            console.log('Player data loaded', data);
            displayPlayerData(data, tableId);
        })
        .catch(error => {
            console.error('Failed to load player data:', error);
        });
}


function displayPlayerData(players, tableId) {
    const table = document.getElementById(tableId);
    if (!table) {
        console.error(`Table with id ${tableId} not found.`);
        return;
    }
    const tableBody = table.querySelector('tbody');
    if (!tableBody) {
        console.error(`Tbody for table with id ${tableId} not found.`);
        return;
    }
    tableBody.innerHTML = '';
    players.forEach((player, index) => {
        let row = `<tr>
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
                <button ${player.Online ? '' : 'disabled'} onclick="createGame(${index})">
                    Create Game
                </button>
            </td>
        </tr>`;
        tableBody.insertAdjacentHTML('beforeend', row);
    });
}

function showSection(sectionId) {
    document.querySelectorAll('.section').forEach(section => {
        section.style.display = 'none';
    });
    document.getElementById(sectionId).style.display = 'block';
}
