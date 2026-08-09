const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
const headers = (json = false) => ({
    ...(json ? { 'Content-Type': 'application/json' } : {}),
    ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {}) 
});

async function request(url, options) {
    const response = await fetch(url, { credentials: 'same-origin', ...options });
    if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        throw new Error(body.error || 'Something went wrong.');
    }

    return response.status === 204 ? null : response.json();
}

function toast(message, type = 'success') {
    const element = document.createElement('div');
    element.className = `toast ${type}`;
    element.textContent = message;
    document.getElementById('toastContainer').append(element);
    setTimeout(() => element.remove(), 3000);
}

document.getElementById('profileForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    try {
        const result = await request('/api/profile', {
            method: 'PATCH',
            headers: headers(true),
            body: JSON.stringify({ 
                username: document.getElementById('username').value, 
                bio: document.getElementById('bio').value 
            })
        });

        document.querySelector('.profile-header h1').textContent = result.username;
        toast('Profile updated.');
    } catch (error) { 
        toast(error.message, 'error'); 
    }
});

document.getElementById('passwordForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    try {
        await request('/api/profile/password', {
            method: 'POST',
            headers: headers(true),
            body: JSON.stringify({
                currentPassword: document.getElementById('currentPassword').value,
                newPassword: document.getElementById('newPassword').value,
                confirmPassword: document.getElementById('confirmPassword').value
            })
        });
        event.currentTarget.reset(); 
        toast('Password changed.');
    } catch (error) { 
        toast(error.message, 'error'); 
    }
});

document.getElementById('avatarInput').addEventListener('change', async (event) => {
    const [file] = event.target.files;
    if (!file) return;

    const form = new FormData(); 
    form.append('avatar', file);

    try {
        const result = await request('/api/profile/avatar', { 
            method: 'POST', 
            headers: headers(), 
            body: form 
        });

        let image = document.getElementById('avatarImage');
        if (!image) {
            image = document.createElement('img'); 
            image.id = 'avatarImage'; 
            document.querySelector('.avatar').replaceChildren(image); 
        }

        image.src = `${result.avatarUrl}?v=${Date.now()}`; 
        toast('Profile picture updated.');
    } catch (error) { 
        toast(error.message, 'error'); 
    }
});
