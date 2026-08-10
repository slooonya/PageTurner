export function showToast(message, type = 'info', duration = 3000) {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className =
        `toast ${type}`;

    let icon;

    switch (type) {
        case 'success':
            icon = 'fa-check-circle';
            break;

        case 'error':
            icon = 'fa-exclamation-circle';
            break;

        default:
            icon = 'fa-info-circle';
    }

    const iconElement = document.createElement('i');
    iconElement.className = `fas ${icon}`;

    const text = document.createElement('span');
    text.textContent = message;

    toast.append(iconElement, text);

    container.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('toast-out');

        setTimeout(() => {
            toast.remove();
        }, 300);

    }, duration);
}