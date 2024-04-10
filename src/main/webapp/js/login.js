document.addEventListener('DOMContentLoaded', function() {
  const loginButton = document.getElementById('login-button');
  const usernameInput = document.getElementById('username');

  loginButton.addEventListener('click', function() {
    const username = usernameInput.value.trim();
    if (username) {
      // Store the username and navigate to the chat page
      sessionStorage.setItem('username', username);
      window.location.href = '../html/chat.html';
    } else {
      alert('Please enter a username.');
    }
  });

  function saveUser(){
     
  }
});
