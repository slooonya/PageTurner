import { getReadingLog, getReadingLogHistory } from './reading-log-api.js';
import * as view from './reading-log-view.js';
import { showToast } from './toast.js';


let historyVisible = false;
let currentHistoryLogId = null;

const $ = (id) => document.getElementById(id);

export async function toggleReadingHistory(logId) {
    if (historyVisible && currentHistoryLogId === Number(logId)) {
        resetHistory();
        return;
    }

    historyVisible = false; 
    currentHistoryLogId = Number(logId); 

    view.hideHistory(); 
    view.clearHistory(); 
    view.showHistoryLoading();

    try {
        const log = await getReadingLog(logId);
        if (!log || !log.title || !log.author) throw new Error('Invalid reading log data.');

        const history = await getReadingLogHistory(log.title, log.author, log.id);

        if (currentHistoryLogId !== Number(logId)) return;

        view.renderHistoryList(
            Array.isArray(history) ? history : [], log.id, { onView: handleHistoryItemClick }
        );

        historyVisible = true;
        view.showHistory();

    } catch (error) {
        console.error('Error loading reading history:', error);
        
        historyVisible = false;
        currentHistoryLogId = null;

        view.clearHistory(); 
        view.hideHistory();

        showToast(error.message || 'Failed to load reading history.', 'error');
    }
}


async function handleHistoryItemClick(logId) {
    try {
        const log = await getReadingLog(logId);
        view.showLogDetails(log);
    } catch (error) {
        console.error('Error loading history entry:', error);

        showToast(error.message || 'Failed to load reading entry.', 'error');
    }
}


export async function refreshCurrentHistory() {
    const button = $('viewHistoryBtn');
    const logId = button?.dataset.logId;

    if (!logId || !historyVisible) return;

    try {
        const log = await getReadingLog(logId);

        if (!log || !log.title || !log.author) {
            hideHistory();
            return;
        }

        const history = await getReadingLogHistory(log.title, log.author, log.id);

        view.renderHistoryList(
            Array.isArray(history) ? history : [],
            log.id,
            { onView: handleHistoryItemClick }
        );

    } catch (error) {
        console.error('Error refreshing reading history:', error);
    }
}


export function resetHistory() {
    historyVisible = false; 
    currentHistoryLogId = null; 

    view.hideHistory(); 
    view.clearHistory();
}

export function setCurrentHistoryLog(logId) { 
    if (currentHistoryLogId !== Number(logId)) resetHistory();
}