const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

async function request(path, options = {}) {
    const response = await fetch(`/api/reading-logs${path}`, {
        credentials: 'include',
        ...options,
        headers: {
            ...(options.body ? { 'Content-Type': 'application/json' } : {}),
            ...(csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {}),
            ...options.headers
        }
    });

    if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        throw new Error(body.error || body.message || 'Request failed.');
    }
    
    return response.status === 204 ? null : response.json();
}

export const getReadingLogs = 
    () => request('');

export const getReadingLog = 
    (id) => request(`/${id}`);

export const createReadingLog = 
    (log) => request('', { 
        method: 'POST', 
        body: JSON.stringify(log) 
    });

export const updateReadingLog = 
    (id, log) => request(`/${id}`, { 
        method: 'PUT', 
        body: JSON.stringify(log) 
    });

export const deleteReadingLog = 
    (id) => request(`/${id}`, { 
        method: 'DELETE' 
    });

export const getReadingLogHistory = 
    (title, author, currentLogId) => request(
        `/history?title=${encodeURIComponent(title)}&author=${encodeURIComponent(author)}&currentLogId=${currentLogId}`
    );
