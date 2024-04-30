document.addEventListener('DOMContentLoaded', function() {
    // Modular function to set up navigation for button clicks
    function setupNavigation(buttonId, targetSection) {
        const button = document.getElementById(buttonId);
        if (button) {
            button.addEventListener('click', function() {
                navigate(targetSection);
            });
        } else {
            console.error('Button not found:', buttonId);
        }
    }

    // Function to navigate to different sections
    function navigate(sectionId) {
        console.log(`Navigating to ${sectionId}`); // Log for debugging
        socket.send(sectionId); // Send section ID to the server
    }

    // Setup navigation for static sections and game rooms
    const navigationMappings = [
        ['btnSection0', 'section0'], ['btnSection1', 'section1'],
        ['btnSection2', 'section2'], ['btnSection3', 'section3'],
        ['btnSection0_sec1', 'section0'], ['btnSection1_sec1', 'section1'],
        ['btnSection2_sec1', 'section2'], ['btnSection3_sec1', 'section3'],
        ['btnSection0_sec2', 'section0'], ['btnSection1_sec2', 'section1'],
        ['btnSection2_sec2', 'section2'], ['btnSection3_sec2', 'section3'],
        ['btnSection0_sec3', 'section0'], ['btnSection1_sec3', 'section1'],
        ['btnSection2_sec3', 'section2'], ['btnSection3_sec3', 'section3'],
        ['gameroom1_exit_bttn', 'section1'], ['gameroom2_exit_bttn', 'section1'],
        ['gameroom3_exit_bttn', 'section1'], ['gameroom4_exit_bttn', 'section1'],
        ['gameroom5_exit_bttn', 'section1'], ['Section1_gameroom1_enter_bttn', 'gameroom1'],
        ['Section1_gameroom2_enter_bttn', 'gameroom2'], ['Section1_gameroom3_enter_bttn', 'gameroom3'],
        ['Section1_gameroom4_enter_bttn', 'gameroom4'], ['Section1_gameroom5_enter_bttn', 'gameroom4'],
        ['Section2_gameroom1_enter_bttn', 'gameroom1'], ['Section2_gameroom2_enter_bttn', 'gameroom2'],
        ['Section2_gameroom3_enter_bttn', 'gameroom3'], ['Section2_gameroom4_enter_bttn', 'gameroom4'],
        ['Section2_gameroom5_enter_bttn', 'gameroom4'], ['Section3_gameroom1_enter_bttn', 'gameroom1'],
        ['Section3_gameroom2_enter_bttn', 'gameroom2'], ['Section3_gameroom3_enter_bttn', 'gameroom3'],
        ['Section3_gameroom4_enter_bttn', 'gameroom4'], ['Section3_gameroom5_enter_bttn', 'gameroom4']
    ];

    // Set up all navigation event listeners
    navigationMappings.forEach(mapping => setupNavigation(mapping[0], mapping[1]));
});
