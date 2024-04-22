const socket = new WebSocket("ws://localhost:9180");

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

    case "start_game":
      const room = data[1];
      const player = data[2];
      const opponent = data[3];
      showGameRoom(room, player, opponent);
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

function showSection(sectionId) {
  console.log("Showing section:", sectionId); // Debug: Log which section is being shown
  document.querySelectorAll("div").forEach((div) => {
    div.classList.add("hidden"); // Hide all sections
  });
  const section = document.getElementById(sectionId);
  if (section) {
    section.classList.remove("hidden"); // Show the current section
  } else {
    console.error("No section found with ID:", sectionId); // Error handling if no section is found
  }
}
