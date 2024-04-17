document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});


function initializeApp() {
    initializeLogin();
    setupWebSocket();
    loadPlayerData('lobbyPlayerData');
    // Initialize the grid debug button event listener
    setupGridDebugButton();
}

function setupGridDebugButton() {
    const gridDebugButton = document.getElementById('game_section_debug');
    gridDebugButton.addEventListener('click', function() {
        showSection('game-container'); // Use the correct ID for your grid section
    });
}

function initializeLogin() {
    const loginButton = document.getElementById('enterLobbyBtn');
    loginButton.addEventListener('click', function() {
        const usernameInput = document.getElementById('username');
        const username = usernameInput.value.trim();
        if (username) {
            saveNewPlayer(username);
            showSection('lobby-section');
        } else {
            alert('Please enter a username.');
        }
    });
}

function saveNewPlayer(username) {
    const colorSelector = document.getElementById('colorSelector');
    const color = colorSelector.value;
    const newPlayer = {
        PlayerUsername: username,
        GridColorChoice: color,
        Online: true,
        GameWon: 0,
        GameLost: 0,
        InGamePoints: 0,
        OpponentUsername: null
    };
    sendMessage(newPlayer);
}

function setupWebSocket() {
    const socket = new WebSocket('ws://localhost:9180/websocket');
    socket.onopen = function() {
        console.log('WebSocket connection established');
    };

    socket.onmessage = function(event) {
        try {
            const data = JSON.parse(event.data);
            if (data.action === 'new_game_created') {
                updateGrid(data.grid);
                updatePlacedWords(data.placedWords);
                console.log('Received non-JSON message:', event.data);
            }
            // Handle other JSON messages
        } catch (e) {
            console.error('Error parsing JSON:', e);
            console.log('Received non-JSON message:', event.data);
            // Handle non-JSON messages or show them directly
        }
    };

    socket.onerror = function(error) {
        console.error('WebSocket error:', error);
    };
    socket.onclose = function() {
        console.log('WebSocket connection closed');
    };

    window.sendMessage = function(message) {
        if (socket.readyState === WebSocket.OPEN) {
            socket.send(JSON.stringify(message));
        } else {
            console.error('WebSocket is not open.');
        }
    };
}

function updateGrid(grid) {
    const gridContainer = document.getElementById('word-grid');
    gridContainer.innerHTML = ''; // Clear previous grid if any

    const table = document.createElement('table');
    grid.forEach(row => {
        const tr = document.createElement('tr');
        row.forEach(cell => {
            const td = document.createElement('td');
            td.textContent = cell; // Assuming cell is a single character
            tr.appendChild(td);
        });
        table.appendChild(tr);
    });

    gridContainer.appendChild(table);
}

function updatePlacedWords(placedWords) {
    const wordsSection = document.getElementById('placed-words-section');
    wordsSection.innerHTML = '<h3>Placed Words</h3>';

    placedWords.forEach(word => {
        const wordDiv = document.createElement('div');
        wordDiv.textContent = word;
        wordsSection.appendChild(wordDiv);
    });
}

function setupLeaderboard() {
    const leaderboardButton = document.getElementById('lobbyLeaderboardButton');
    leaderboardButton.addEventListener('click', function() {
        loadLeaderboardData();
    });
}

document.getElementById('refreshBtn').addEventListener('click', function() {
    fetch('new_players.json') // Adjust the URL as needed
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok: ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            displayPlayerData(data);
        })
        .catch(error => {
            console.error('Failed to load player data:', error);
        });
});

function displayPlayerData(players) {
    const table = document.getElementById('lobbyPlayerData');
    const tbody = table.querySelector('tbody');
    tbody.innerHTML = ''; // Clear existing entries
    players.forEach(player => {
        const row = `<tr>
            <td>${player.PlayerUsername}</td>
            <td>${player.Online ? 'Yes' : 'No'}</td>
            <td>${player.GameWon}</td>
            <td>${player.GameLost}</td>
            <td>${player.InGamePoints}</td>
            <td>${player.OpponentUsername || 'None'}</td>
            <td style="background-color: ${player.GridColorChoice}; width: 20px; height: 20px;"></td>
            <td><button ${player.Online ? '' : 'disabled'}>Create Game</button></td>
        </tr>`;
        tbody.insertAdjacentHTML('beforeend', row);
    });
}

function loadLeaderboardData() {
    // Optional: Change 'players.json' if leaderboard data is different
    fetch('players.json')
        .then(response => response.json())
        .then(data => {
            displayLeaderboardData(data, 'playerData');
        })
        .catch(error => {
            console.error('Failed to load leaderboard data:', error);
        });
}

function displayLeaderboardData(data, tableId) {
    const table = document.getElementById(tableId);
    const tbody = table.getElementsByTagName('tbody')[0];
    tbody.innerHTML = '';
    data.forEach(player => {
        const row = `<tr>
            <td>${player.PlayerUsername}</td>
            <td>${player.GameWon}</td>
            <td>${player.GameLost}</td>
        </tr>`;
        tbody.insertAdjacentHTML('beforeend', row);
    });
    showSection('leaderboard-section');  // Display the leaderboard section
}

function showSection(sectionId) {
    const sections = document.querySelectorAll('.section');
    sections.forEach(section => section.style.display = 'none'); // Hide all sections

    const activeSection = document.getElementById(sectionId);
    if (activeSection) {
        if (sectionId === 'game-container') {
            activeSection.style.display = 'grid'; // Assuming the grid section should be a grid
        } else {
            activeSection.style.display = 'block'; // For other sections use 'block' or 'flex' as needed
        }
    } else {
        console.error('No section found with ID:', sectionId);
    }
}
function createGame(username) {
    console.log(`Creating game with ${username}`); // Logic for game creation
}