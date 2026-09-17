import { getAdminReadingLogs, getAdminReadingLog, updateAdminReadingLog, deleteAdminReadingLog } from './admin-reading-log-api.js';
import { filterAndSort } from './admin-reading-log-filters.js';
import * as form from './admin-reading-log-form.js';
import * as view from './admin-reading-log-view.js';
import { bindModalEvents, closeModal, openConfirmationModal } from './admin-modal.js';
import { showToast } from '../home/toast.js';

const $ = (id) => document.getElementById(id);
const state = { logs: [] };

async function loadLogs() {
    view.showLoading();
    try {
        state.logs = await getAdminReadingLogs();
        render();
    } catch (error) {
        console.error('Failed to load admin reading logs:', error);
        view.showLoadError(error.message);
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
    view.renderEntries(filterAndSort(state.logs, getCurrentFilters()), {
        onView: showLogDetails,
        onEdit: editLog,
        onDelete: confirmDelete
    });
}

async function showLogDetails(id) {
    try {
        const log = await getAdminReadingLog(id);
        view.showLogDetails(log, confirmDelete);
    } catch (error) {
        console.error('Failed to load log details:', error);
        showToast(error.message || 'Failed to load reading log details.', 'error');
    }
}

async function editLog(id) {
    try {
        form.populateForm(await getAdminReadingLog(id));
        view.openEditForm();
    } catch (error) {
        console.error('Failed to load reading log for editing:', error);
        showToast(error.message || 'Failed to load reading log.', 'error');
    }
}

async function handleFormSubmit(event) {
    event.preventDefault();
    if (!form.validateForm()) return;

    const id = $('logId').value;
    if (!id) {
        showToast('Select a reading log to edit.', 'error');
        return;
    }

    try {
        await updateAdminReadingLog(id, form.getFormData());
        closeModal($('formModal'));
        await loadLogs();
        showToast('Reading log updated successfully.', 'success');
    } catch (error) {
        console.error('Failed to update reading log:', error);
        handleSaveError(error);
    }
}

function handleSaveError(error) {
    const message = error.message || 'Failed to update reading log.';
    if (message.startsWith('PAGE_COUNT_MISMATCH:')) {
        const [, existingPages, newPages] = message.split(':');
        form.showPageMismatchError(existingPages, newPages);
        return;
    }
    showToast(message, 'error');
}

function confirmDelete(id) {
    openConfirmationModal({
        title: 'Delete Reading Log',
        message: 'Are you sure you want to delete this reading log?',
        confirmText: 'Delete Log',
        onConfirm: () => deleteLog(id)
    });
}

async function deleteLog(id) {
    try {
        await deleteAdminReadingLog(id);
        closeModal($('detailModal'));
        await loadLogs();
        showToast('Reading log deleted successfully.', 'success');
    } catch (error) {
        console.error('Failed to delete reading log:', error);
        showToast(error.message || 'Failed to delete reading log.', 'error');
    }
}

function resetFilters() {
    ['search', 'startDate', 'endDate', 'minTime', 'maxTime'].forEach((id) => {
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

function bindEvents() {
    $('logForm')?.addEventListener('submit', handleFormSubmit);
    $('cancelBtn')?.addEventListener('click', () => closeModal($('formModal')));
    $('search')?.addEventListener('input', render);
    $('searchBtn')?.addEventListener('click', render);
    $('sort')?.addEventListener('change', render);
    $('applyFilters')?.addEventListener('click', render);
    $('resetFilters')?.addEventListener('click', resetFilters);
    $('filterToggle')?.addEventListener('click', toggleFilters);
    $('minTime')?.addEventListener('input', validateTimeInput);
    $('maxTime')?.addEventListener('input', validateTimeInput);
}

bindEvents();
bindModalEvents();
loadLogs();
