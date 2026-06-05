function updateClock() {
    const clockElement = document.getElementById('clock');
    if (clockElement) {
        const now = new Date();
        clockElement.textContent = now.toLocaleString();
    }
}

updateClock();
setInterval(updateClock, 9000); // каждые 9 секунд