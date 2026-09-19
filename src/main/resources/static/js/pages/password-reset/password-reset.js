const $ = (id) => document.getElementById(id);

const form = $('resetPasswordForm');
const passwordField = $('password');
const confirmPasswordField = $('confirmPassword');

const requirements = {
    length: $('req-length'),
    uppercase: $('req-uppercase'),
    lowercase: $('req-lowercase'),
    number: $('req-number')
};


function updateRequirement(element, valid) {
    if (!element) return;

    const icon = element.querySelector('.requirement-icon');

    element.classList.toggle('valid', valid);

    if (icon) {
        icon.className = valid
            ? 'fas fa-check requirement-icon'
            : 'fas fa-xmark requirement-icon';
    }
}


function validateRequirements() {
    const password = passwordField.value;

    updateRequirement(
        requirements.length,
        password.length >= 8 && password.length <= 64
    );

    updateRequirement(
        requirements.uppercase,
        /[A-Z]/.test(password)
    );

    updateRequirement(
        requirements.lowercase,
        /[a-z]/.test(password)
    );

    updateRequirement(
        requirements.number,
        /[0-9]/.test(password)
    );
}


function validatePasswordMatch() {
    const matches =
        passwordField.value === confirmPasswordField.value;

    const hasConfirmation =
        confirmPasswordField.value.length > 0;

    if (hasConfirmation && !matches) {
        showPasswordMatchError();
        return false;
    }

    clearPasswordMatchError();
    return true;
}


function showPasswordMatchError() {
    clearPasswordMatchError();

    const errorElement = document.createElement('small');
    errorElement.className = 'inline-error password-match-error';
    errorElement.textContent = 'Passwords do not match';

    confirmPasswordField.parentNode.appendChild(errorElement);
    confirmPasswordField.parentNode.classList.add('error');
}


function clearPasswordMatchError() {
    const errorElement =
        confirmPasswordField.parentNode
            .querySelector('.password-match-error');

    if (errorElement) {
        errorElement.remove();
    }

    confirmPasswordField.parentNode.classList.remove('error');
}


function bindEvents() {
    passwordField?.addEventListener('input', () => {
        validateRequirements();

        if (confirmPasswordField.value) {
            validatePasswordMatch();
        }
    });

    confirmPasswordField?.addEventListener(
        'input',
        validatePasswordMatch
    );

    form?.addEventListener('submit', (event) => {
        validateRequirements();

        if (!validatePasswordMatch()) {
            event.preventDefault();
            confirmPasswordField.focus();
        }
    });
}


bindEvents();