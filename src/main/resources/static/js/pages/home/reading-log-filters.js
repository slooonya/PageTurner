export function filterAndSort(
    logs, 
    { 
        query = '', 
        startDate = '',
        endDate = '', 
        minTime = '', 
        maxTime = '', 
        sort = 'date-desc'
    }
) {
    const term = query.trim().toLowerCase();

    const filtered = logs.filter((log) => {
        const text = `${log.title} ${log.author} ${log.notes || ''}`.toLowerCase();
        const date = new Date(log.date);

        return (!term || text.includes(term)) &&
            (!startDate || date >= new Date(startDate)) &&
            (!endDate || date <= new Date(`${endDate}T23:59:59`)) &&
            (!minTime || log.timeSpent >= Number(minTime)) &&
            (!maxTime || log.timeSpent <= Number(maxTime));
    });

    const [field, direction] = sort.split('-');
    
    return [...filtered].sort((a, b) => {
        const result = field === 'date' ? new Date(a.date) - new Date(b.date)
            : field === 'time' ? a.timeSpent - b.timeSpent
            : a.title.localeCompare(b.title);
        return direction === 'desc' ? -result : result;
    });
}
