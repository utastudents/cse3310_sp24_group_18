document.addEventListener('DOMContentLoaded', function() {
  const refreshBtn = document.getElementById('refreshBtn');
  const playerData = document.getElementById('playerData').querySelector('tbody');

  refreshBtn.addEventListener('click', function() {
    loadPlayerData();
  });

  function loadPlayerData() {
    fetch('../data/players.json')
      .then(response => response.json())
      .then(data => {
        displayPlayerData(data);
      })
      .catch(error => console.error('Error loading player data:', error));
  }

  function displayPlayerData(players) {
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

  loadPlayerData();
});
