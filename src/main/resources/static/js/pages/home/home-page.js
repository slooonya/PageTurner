import {
    getReadingLogs,
    getReadingLog,
    createReadingLog,
    updateReadingLog,
    deleteReadingLog
} from './reading-log-api.js';

import { filterAndSort } from './reading-log-filters.js';
import * as view from './reading-log-view.js';
import * as form from './reading-log-form.js';
import { toggleReadingHistory, resetHistory, refreshCurrentHistory } from './reading-log-history.js';
import { showToast } from './toast.js';
import { openModal, closeAllModals, bindModalEvents, openConfirmationModal } from './modal.js';


const $ = (id) => document.getElementById(id);

const state = { 
    logs: [],
    currentHistoryLogId: null
};


async function loadLogs() {
    try { 
        state.logs = await getReadingLogs(); 
        render(); 
    } catch (error) { 
        console.error('Failed to load reading logs:', error); 
        showToast('Failed to load reading entries', 'error'); 
    }
}


function getCurrentFilters() {
    return {
        query: $('search')?.value || '',
        startDate: $('startDate')?.value || '',
        endDate: $('endDate')?.value || '',
        minTime: $('minTime')?.value || '',
        maxTime: $('maxTime')?.value || '',
        sort: $('sort')?.value || 'date-desc'
    };
}


function render() {
    const filteredLogs = filterAndSort(state.logs, getCurrentFilters());

    view.renderEntries(
        filteredLogs, {
            onAdd:openNewLogForm,
            onView: showLogDetails,
            onEdit: editLog,
            onDelete: handleDelete
        }
    );
}


async function handleEdit(id) { 
    try { 
        const log = await getReadingLog(id);
        const form = $('logForm');
        $('logId').value = log.id; 
        $('title').value = log.title || ''; 
        $('author').value = log.author || ''; 
        $('totalPages').value = log.totalPages ?? ''; 
        $('date').value = log.date || ''; 
        $('timeSpent').value = log.timeSpent ?? 0;
        $('currentPage').value = log.currentPage ?? ''; 
        $('notes').value = log.notes || ''; 
        $('formTitle').textContent = 'Edit Reading Log'; 
        
        const submitButton = form?.querySelector('button[type="submit"]'); 
        if (submitButton) submitButton.textContent = 'Save Changes'; 
        
        view.openModal($('formModal')); 
    } catch (error) { 
        console.error('Failed to load reading log for editing:', error); 
        showToast(error.message || 'Failed to load reading entry', 'error'); 
    } 
}


function handleDelete(id) { 
    openConfirmationModal({ 
        title: 'Delete Reading Log', 
        message: 'Are you sure you want to delete this reading log? This action cannot be undone.', 
        confirmText: 'Delete', 
        cancelText: 'Cancel', 
        onConfirm: () => deleteLog(id)
    }); 
}


function resetFilters() {
    ['search', 'startDate', 'endDate', 'minTime', 'maxTime']
        .forEach((id) => {
            const element = $(id);
            if (element) element.value = '';
        });

    if ($('sort')) $('sort').value = 'date-desc';

    render();
}


function toggleFilters() {
    $('filterControls')?.classList.toggle('expanded');
    $('filterToggle')?.classList.toggle('active');
}


function validateTimeInput(event) {
    if (Number(event.target.value) < 0) event.target.value = 0;
}


function openNewLogForm() {
    form.resetForm();
    openModal($('formModal'));
}


async function showLogDetails(logOrId) {
    try {
        let log;

        if (typeof logOrId === 'object')
            log = logOrId;
        else log = await getReadingLog(logOrId);

        state.currentHistoryLogId = log.id;

        view.showLogDetails(log);
    } catch (error) {
        console.error('Failed to load log details:', error);
        showToast(error.message || 'Failed to load log details', 'error');
    }
}


async function editLog(id) {
    try {
        const log = await getReadingLog(id);
        form.populateForm(log);
        openModal($('formModal'));
    } catch (error) {
        console.error('Failed to load log for editing:', error);
        showToast(error.message || 'Failed to load reading entry', 'error');
    }
}


async function createLog(logData) {
    try {
        await createReadingLog(logData);
        closeAllModals();
        await loadLogs();
        showToast('Reading log created successfully', 'success');
    } catch (error) {
        console.error('Error creating reading log:', error);
        handleSaveError(error);
    }
}


async function updateLog(id, logData) {
    const originalLog = state.logs.find((log) => String(log.id) === String(id));
    if (originalLog && !form.hasChanges(originalLog, logData)) {
        closeAllModals();
        return;
    }

    try {
        await updateReadingLog(id, logData);
        closeAllModals();
        await loadLogs();
        showToast('Reading log updated successfully', 'success');
    } catch (error) {
        console.error('Error updating reading log:', error);
        handleSaveError(error);
    }
}


function handleSaveError(error) {
    const message = error.message || 'Failed to save reading log.';
    if (message.startsWith('PAGE_COUNT_MISMATCH:')) {
        const parts = message.split(':');
        form.showPageMismatchError(parts[1], parts[2]);
        return;
    }

    showToast(message, 'error');
}


async function deleteLog(id) {
    try {
        await deleteReadingLog(id);

        state.logs = await getReadingLogs();
        render();

        if (String(state.currentHistoryLogId) === String(id)) {
            state.currentHistoryLogId = null;
            closeAllModals();
            resetHistory();
        } else {
            await refreshCurrentHistory();
        }

        showToast('Reading entry deleted', 'success');

    } catch (error) {
        console.error('Failed to delete reading log:', error);

        showToast(
            error.message || 'Failed to delete reading entry.',
            'error'
        );
    }
}


async function handleFormSubmit(event) { 
    event.preventDefault(); 
    const id = $('logId').value; 
    const data = form.getFormData(); 
    
    try { 
        if (id) { 
            await updateReadingLog(id, data); 
            showToast('Reading entry updated', 'success'); 
        } else { 
            await createReadingLog(data); 
            showToast('Reading entry saved', 'success'); 
        } 
        
        state.logs = await getReadingLogs(); 
        closeAllModals(); 
        render();
        await refreshCurrentHistory(); 
    } catch (error) { 
        console.error('Failed to save reading log:', error); 
        showToast(error.message || 'Failed to save entry.', 'error'); 
    } 
}


function handleHistoryClick() {
    const button = $('viewHistoryBtn');
    const logId = button?.dataset.logId;

    if (!logId) {
        showToast('Unable to identify this reading log', 'error');
        return;
    }

    toggleReadingHistory(logId);
}


function setupModalCloseBehavior() {
    bindModalEvents();
}


function bindEvents() {
    $('addLogBtn')?.addEventListener('click', openNewLogForm);
    $('logForm')?.addEventListener('submit', handleFormSubmit);
    $('search')?.addEventListener('input', render);
    $('searchBtn')?.addEventListener('click', render);
    $('sort')?.addEventListener('change', render);
    $('applyFilters')?.addEventListener('click', render);
    $('resetFilters')?.addEventListener('click', resetFilters);
    $('filterToggle')?.addEventListener('click', toggleFilters);
    $('minTime')?.addEventListener('input', validateTimeInput);
    $('maxTime')?.addEventListener('input', validateTimeInput);
    $('viewHistoryBtn')?.addEventListener('click', handleHistoryClick);
}

bindEvents();
bindModalEvents();
resetHistory();
closeAllModals();
setupModalCloseBehavior();
loadLogs();
