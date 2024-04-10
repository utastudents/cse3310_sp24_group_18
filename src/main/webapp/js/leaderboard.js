document.addEventListener('DOMContentLoaded', function() {
    const csvPath = '../data/main.csv'; // Adjust the path as needed
    const table = document.getElementById('csvRoot');
    const refreshBtn = document.getElementById('refreshBtn');
  
    refreshBtn.addEventListener('click', function() {
        loadCSV(csvPath);
    });

    function loadCSV(filePath) {
        fetch(filePath)
            .then(response => response.text())
            .then(text => {
                const rows = text.split('\n').map(row => row.split(',').map(cell => cell.trim()));
                displayCSV(rows);
            })
            .catch(error => console.error('Error loading the CSV file:', error));
    }
  
    function displayCSV(rows) {
        table.innerHTML = ''; // Clear the table

        // Manually generate HTML for table headers
        const thead = document.createElement('thead');
        const headerRow = document.createElement('tr');
        const headers = ['Player Username', 'Online', 'Game Won', 'Game Lost']; // Adjusted to match your requirement
        headers.forEach(headerText => {
            const th = document.createElement('th');
            th.textContent = headerText;
            headerRow.appendChild(th);
        });
        thead.appendChild(headerRow);
        table.appendChild(thead);

        // Generate HTML for table body
        const tbody = document.createElement('tbody');
        rows.forEach((row, rowIndex) => {
            if (rowIndex === 0) return; // Skip the header row in the CSV
            const tr = document.createElement('tr');
            headers.forEach(header => {
                const cellIndex = rows[0].indexOf(header); // Find the index of the header in the CSV
                if (cellIndex !== -1) {
                    const td = document.createElement('td');
                    td.textContent = row[cellIndex];
                    tr.appendChild(td);
                }
            });

            tbody.appendChild(tr);
        });
        table.appendChild(tbody);
    }

    // Load the CSV data initially
    loadCSV(csvPath);
});
