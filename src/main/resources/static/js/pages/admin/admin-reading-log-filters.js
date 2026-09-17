export function filterAndSort(logs, {
    query = '', startDate = '', endDate = '', minTime = '', maxTime = '', sort = 'date-desc'
} = {}) {
    const term = query.trim().toLowerCase();
    const filtered = logs.filter((log) => {
        const text = `${log.title} ${log.author} ${log.notes || ''}`.toLowerCase();
        return (!term || text.includes(term)) &&
            (!startDate || log.date >= startDate) &&
            (!endDate || log.date <= endDate) &&
            (!minTime || log.timeSpent >= Number(minTime)) &&
            (!maxTime || log.timeSpent <= Number(maxTime));
    });
    const [field, direction] = sort.split('-');
    return [...filtered].sort((a, b) => {
        const result = field === 'date' ? new Date(a.date) - new Date(b.date)
            : field === 'time' ? a.timeSpent - b.timeSpent
            : field === 'user' ? (a.userName || '').localeCompare(b.userName || '')
            : a.title.localeCompare(b.title);
        return direction === 'desc' ? -result : result;
    });
}
