const socket = new WebSocket("ws://localhost:9180");

document.getElementById("loginForm").addEventListener("submit", (event) => {
  event.preventDefault(); // Prevent form submission

  const username = document.getElementById("username").value;
  connectWebSocket(username);
});

// connect and send the username to the server
function connectWebSocket(username) {
    console.log("Attempting to add new player");
    // Send the username to the server
    try {
        socket.send("new_player:"+username);
        console.log("New player added: ", username);
    }
    catch (error) {
        console.log("Error adding new player: ", error);
    }
}

socket.onopen = function (event) {
  console.log("WebSocket connection established");
};

socket.onmessage = function (event) {
  const sectionToShow = event.data;
  // Use a switch case to determine which section to show
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

socket.onerror = function (error) {
  console.log("WebSocket Error: ", error);
};

socket.onclose = function (event) {
    console.log("WebSocket connection closed", event);
    // Send a message that the user left, including the username
    if (username) {
        socket.send("user_left:" + username);
    }
};

function showSection(sectionId) {
  // Hide all sections
  document.querySelectorAll("div").forEach((div) => {
    div.classList.add("hidden");
  });

  // Show the specified section
  const section = document.getElementById(sectionId);
  if (section) {
    section.classList.remove("hidden");
  }
}
