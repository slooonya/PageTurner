import { validateField } from './auth-validation.js';

const $ = (selector) => document.querySelector(selector);
const signUpButton = $('#sign-up-btn');
const signInButton = $('#sign-in-btn');
const container = $('.auth-container');
const signUpForm = $('.sign-up-form');
const password = $('#password');
const confirmation = $('#confirmPassword');

signUpButton?.addEventListener('click', () => container?.classList.add('sign-up-mode'));
signInButton?.addEventListener('click', () => container?.classList.remove('sign-up-mode'));

function setFieldError(field, message) {
    const wrapper = field.closest('.input-field');
    let feedback = wrapper?.querySelector('.inline-error');

    if (!feedback && wrapper) {
        feedback = document.createElement('small');
        feedback.className = 'inline-error';
        wrapper.append(feedback);
    }
    wrapper?.classList.toggle('error', Boolean(message));

    if (feedback) feedback.textContent = message;

    return !message;
}

function validate(field) {
    return setFieldError(field, validateField(field, password?.value ?? ''));
}

signUpForm?.querySelectorAll('input:not([type="file"])').forEach((field) => {
    field.addEventListener('blur', () => validate(field));
    field.addEventListener('input', () => {
        validate(field);
        if (field === password && confirmation?.value) validate(confirmation);
    });
});

signUpForm?.addEventListener('submit', (event) => {
    const valid = [...signUpForm.querySelectorAll('input:not([type="file"])')].every(validate);

    if (!valid) {
        event.preventDefault();
        signUpForm.querySelector('.input-field.error input')?.focus();
    }
});

const avatarInput = $('#avatar');
const avatarPreview = $('#avatarPreview');
avatarInput?.addEventListener('change', () => {
    const [file] = avatarInput.files;
    if (!file) { avatarPreview.hidden = true; return; }

    const supported = ['image/jpeg', 'image/png'].includes(file.type);
    if (!supported || file.size > 5 * 1024 * 1024) {
        avatarInput.value = '';
        avatarPreview.hidden = true;
        setFieldError(avatarInput, supported ? 'Choose an image smaller than 5 MB.' : 'Choose a PNG or JPEG image.');
        return;
    }
    
    setFieldError(avatarInput, '');
    avatarPreview.src = URL.createObjectURL(file);
    avatarPreview.hidden = false;
});
