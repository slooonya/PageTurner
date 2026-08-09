const rules = {
    username: (value) => !value ? 'Username is required.' : /^[a-zA-Z0-9_]{4,20}$/.test(value) ? '' : 'Use 4–20 letters, numbers, or underscores.',
    email: (value) => !value ? 'Email is required.' : /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value) ? '' : 'Enter a valid email address.',
    password: (value) => !value ? 'Password is required.' : /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,64}$/.test(value) ? '' : 'Use 8–64 characters with upper/lowercase letters and a number.'
};

export function validateField(field, password) {
    if (field.id === 'confirmPassword') {
        return !field.value ? 'Please confirm your password.' : field.value === password ? '' : 'Passwords do not match.';
    }
    
    return rules[field.id]?.(field.value.trim()) ?? '';
}
