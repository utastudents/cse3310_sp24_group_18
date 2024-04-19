const socket = new WebSocket("ws://localhost:9180");

document.getElementById("loginForm").addEventListener("submit", (event) => {
  event.preventDefault(); // Prevent form submission

  const username = document.getElementById("username").value;
  connectWebSocket(username);
  showSection("section1");
});

document.getElementById("gameLobby").addEventListener("submit", (event) => {
  event.preventDefault(); // Prevent form submission

  const lobbyName = document.getElementById("lobbyName").value;
  createGameLobby(lobbyName);
});

function createGameLobby(lobbyName) {
  console.log("Attempting to create a new game lobby");
  // Send the lobby name to the server
  try {
    socket.send("new_lobby:" + lobbyName);
    console.log("New lobby created: ", lobbyName);
  } catch (error) {
    console.log("Error creating new lobby: ", error);
  }
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
      console.log("section1");
      break;
    case "section2":
      showSection("section2");
      console.log("section2");
      break;
    case "section3":
      showSection("section3");
      console.log("section3");
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
      console.log("No such section exists");
      break;
  }
};

socket.onerror = function(event) {
    console.error("WebSocket error observed:", event);
  };

  socket.onclose = function(event) {
    console.log("WebSocket connection closed", event.code, event.reason);
  };

function showSection(sectionId) {
  // Hide all sections
  document.querySelectorAll("div").forEach((div) => {
    if (div.id !== 'currentUser') { // Check if the id is not 'currentUser'
        div.classList.add("hidden");
      }
  });

  // Show the specified section
  const section = document.getElementById(sectionId);
  if (section) {
    section.classList.remove("hidden");
  }
}
