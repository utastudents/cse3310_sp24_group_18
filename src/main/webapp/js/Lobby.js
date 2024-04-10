document.addEventListener('DOMContentLoaded', function() {
    const csvPath = '../data/main.csv'; // Path to your CSV file
  
    const table = document.getElementById('csvRoot');
    const refreshBtn = document.getElementById('refreshBtn');
  
    refreshBtn.addEventListener('click', function() {
      loadCSV(csvPath);
    });
  
    function loadCSV(filePath) {
      fetch(filePath)
        .then(response => response.text())
        .then(text => {
          const rows = text.split('\n').map(row => row.split(','));
          displayCSV(rows);
        })
        .catch(error => console.error('Error loading the CSV file:', error));
    }
  
    function displayCSV(rows) {
      table.innerHTML = ''; // Clear the table
  
      // Manually generate HTML for table headers
      const thead = document.createElement('thead');
      const headerRow = document.createElement('tr');
      const headers = ['Player Username', 'Online', 'Game Won', 'Game Lost', 'In-game Points', 'Opponent Username', 'Grid Color Choice', 'Create Game'];
      headers.forEach(headerText => {
        const th = document.createElement('th');
        th.textContent = headerText;
        headerRow.appendChild(th);
      });
      thead.appendChild(headerRow);
      table.appendChild(thead);
  
      // Generate HTML for table body
      const tbody = document.createElement('tbody');
      rows.forEach(row => { // Changed from rows.slice(1) to rows to include the first line
        const tr = document.createElement('tr');
        row.forEach(cell => {
          const td = document.createElement('td');
          td.textContent = cell;
          tr.appendChild(td);
        });
  
        // Create the "Create Game" button
        const buttonTd = document.createElement('td');
        const createGameBtn = document.createElement('button');
        createGameBtn.textContent = 'Create Game';
  
        // Determine button state based on conditions
        const online = row[1].trim().toLowerCase() === 'true';
        const opponentUsername = row[5].trim();
  
        if (online && opponentUsername !== 'null') {
          createGameBtn.classList.add('disabled');
          createGameBtn.disabled = false;
        } else if (!online && opponentUsername === 'null') {
          createGameBtn.classList.add('neutral');
          createGameBtn.disabled = false;
        } else {
          createGameBtn.classList.add('enabled');
          createGameBtn.disabled = false;
        }
  
        buttonTd.appendChild(createGameBtn);
        tr.appendChild(buttonTd);
        tbody.appendChild(tr);
      });
      table.appendChild(tbody);
    }
  
    // Load the CSV data initially
    loadCSV(csvPath);
  });
  