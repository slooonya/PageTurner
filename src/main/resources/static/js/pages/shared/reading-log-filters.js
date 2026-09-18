export function filterAndSort(logs, {
    query = '',
    startDate = '',
    endDate = '',
    minTime = '',
    maxTime = '',
    sort = 'date-desc'
} = {}) {
    const term = query.trim().toLowerCase();

    const filtered = logs.filter((log) => {
        const text = `${log.title || ''} ${log.author || ''} ${log.notes || ''}`.toLowerCase();
        const date = log.date || '';

        return (!term || text.includes(term)) &&
            (!startDate || date >= startDate) &&
            (!endDate || date <= endDate) &&
            (!minTime || Number(log.timeSpent) >= Number(minTime)) &&
            (!maxTime || Number(log.timeSpent) <= Number(maxTime));
    });

    const [field, direction] = sort.split('-');

    return [...filtered].sort((a, b) => {
        const result = field === 'date'
            ? new Date(a.date || a.createdAt) - new Date(b.date || b.createdAt)
            : field === 'time'
                ? Number(a.timeSpent) - Number(b.timeSpent)
                : field === 'user'
                    ? getUserName(a).localeCompare(getUserName(b))
                    : String(a.title || '').localeCompare(String(b.title || ''));

        return direction === 'desc' ? -result : result;
    });
}


function getUserName(log) {
    return String(log.userName || log.username || '');
}
