document.getElementById('btnSection0').addEventListener('click', () => { navigate('section0'); });
document.getElementById('btnSection1').addEventListener('click', () => { navigate('section1'); });
document.getElementById('btnSection2').addEventListener('click', () => { navigate('section2'); });
document.getElementById('btnSection3').addEventListener('click', () => { navigate('section3'); });

document.getElementById('btnSection0_sec1').addEventListener('click', () => { navigate('section0'); });
document.getElementById('btnSection1_sec1').addEventListener('click', () => { navigate('section1'); });
document.getElementById('btnSection2_sec1').addEventListener('click', () => { navigate('section2'); });
document.getElementById('btnSection3_sec1').addEventListener('click', () => { navigate('section3'); });

document.getElementById('btnSection0_sec2').addEventListener('click', () => { navigate('section0'); });
document.getElementById('btnSection1_sec2').addEventListener('click', () => { navigate('section1'); });
document.getElementById('btnSection2_sec2').addEventListener('click', () => { navigate('section2'); });
document.getElementById('btnSection3_sec2').addEventListener('click', () => { navigate('section3'); });

document.getElementById('btnSection0_sec3').addEventListener('click', () => { navigate('section0'); });
document.getElementById('btnSection1_sec3').addEventListener('click', () => { navigate('section1'); });
document.getElementById('btnSection2_sec3').addEventListener('click', () => { navigate('section2'); });
document.getElementById('btnSection3_sec3').addEventListener('click', () => { navigate('section3'); });

// In-Game room Exit
document.getElementById('gameroom1_exit_bttn').addEventListener('click', () => { navigate('section1'); });
document.getElementById('gameroom2_exit_bttn').addEventListener('click', () => { navigate('section1'); });
document.getElementById('gameroom3_exit_bttn').addEventListener('click', () => { navigate('section1'); });
document.getElementById('gameroom4_exit_bttn').addEventListener('click', () => { navigate('section1'); });


// In-Game room Entry
document.getElementById('Section1_gameroom1_enter_bttn').addEventListener('click', () => { navigate('gameroom1'); });
document.getElementById('Section1_gameroom2_enter_bttn').addEventListener('click', () => { navigate('gameroom2'); });
document.getElementById('Section1_gameroom3_enter_bttn').addEventListener('click', () => { navigate('gameroom3'); });
document.getElementById('Section1_gameroom4_enter_bttn').addEventListener('click', () => { navigate('gameroom4'); });

document.getElementById('Section2_gameroom1_enter_bttn').addEventListener('click', () => { navigate('gameroom1'); });
document.getElementById('Section2_gameroom2_enter_bttn').addEventListener('click', () => { navigate('gameroom2'); });
document.getElementById('Section2_gameroom3_enter_bttn').addEventListener('click', () => { navigate('gameroom3'); });
document.getElementById('Section2_gameroom4_enter_bttn').addEventListener('click', () => { navigate('gameroom4'); });

document.getElementById('Section3_gameroom1_enter_bttn').addEventListener('click', () => { navigate('gameroom1'); });
document.getElementById('Section3_gameroom2_enter_bttn').addEventListener('click', () => { navigate('gameroom2'); });
document.getElementById('Section3_gameroom3_enter_bttn').addEventListener('click', () => { navigate('gameroom3'); });
document.getElementById('Section3_gameroom4_enter_bttn').addEventListener('click', () => { navigate('gameroom4'); });

function navigate(sectionId) {
    socket.send(sectionId); // Send section ID to the server
}