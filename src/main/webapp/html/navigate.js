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


function navigate(sectionId) {
    socket.send(sectionId); // Send section ID to the server
}