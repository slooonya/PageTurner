import { filterAndSort } from './reading-log-filters.js';
import * as view from './reading-log-view.js';

const state = { logs: [] };
const $ = (id) => document.getElementById(id);

function currentFilters() {
    return {
        query: $('search').value,
        startDate: $('startDate').value,
        endDate: $('endDate').value,
        minTime: $('minTime').value,
        maxTime: $('maxTime').value,
        sort: $('sort').value
    };
}

function renderEntries() {
    view.renderEntries(filterAndSort(state.logs, currentFilters()));
}

function resetFilters() {
    ['search', 'startDate', 'endDate', 'minTime', 'maxTime'].forEach((id) => { $(id).value = ''; });
    $('sort').value = 'date-desc';
    renderEntries();
}

function bindEvents() {
    $('addLogBtn').addEventListener('click', view.openEntryForm);
    $('logForm').addEventListener('submit', (event) => {
        event.preventDefault();
        view.showToast('Saving entries will be available when the backend is connected.', 'info');
    });

    ['search', 'sort'].forEach((id) => $(id).addEventListener(id === 'search' ? 'input' : 'change', renderEntries));
    $('searchBtn').addEventListener('click', renderEntries);
    $('applyFilters').addEventListener('click', renderEntries);
    $('resetFilters').addEventListener('click', resetFilters);
    $('filterToggle').addEventListener('click', () => $('filterControls').classList.toggle('expanded'));

    document.querySelectorAll('[data-modal-close]').forEach((button) => button.addEventListener('click', view.closeAllModals));
    document.querySelectorAll('.modal, .progress-warning-modal').forEach((modal) => {
        modal.addEventListener('click', (event) => { if (event.target === modal) view.closeModal(modal); });
    });
    document.addEventListener('keydown', (event) => { if (event.key === 'Escape') view.closeAllModals(); });
}

bindEvents();
view.closeAllModals();
renderEntries();
