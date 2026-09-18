import {
    getViolationLogs,
    getViolationLog,
    restoreViolationLog,
    updateViolationLog
} from './violation-log-api.js';

import { filterAndSort } from '../shared/reading-log-filters.js';
import * as form from './violation-log-form.js';
import * as view from './violation-log-view.js';
import { bindModalEvents, closeModal, openConfirmationModal } from '../home/modal.js';
import { showToast } from '../home/toast.js';


const $ = (id) => document.getElementById(id);

const state = {
    logs: []
};


async function loadLogs() {
    view.showLoading();

    try {
        state.logs = await getViolationLogs();
        render();
    } catch (error) {
        console.error('Failed to load violation logs:', error);
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
        onRestore: confirmRestore
    });
}


async function showLogDetails(id) {
    try {
        view.showLogDetails(await getViolationLog(id));
    } catch (error) {
        console.error('Failed to load violation log details:', error);
        showToast(error.message || 'Failed to load violation log details.', 'error');
    }
}


async function editLog(id) {
    try {
        form.populateForm(await getViolationLog(id));
        view.openEditForm();
    } catch (error) {
        console.error('Failed to load violation log for editing:', error);
        showToast(error.message || 'Failed to load violation log.', 'error');
    }
}


async function handleFormSubmit(event) {
    event.preventDefault();

    if (!form.validateForm()) return;

    const id = $('logId').value;
    if (!id) {
        showToast('Select a violation log to edit.', 'error');
        return;
    }

    try {
        await updateViolationLog(id, form.getFormData());
        closeModal($('formModal'));
        await loadLogs();
        showToast('Violation log updated successfully.', 'success');
    } catch (error) {
        console.error('Failed to update violation log:', error);
        handleSaveError(error);
    }
}


function handleSaveError(error) {
    const message = error.message || 'Failed to update violation log.';

    if (message.startsWith('PAGE_COUNT_MISMATCH:')) {
        const [, existingPages, newPages] = message.split(':');
        form.showPageMismatchError(existingPages, newPages);
        return;
    }

    showToast(message, 'error');
}


function confirmRestore(id) {
    openConfirmationModal({
        title: 'Restore Reading Log',
        message: 'Are you sure you want to restore this reading log?',
        confirmText: 'Restore Log',
        onConfirm: () => restoreLog(id)
    });
}


async function restoreLog(id) {
    try {
        await restoreViolationLog(id);
        closeModal($('detailModal'));
        await loadLogs();
        showToast('Reading log restored successfully.', 'success');
    } catch (error) {
        console.error('Failed to restore reading log:', error);
        showToast(error.message || 'Failed to restore reading log.', 'error');
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
    $('AllLogBtn')?.addEventListener('click', loadLogs);
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
