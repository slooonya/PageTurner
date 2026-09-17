export function openModal(modal) {
    if (!modal) return;

    modal.removeAttribute('hidden');
    document.body.style.overflow = 'hidden';
}

export function closeModal(modal) {
    if (!modal) return;

    modal.setAttribute('hidden', '');

    if (!document.querySelector('.modal:not([hidden])')) {
        document.body.style.overflow = '';
    }
}

export function bindModalEvents() {
    document.querySelectorAll('.modal').forEach((modal) => {
        modal.addEventListener('click', (event) => {
            if (event.target === modal) closeModal(modal);
        });
    });
    document.querySelectorAll('.modal-close').forEach((button) => {
        button.addEventListener('click', () => closeModal(button.closest('.modal')));
    });
}

export function openConfirmationModal({ title, message, confirmText, onConfirm }) {
    const modal = document.getElementById('confirmationModal');
    const confirmButton = document.getElementById('confirmActionBtn');
    const cancelButton = document.getElementById('confirmCancelBtn');
    document.getElementById('confirmationTitle').textContent = title;
    document.getElementById('confirmationMessage').textContent = message;
    confirmButton.textContent = confirmText;
    confirmButton.onclick = async () => {
        confirmButton.disabled = true;
        try { await onConfirm(); closeModal(modal); }
        finally { confirmButton.disabled = false; }
    };
    cancelButton.onclick = () => closeModal(modal);
    openModal(modal);
}
