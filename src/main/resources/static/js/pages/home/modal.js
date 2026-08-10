export function openModal(modal) {
    if (!modal) {
        console.error('openModal: modal element not found');
        return;
    }

    modal.removeAttribute('hidden');
    document.body.style.overflow = 'hidden';

    console.log('Opened modal:', modal.id);
}

export function closeModal(modal) {
    if (!modal) return;

    modal.setAttribute('hidden', '');

    if (!document.querySelector(
        '.modal:not([hidden]), .progress-warning-modal:not([hidden])'
    )) {
        document.body.style.overflow = '';
    }
}

export function closeAllModals() {
    document
        .querySelectorAll('.modal, .progress-warning-modal')
        .forEach((modal) => {
            modal.setAttribute('hidden', '');
        });

    document.body.style.overflow = '';
}


export function bindModalEvents() {

    document.querySelectorAll('[data-modal-close]').forEach((button) => {
        button.addEventListener('click', (event) => {
            event.preventDefault();
            closeAllModals();
        });
    });

    document.querySelectorAll('.modal, .progress-warning-modal')
        .forEach((modal) => {
            modal.addEventListener('click', (event) => {
                if (event.target === modal) {
                    closeModal(modal);
                }
            });
        });

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            closeAllModals();
        }
    });
}


export function openConfirmationModal({
    title = 'Confirm Action',
    message = 'Are you sure?',
    confirmText = 'Confirm',
    cancelText = 'Cancel',
    onConfirm
}) {
    const modal = document.getElementById('confirmationModal');

    if (!modal) {
        console.error(
            'Confirmation modal not found. Check that #confirmationModal exists in the DOM.'
        );
        return;
    }

    const titleElement = document.getElementById('confirmationTitle');
    const messageElement = document.getElementById('confirmationMessage');
    const confirmButton = document.getElementById('confirmActionBtn');
    const cancelButton = document.getElementById('confirmCancelBtn');

    if (titleElement) {
        titleElement.textContent = title;
    }

    if (messageElement) {
        messageElement.textContent = message;
    }

    if (confirmButton) {
        confirmButton.innerHTML = `
            <i class="fas fa-trash"></i> ${confirmText}
        `;

        confirmButton.onclick = null;

        confirmButton.onclick = async (event) => {
            event.preventDefault();

            try {
                confirmButton.disabled = true;

                if (onConfirm) {
                    await onConfirm();
                }

                closeModal(modal);

            } catch (error) {
                console.error('Confirmation action failed:', error);
            } finally {
                confirmButton.disabled = false;
            }
        };
    }

    if (cancelButton) {
        cancelButton.textContent = cancelText;
    }

    openModal(modal);
}