const socket = new WebSocket("ws://localhost:9180");
// grid
document.getElementById('sendSelection').addEventListener('click', sendSelectedCells);
document.getElementById("loginForm").addEventListener("submit", (event) => {
  event.preventDefault(); // Prevent form submission

  const username = document.getElementById("username").value;
  connectWebSocket(username);
  updateGameTable([]);
  // showSection("After Login : section1");
  socket.send("section1");
});


function updatePlayerList(playerNamesJSON) {
  const playerNames = JSON.parse(playerNamesJSON);
  const playerList = document.getElementById("playerList");
  playerList.innerHTML = ""; // Clear previous entries

  playerNames.forEach((player) => {
    let playerItem = document.createElement("li");
    playerItem.textContent = player; // Set the text to the player's name
    playerList.appendChild(playerItem);
  });
}

// showGameRoom function to show the game room
function showGameRoom(roomId, player, opponent) {
  showSection(roomId); // Shows the appropriate game room
  document.getElementById(roomId + "_player").textContent = player;
  document.getElementById(roomId + "_opponent").textContent = opponent;
}

// CHAT
function sendChatMessage(roomId) {
  const input = document.getElementById(`${roomId}_chat_input`);
  const message = input.value.trim();
  if(message) {
    socket.send(`chat:${roomId}:${message}`);
    input.value = ''; // Clear input field after sending
  }
}


function updateGameTable(gameRooms) {
  const tbody = document.getElementById("gameTableBody");
  tbody.innerHTML = ""; // Clear existing content

  gameRooms.forEach((room) => {
    let row = tbody.insertRow();
    let cellRoom = row.insertCell(0);
    let cellPlayers = row.insertCell(1);
    let cellJoin = row.insertCell(2);

    cellRoom.textContent = room.name;
    cellPlayers.textContent = room.players;

    let joinButton = document.createElement("button");
    joinButton.textContent = "Join";
    joinButton.id = "join_" + room.name; // Unique ID for button
    joinButton.addEventListener("click", function () {
      socket.send("join_game:" + room.name);
    });
    cellJoin.appendChild(joinButton);
  });
}

// GRID///

let selectedCells = [];

function generateGridHTML(grid) {
  // Create grid HTML with clickable cells
  return grid.map((row, rowIndex) =>
    `<div class="grid-row">${row
      .map((cell, cellIndex) => 
        `<span class="grid-cell" data-row="${rowIndex}" data-cell="${cellIndex}" onclick="cellClickHandler(this)">${cell}</span>`)
      .join('')}</div>`
  ).join('');
}

function toggleGridVisibility(roomId) {
  const gridElement = document.getElementById(`${roomId}_grid`);
  if (gridElement) {
    gridElement.classList.toggle("hidden");
  } else {
    console.error("No grid element found for room ID:", roomId);
  }
}

function toggleCellSelection(cellElement, cellValue) {
  const rowIndex = cellElement.dataset.row;
  const cellIndex = cellElement.dataset.cell;
  const cellKey = `${rowIndex}-${cellIndex}`;

  if (selectedCells.includes(cellKey)) {
    selectedCells = selectedCells.filter((cell) => cell !== cellKey);
    cellElement.classList.remove('selected');
  } else {
    selectedCells.push(cellKey);
    cellElement.classList.add('selected');
  }
}

function cellClickHandler(cell) {
  // Toggle selected class on click
  cell.classList.toggle('selected');
}


// Function to send selected cells as a string
function sendSelectedCells() {
  const selectedCells = document.querySelectorAll('.grid-cell.selected');
  const selectedText = Array.from(selectedCells).map(cell => cell.textContent).join('');
  console.log(selectedText);
}
function updateGrid(roomId, gridData) {
  const gridHtml = generateGridHTML(gridData);
  const gridElement = document.getElementById(`${roomId}_grid`);
  if (gridElement) {
    gridElement.innerHTML = gridHtml;
    gridElement.classList.remove("hidden"); // Make the grid visible
  } else {
    console.error("No grid element found for room ID:", roomId);
  }
}

function updateWords(roomId, words) {
  const wordsListHtml = words.map((word) => `<li>${word}</li>`).join("");
  const wordsElement = document.getElementById(`${roomId}_words`);
  if (wordsElement) {
    wordsElement.innerHTML = wordsListHtml;
  } else {
    console.error("No words element found for room ID:", roomId);
  }
}

function updateLoggedInUser(username) {
  // Query all elements that could contain the username and update them.
  document.querySelectorAll(".currentUsername").forEach(function (span) {
    span.textContent = username;
  });
  console.log("Logged in user updated: ", username);
}
// connect and send the username to the server
function connectWebSocket(username) {
  console.log("Attempting to add new player");
  // Send the username to the server
  try {
    socket.send("new_player:" + username);
    console.log("New player added: ", username);
  } catch (error) {
    console.log("Error adding new player: ", error);
  }
}

socket.onopen = function (event) {
  console.log("WebSocket connection established");
};

socket.onmessage = function (event) {
  const sectionToShow = event.data;
  // Use a switch case to determine which section to show

  console.log("Received message:", event.data);

  const data = event.data.split(":");
  const command = data[0];
  const content = data.slice(1).join(":"); // Ensure all content after the first colon is included

  //   // Check if the message is about updating game rooms
  //   if (data.startsWith("update_gameRooms:")) {
  //     let gameRoomsJson = data.substring("update_gameRooms:".length);
  //     updateGameTable(JSON.parse(gameRoomsJson));
  // }

  if (command === 'chat') {
    const roomId = data[1];
    const message = data.slice(2).join(':');
    const messagesContainer = document.getElementById(`${roomId}_messages`);
    messagesContainer.innerHTML += `<div>${message}</div>`;
  }

  switch (command) {
    case "update_players":
      const username = content;
      console.log("Updating player list with data:", content);
      updatePlayerList(username); // Handle updated player list
      console.log(
        "[switch: command (update_players)] Received data:",
        event.data
      );
      console.log(
        "[switch: command (update_players)] Received command:",
        command
      );
      break;

    case "update_gameRooms":
      console.log("RECIEVED GAMEROOM UPDATE REQUEST [update_gameRooms]");
      const gameRooms = JSON.parse(content);
      console.log("Updating game rooms with data:", gameRooms);
      updateGameTable(gameRooms);
      break;

    case "update_grid":
      if (data.length >= 3) {
        const roomId = data[1];
        const gridData = JSON.parse(data.slice(2).join(":")); // Joining back the rest of the message
        updateGrid(roomId, gridData);
      }
      break;
    case "update_words":
      if (data.length >= 3) {
        const roomId = data[1];
        const words = JSON.parse(data.slice(2).join(":")); // Joining back the rest of the message
        updateWords(roomId, words);
      }
      break;

    case "start_game":
      // Extracted room ID, player, and opponent names
      const room = data[1];
      const player = data[2];
      const opponent = data[3];
      // Show the game room with updated player and opponent info
      showGameRoom(room, player, opponent);
      // Additionally, show the grid and words for this game room
      showSection(room);
      break;

    case "player_added":
      try {
        const username = content;
        updateLoggedInUser(username);
      } catch (error) {
        console.log("Error updating logged in user: ", error);
        break;
      }

      break;
    default:
      console.log("no such command [update_players]", command);
      break;
  }

  switch (sectionToShow) {
    case "section0":
      showSection("section0");
      console.log("section0");
      break;
    case "section1":
      showSection("section1");
      console.log("From the switch case :section1");
      break;
    case "section2":
      showSection("section2");
      console.log("From the switch case :section2");
      // Add any additional logic for section2 button clicks here
      break;
    case "section3":
      showSection("section3");
      console.log("From the switch case :section3");
      // Add any additional logic for section3 button clicks here
      break;
    case "gameroom1":
      showSection("gameroom1");
      console.log("From the switch case :gameroom1");
      // Add any additional logic for gameroom1 button clicks here
      break;

    case "gameroom2":
      console.log("From the switch case :gameroom2");
      showSection("gameroom2");
      // Add any additional logic for gameroom2 button clicks here
      break;

    case "gameroom3":
      console.log("From the switch case :gameroom3");
      showSection("gameroom3");
      // Add any additional logic for gameroom3 button clicks here
      break;

    case "gameroom4":
      console.log("From the switch case :gameroom4");
      showSection("gameroom4");
      // Add any additional logic for gameroom4 button clicks here
      break;

    // if new player added then console.log player and username
    case "player_added":
      const player = event.data;
      console.log("New player added: ", player);
      break;
    case "player_not_added":
      console.log("Player not added");
      break;
    default:
      console.log("No such section exists", sectionToShow);
      break;
  }
};

socket.onerror = function (event) {
  console.error("WebSocket error observed:", event);
};

socket.onclose = function (event) {
  console.log("WebSocket connection closed", event.code, event.reason);
};

function generateGridHTML(gridData) {
  console.log("Received grid data:", gridData); // Log to check the data
  const gridHtml = gridData
    .map((row) => `<div>${row.join(" ")}</div>`)
    .join("");
  console.log("Generated grid HTML:", gridHtml); // Check the output HTML
  return gridHtml;
}

function showSection(sectionId) {
  console.log("Showing section:", sectionId); // Debug: Log which section is being shown
  // Hide all sections
  document
    .querySelectorAll("div[id^='section'], div[id^='gameroom']")
    .forEach((div) => {
      div.classList.add("hidden");
    });

  // Show the current section
  const section = document.getElementById(sectionId);
  if (section) {
    section.classList.remove("hidden");
  } else {
    console.error("No section found with ID:", sectionId);
  }

  // If the section is a game room, also remove the hidden class from its grid and word list
  if (sectionId.startsWith("gameroom")) {
    const grid = document.getElementById(sectionId + "_grid");
    const words = document.getElementById(sectionId + "_words");
    if (grid && words) {
      grid.classList.remove("hidden");
      words.classList.remove("hidden");
    }
  }
}
