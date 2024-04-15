document.getElementById('refreshBtn').addEventListener('click', function() {
    fetch('players.json')  // Adjust the URL as needed
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
    tbody.innerHTML = '';  // Clear existing entries
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
