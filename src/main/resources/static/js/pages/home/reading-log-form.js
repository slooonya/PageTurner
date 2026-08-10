const $ = (id) => document.getElementById(id);


export function getFormData() {
    return {
        title: $('title').value.trim(),
        author: $('author').value.trim(),
        date: $('date').value,
        timeSpent: parseInt($('timeSpent').value, 10),
        currentPage: parseInt($('currentPage').value, 10) || null,
        totalPages: parseInt($('totalPages').value, 10) || null,
        notes: $('notes').value.trim()
    };
}


export function resetForm() {
    const form = $('logForm');
    form?.reset();

    $('logId').value = '';
    $('formTitle').textContent = 'Add New Reading Log';

    const submitButton = form?.querySelector('button[type="submit"]');
    if (submitButton) submitButton.textContent = 'Add Log';

    clearErrors();
}


export function populateForm(log) {
    $('logId').value = log.id;
    $('title').value = log.title;
    $('author').value = log.author;
    $('date').value = log.date ? log.date.split('T')[0] : '';
    $('timeSpent').value = log.timeSpent ?? '';
    $('currentPage').value = log.currentPage ?? '';
    $('totalPages').value = log.totalPages ?? '';
    $('notes').value = log.notes || '';
    $('formTitle').textContent = 'Edit Reading Log';

    const submitButton = $('logForm')?.querySelector('button[type="submit"]');
    if (submitButton) submitButton.textContent = 'Update Log';

    clearErrors();
}


export function validateForm() {
    clearErrors();

    let isValid = true;

    const dateInput = $('date').value;

    if (dateInput) {
        const selectedDate = new Date(dateInput);
        const today = new Date();

        selectedDate.setHours(0, 0, 0, 0);
        today.setHours(0, 0, 0, 0);

        if (selectedDate > today) {
            showError('date', 'Date cannot be in the future');

            isValid = false;
        }
    }

    const currentPage = parseInt($('currentPage').value, 10);
    const totalPages = parseInt($('totalPages').value, 10);

    if (currentPage && totalPages && currentPage > totalPages) {
        showError('currentPage', 'Current page cannot exceed total pages');
        isValid = false;
    }

    if (!$('title').value.trim()) {
        showError('title', 'Title is required');
        isValid = false;
    }

    if (!$('author').value.trim()) {
        showError('author', 'Author is required');
        isValid = false;
    }

    return isValid;
}


export function hasChanges(original, updated) {
    if (!original) return true;

    return (
        original.title !== updated.title ||
        original.author !== updated.author ||
        original.date !== updated.date ||
        original.timeSpent !== updated.timeSpent ||
        original.currentPage !== updated.currentPage ||
        original.totalPages !== updated.totalPages ||
        original.notes !== updated.notes
    );
}


export function findPageMismatch(logs, logData) {
    const existingLogs =
        logs.filter((log) =>
            log.title.toLowerCase() === logData.title.toLowerCase() &&
            log.author.toLowerCase() === logData.author.toLowerCase()
        );

    if (!existingLogs.length) return null;

    return existingLogs.find((log) =>
        log.totalPages &&
        logData.totalPages &&
        log.totalPages !== logData.totalPages
    ) || null;
}


export function clearErrors() {
    document.querySelectorAll('.error-message')
        .forEach((element) => { element.textContent = ''; element.style.display = 'none'; });

    document.querySelectorAll('.error-input')
        .forEach((element) => { element.classList.remove('error-input'); });
}


export function showError(fieldId, message) {
    const field = $(fieldId);
    if (!field) return;

    let errorElement = document.getElementById(`${fieldId}-error`);
    if (!errorElement) {
        errorElement = document.createElement('div');
        errorElement.id = `${fieldId}-error`;
        errorElement.className = 'error-message';
        field.parentNode.insertBefore(errorElement, field.nextSibling);
    }

    field.classList.add('error-input');
    errorElement.textContent = message;
    errorElement.style.display = 'block';
}


export function showPageMismatchError(existingPages, newPages) {
    clearErrors();

    const totalPagesInput = $('totalPages');
    const errorElement = $('totalPages-error');

    if (!totalPagesInput || !errorElement) return;

    totalPagesInput.classList.add('error-input');

    errorElement.textContent =
        `This book is already recorded with ${existingPages} pages. ` +
        `You cannot change it to ${newPages} pages.`;

    errorElement.style.display ='block';

    totalPagesInput.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
    });
}