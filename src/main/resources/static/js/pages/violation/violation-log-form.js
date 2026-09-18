const $ = (id) => document.getElementById(id);


export function getFormData() {
    return {
        title: $('title').value.trim(),
        author: $('author').value.trim(),
        date: $('date').value,
        timeSpent: parseInt($('timeSpent').value, 10),
        currentPage: parseInt($('currentPage').value, 10),
        totalPages: parseInt($('totalPages').value, 10),
        notes: $('notes').value.trim()
    };
}


export function populateForm(log) {
    $('logId').value = log.id;
    $('title').value = log.title || '';
    $('author').value = log.author || '';
    $('date').value = log.date || '';
    $('timeSpent').value = log.timeSpent ?? '';
    $('currentPage').value = log.currentPage ?? '';
    $('totalPages').value = log.totalPages ?? '';
    $('notes').value = log.notes || '';
    clearErrors();
}


export function validateForm() {
    clearErrors();

    const data = getFormData();
    let valid = true;

    if (!data.title) { showError('title', 'Title is required'); valid = false; }
    if (!data.author) { showError('author', 'Author is required'); valid = false; }
    if (!data.date) { showError('date', 'Date is required'); valid = false; }
    if (!Number.isInteger(data.timeSpent) || data.timeSpent < 1) { showError('timeSpent', 'Enter a valid time'); valid = false; }
    if (!Number.isInteger(data.currentPage) || data.currentPage < 1) { showError('currentPage', 'Enter a valid page'); valid = false; }
    if (!Number.isInteger(data.totalPages) || data.totalPages < 1) { showError('totalPages', 'Enter a valid page count'); valid = false; }
    if (data.currentPage > data.totalPages) { showError('currentPage', 'Current page cannot exceed total pages'); valid = false; }

    return valid;
}


export function clearErrors() {
    document.querySelectorAll('.error-message').forEach((element) => {
        element.textContent = '';
        element.style.display = 'none';
    });

    document.querySelectorAll('.error-input').forEach((element) => {
        element.classList.remove('error-input');
    });
}


export function showPageMismatchError(existingPages, newPages) {
    showError(
        'totalPages',
        `This book is already recorded with ${existingPages} pages. You cannot change it to ${newPages} pages.`
    );
}


function showError(fieldId, message) {
    const field = $(fieldId);
    const error = $(`${fieldId}-error`);

    if (!field || !error) return;

    field.classList.add('error-input');
    error.textContent = message;
    error.style.display = 'block';
}
