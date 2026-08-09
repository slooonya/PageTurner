const backButton = document.getElementById('backButton');

if (backButton && window.history.length <= 1) {
    backButton.disabled = true;
    backButton.title = 'There is no previous page to return to.';
}

backButton?.addEventListener('click', () => window.history.back());
