// const socket = new WebSocket("ws://localhost:9118");
serverUrl = "ws://" + window.location.hostname + ":9118";
const socket = new WebSocket(serverUrl);
var lastSentWords = []; // This will store the last sent words for reference
var confirmedCells = new Set(); // Stores IDs of cells confirmed as correct
var selectedWords = []; // This will store the selected words

function showGameRules() {
  const gameRules = "Welcome to the Game!\n\nHere's how to play:\n1. Search the grid and sequentially select the letters to form words. You can select each letter in any direction: horizontal, vertical, or diagonal.\n2. As you select letters, they will be highlighted in cyan to indicate your current selection.\n3. After you have selected all letters for a word, click the 'Check Selection' button to submit your word for verification.\n4. If the word is correct, it will turn green on the grid. If it's incorrect, the selection will fade away, allowing you to try again.\n5. Each correct word remains highlighted in green.\n6. Aim to find and correctly identify more words than your opponents to win the game.\n\nBest of luck, and enjoy the challenge!";
  alert(gameRules);
}

document.addEventListener("DOMContentLoaded", function () {
  setupEventListeners();
  fetchVersionAndUpdateTitle();
});

function fetchVersionAndUpdateTitle() {
  fetch('/api/version')
    .then(response => response.text())
    .then(version => {
        document.title = `${version}`;  
    })
    .catch(error => console.error('Failed to fetch version:', error));
}

function setupEventListeners() {
  // Login form submission event
  const loginForm = document.getElementById("loginForm");
  if (loginForm) {
    loginForm.addEventListener("submit", function (event) {
      event.preventDefault(); // Prevent form submission
      const username = document.getElementById("username").value;
      connectWebSocket(username);
      updateGameTable([]);
      socket.send("section1");
    });
  }

  // Game reset event
  const resetGameButton = document.getElementById("resetGame");
  if (resetGameButton) {
    resetGameButton.addEventListener("click", function () {
      console.log("[RESETING ALL GAMES] \n Resetting the game");
      socket.send("reset_game:" + "gameroom1");
      socket.send("reset_game:" + "gameroom2");
      socket.send("reset_game:" + "gameroom3");
      socket.send("reset_game:" + "gameroom4");
      socket.send("reset_game:" + "gameroom5");
      alert("Games are being reset and new grid will be generated with all players associated with them removed!\nPlease wait till all player count becomes 0/2\nIt will take 5 seconds (1 second each)\nClick 'OK' to view latest changes.")
    });
  }
}

// Add event listeners for sending words in each game room
addSendButtonListener("gameroom1");
addSendButtonListener("gameroom2");
addSendButtonListener("gameroom3");
addSendButtonListener("gameroom4");
addSendButtonListener("gameroom5");

// GRID
function addSendButtonListener(roomId) {
  const sendButton = document.getElementById(roomId + "_send");
  if (sendButton) {
    sendButton.addEventListener("click", function () {
      sendWords(roomId);
    });
  }
}

function toggleCell(cell, value) {
  let cellId = cell.id;
  if (!confirmedCells.has(cellId)) {
    // Only toggle if not confirmed as correct
    if (cell.style.backgroundColor === "cyan") {
      cell.style.backgroundColor = "";
      removeFromSelected(value);
    } else {
      cell.style.backgroundColor = "cyan";
      addToSelected(value);
    }
  }
}

function addToSelected(word) {
  selectedWords.push(word);
};

function removeFromSelected(word) {
  const index = selectedWords.indexOf(word);
  if (index > -1) {
    selectedWords.splice(index, 1);
  }
}

function sendWords(roomId) {
  const usernameSpan = document.querySelector(".currentUsername");
  if (usernameSpan) {
    const username = usernameSpan.textContent;
    if (selectedWords.length > 0) {
      const message =
        "check_word:" + roomId + ":" + username + ":" + selectedWords.join("");
      lastSentWords = selectedWords.slice(); // Copy of currently selected words
      socket.send(message);
      selectedWords = []; // Optionally clear the selected words array immediately
    }
  } else {
    console.error("Username display element not found");
  }
}

function updateGrid(roomId, gridJson) {
  let gridData = JSON.parse(gridJson);
  let gridHtml = formatGridHtml(gridData);
  document.getElementById(`${roomId}_grid`).innerHTML = gridHtml;
}

function formatGridHtml(grid) {
  let html = '<table class="game-grid">';
  let cellId = 0;
  grid.forEach((row, rowIndex) => {
    html += "<tr>";
    row.forEach((cell, colIndex) => {
      html += `<td id="cell_${rowIndex}_${colIndex}" onclick="toggleCell(this, '${cell}')" class="grid-cell">${cell}</td>`;
    });
    html += "</tr>";
  });
  html += "</table>";
  return html;
}

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
  document.getElementById(roomId + "_player").textContent = player;
  document.getElementById(roomId + "_opponent").textContent = opponent;
  showSection(roomId); // Shows the appropriate game room
  showGameRules();
}

function updateGameTable(gameRooms) {
  const tbody = document.getElementById("gameTableBody");
  tbody.innerHTML = ""; // Clear existing content

  gameRooms.forEach((room) => {
    let row = tbody.insertRow();
    let cellRoom = row.insertCell(0);
    let cellPlayers = row.insertCell(1);
    let cellJoin = row.insertCell(2);
    let cellGridTime = row.insertCell(3);  // Cell for grid generation time


    cellRoom.textContent = room.name;
    cellPlayers.textContent = room.players;
    cellGridTime.textContent = room.gridGenerationTime + " seconds";  // Display grid generation time in milliseconds


    let joinButton = document.createElement("button");
    joinButton.textContent = "Join";
    joinButton.id = "join_" + room.name; // Unique ID for button
    joinButton.addEventListener("click", function () {
      socket.send("join_game:" + room.name);
    });
    cellJoin.appendChild(joinButton);
  });
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

// CHAT

function sendChatMessage(roomId) {
  const input = document.getElementById(roomId + "_chat_input");
  const usernameSpan = document.querySelector(".currentUsername"); // Adjust selector as needed
  const username = usernameSpan.textContent; // Or use .value if it’s an input field
  const message = input.value.trim();

  if (message) {
    const fullMessage = username + ": " + message; // Combine username with message
    socket.send(`chat:${roomId}:${fullMessage}`);
    input.value = ""; // Clear the input after sending
  }
}

function updateChat(roomId, message) {
  const chatBox = document.getElementById(roomId + "_chat");
  const messageDiv = document.createElement("div");
  messageDiv.textContent = message;
  chatBox.appendChild(messageDiv);
}

function appendChatMessage(roomId, message) {
  const chatBox = document.getElementById(roomId + "_chat");
  if (chatBox) {
    const messageDiv = document.createElement("div");
    messageDiv.textContent = message; // Set the message text
    chatBox.appendChild(messageDiv); // Append the new div to the chat box
  } else {
    console.error("Chat box not found for room:", roomId);
  }
}

// leader board
function updateLeaderboard(roomId, scores) {
  const tbody = document
    .getElementById(roomId + "_leaderboard_table")
    .getElementsByTagName("tbody")[0];
  tbody.innerHTML = ""; // Clear existing rows

  Object.entries(scores).forEach(([player, score]) => {
    const row = tbody.insertRow();
    const cellPlayer = row.insertCell(0);
    const cellScore = row.insertCell(1);
    cellPlayer.textContent = player;
    cellScore.textContent = score;

    // debug
    console.log("Player: ", player, " Score: ", score);
  });
}

function extractJson(jsonPart) {
  let braceCount = 0;
  let json = "";

  for (let i = 0; i < jsonPart.length; i++) {
    json += jsonPart[i];
    if (jsonPart[i] === "{") {
      braceCount++;
    } else if (jsonPart[i] === "}") {
      braceCount--;
      if (braceCount === 0) {
        break; // When brace count returns to 0, we've captured a complete JSON object
      }
    }
  }

  return json;
}

function clearSelection() {
  document.querySelectorAll(".grid-cell").forEach((cell) => {
    if (!confirmedCells.has(cell.id)) {
      // Only clear non-confirmed cells
      cell.style.backgroundColor = "";
    }
  });
}


function highlightWords(wordsPositions) {
  wordsPositions.forEach((pos) => {
    let cell = document.getElementById(`cell_${pos[0]}_${pos[1]}`);
    cell.style.backgroundColor = "green";
    confirmedCells.add(cell.id); // Add to confirmed list
  });
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
function updateHighlightedCells(roomId, word, positions) {
  console.log(`Updating highlighted cells for word: ${word} in room: ${roomId}`);
  positions.forEach(pos => {
    const cellId = `cell_${pos[0]}_${pos[1]}`;
    const cell = document.getElementById(cellId);
    if (cell) {
      cell.style.backgroundColor = 'green';  // Highlight the cell
    }
    confirmedCells.add(cell.id); // Add to confirmed list
  });
}


socket.onmessage = function (event) {
  const sectionToShow = event.data;
  // Use a switch case to determine which section to show

  console.log("Received message:", event.data);
  const data = event.data.split(":");
  const command = data[0];
  const content = data.slice(1).join(":"); // Ensure all content after the first colon is included
  let jsonPart = data.slice(2).join(":"); // Join the remaining parts that might contain JSON data

  switch (command) {
    case "update_highlight":
      const roomIdHighlight = data[1];
      const word = data[2];
      let positions = JSON.parse(data[2]);
      updateHighlightedCells(roomIdHighlight, word, positions);
      break;

      case "username_exists":
        alert("This username already exists. Please try a different one.");
        showSection("section0");
        break;

    case "update_words":
      const roomIdWords = data[1];
      const wordsJson = data.slice(2).join(":");
      const wordsList = JSON.parse(wordsJson);
      updateWords(roomIdWords, wordsList);
      break;

    case "update_scores": {
      const roomId = data[1];

      console.log("CONTENT:\n" + content);
      const json = extractJson(jsonPart);
      console.log("JSON:\n" + json);
      const scores = JSON.parse(json);

      console.log("\n\n-- JSON --\n\n" + json);
      console.log("\n\n-- SCORE UPDATE REQUEST --\n\n");
      console.log("Updating scores for room:", roomId);
      console.log("Scores:", scores);

      updateLeaderboard(roomId, scores);
      break;
    }
    case "word_correct":
      console.log("Word is correct: " + content);
      //let positions = JSON.parse(data[2]); // Assuming positions are passed as JSON
      highlightWords(positions, true);
      console.log(positions)
      break;

    case "word_incorrect":
      console.log("Word is incorrect: " + content);
      clearSelection(); // Now only clears unconfirmed selection
      break;

    case "update_grid":
      const roomId = data[1];
      const gridJson = data.slice(2).join(":"); // Assuming grid data is sent as JSON
      updateGrid(roomId, gridJson);
      break;

    case "chat_update":
      if (data.length >= 3) {
        const roomId = data[1];
        const message = data.slice(2).join(":");
        appendChatMessage(roomId, message);
      }
      break;

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

    case "winner":
      alert(data[1] + " wins!"); // Alert user who wins
      break;

    case "update_gameRooms":
      console.log("RECIEVED GAMEROOM UPDATE REQUEST [update_gameRooms]");
      const gameRooms = JSON.parse(content);
      console.log("Updating game rooms with data:", gameRooms);
      updateGameTable(gameRooms);
      break;

    case "chat_update":
      if (data.length >= 3) {
        const roomId = data[1];
        const message = data[2];
        const chatBox = document.getElementById(roomId + "_chat");
        chatBox.innerHTML += "<div>" + message + "</div>"; // Append new message
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
      break;
    case "section3":
      showSection("section3");
      console.log("From the switch case :section3");
      break;
    case "gameroom1":
      showSection("gameroom1");
      console.log("From the switch case :gameroom1");
      break;

    case "gameroom2":
      console.log("From the switch case :gameroom2");
      showSection("gameroom2");
      break;

    case "gameroom3":
      console.log("From the switch case :gameroom3");
      showSection("gameroom3");
      break;

    case "gameroom4":
      console.log("From the switch case :gameroom4");
      showSection("gameroom4");
      break;

    case "gameroom5":
      console.log("From the switch case :gameroom5");
      showSection("gameroom5");
      break;

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

function showSection(sectionId) {
  console.log("Showing section:", sectionId); 
  document
    .querySelectorAll("div[id^='section'], div[id^='gameroom']")
    .forEach((div) => {

      if (!div.id.endsWith('_words_container')) {
        div.classList.add("hidden");
      }
    });

  // Show the current section
  const section = document.getElementById(sectionId);
  if (section) {
    section.classList.remove("hidden");
  } else {
    console.error("No section found with ID:", sectionId);
  }

  // If the section is a game room, also removes the hidden class from its grid, word list, and chat area.. preety smart right?
  if (sectionId.startsWith("gameroom")) {
    const grid = document.getElementById(sectionId + "_grid");
    const words = document.getElementById(sectionId + "_words");
    const chatArea = document.getElementById(sectionId + "_chat");

    if (grid && words && chatArea) {
      grid.classList.remove("hidden");
      words.classList.remove("hidden");
      chatArea.classList.remove("hidden");
    }
  }}