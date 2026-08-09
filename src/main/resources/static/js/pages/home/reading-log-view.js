const $ = (id) => document.getElementById(id);

export function renderEntries(entries) {
    const container = $('logs');
    container.replaceChildren();
    if (!entries.length) {
        container.innerHTML = '<div class="empty-state"><p>No reading entries yet.</p></div>';
    }
}

export function openEntryForm() {
    $('logForm').reset();
    $('formTitle').textContent = 'Add New Reading Log';
    openModal($('formModal'));
}

export function openModal(modal) {
    modal.hidden = false;
    document.body.style.overflow = 'hidden';
}

export function closeModal(modal) {
    modal.hidden = true;
    if (!document.querySelector('.modal:not([hidden]), .progress-warning-modal:not([hidden])')) {
        document.body.style.overflow = '';
    }
}

export function closeAllModals() {
    document.querySelectorAll('.modal, .progress-warning-modal').forEach(closeModal);
}

export function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    $('toastContainer').append(toast);
    setTimeout(() => toast.remove(), 3000);
}
