document.addEventListener('DOMContentLoaded', function() {
  const sendButton = document.getElementById('send-button');
  const messageInput = document.getElementById('message-input');
  const chatMessages = document.getElementById('chat-messages');
  const username = sessionStorage.getItem('username');
  const socket = new WebSocket(`ws://localhost:12345`);

  socket.onopen = function() {
    console.log('Connected to the server');
  };

  socket.onmessage = function(event) {
    const message = event.data;
    const messageElement = document.createElement('div');
    messageElement.textContent = message;
    chatMessages.appendChild(messageElement);
  };

  sendButton.addEventListener('click', function() {
    const message = `${username}: ${messageInput.value.trim()}`;
    if (message) {
      socket.send(message);
      messageInput.value = '';
    }
  });
});
