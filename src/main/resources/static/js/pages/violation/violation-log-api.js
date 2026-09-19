const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
const apiBase = '/api/violation-logs';


async function request(path = '', options = {}) {
    const response = await fetch(`${apiBase}${path}`, {
        credentials: 'include',
        ...options,
        headers: {
            ...(options.body ? { 'Content-Type': 'application/json' } : {}),
            ...(csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {}),
            ...options.headers
        }
    });

    if (response.status === 401) {
        window.location.assign('/auth');
        throw new Error('Authentication required.');
    }

    if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        throw new Error(body.error || body.message || 'Request failed.');
    }

    return response.status === 204 ? null : response.json();
}


export const getViolationLogs = () => request();

export const getViolationLog = (id) => request(`/${id}`);

export const updateViolationLog = (id, log) => request(`/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(log)
});

export const restoreViolationLog = (id) => request(`/${id}`, {
    method: 'DELETE'
});
