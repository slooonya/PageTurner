import { openModal } from '../home/modal.js';

const $ = (id) => document.getElementById(id);

export function showLoading() {
    $('logs').innerHTML = '<div class="loading">Loading logs...</div>';
}

export function showLoadError(message) {
    const container = $('logs');
    container.replaceChildren();
    const error = document.createElement('div');
    error.className = 'empty-state';
    error.textContent = `Error loading logs: ${message}`;
    container.append(error);
}

export function renderEntries(logs, handlers = {}) {
    const container = $('logs');
    container.replaceChildren();
    if (!logs.length) {
        renderEmptyState(container);
        return;
    }
    logs.forEach((log) => container.append(createLogCard(log, handlers)));
}

export function showLogDetails(log, onDelete) {
    $('detailTitle').textContent = log.title;
    $('detailUser').textContent = log.userName || 'Unknown';
    $('detailAuthor').textContent = log.author;
    $('detailDate').textContent = formatDate(log.createdAt || log.date);
    $('detailTime').textContent = `${log.timeSpent} minutes`;
    $('detailProgress').textContent = log.totalPages ? `${getProgress(log)}%` : 'N/A';
    $('detailNotes').textContent = log.notes || 'None';
    openModal($('detailModal'));
}

export function openEditForm() {
    openModal($('formModal'));
}


function createLogCard(log, handlers) {
    const progress = getProgress(log);
    const card = document.createElement('div');
    card.className = 'log-card';
    card.innerHTML = `
            <div class="log-card-header">
                <h3>${escapeHtml(log.title)}</h3>
                <p class="log-author"><em>by ${escapeHtml(log.author)}</em></p>
            </div>

           ${log.totalPages ? `
                <div class="log-progress">
                    <div class="log-progress-bar" style="width: ${progress}%"></div>
                </div>
                <p>${progress}% (${log.currentPage}/${log.totalPages})</p>` : ''}

            <div class="log-meta">
                <div class="log-date">
                    <i class="fas fa-calendar"></i>
                    <span>${formatDate(log.date)}</span>
                </div>

                <div class="log-date">
                    <i class="fas fa-user"></i>
                    <span>${escapeHtml(log.userName || 'Unknown User')}</span>
                </div>
            </div>

            <div class="log-notes">
                <h4>Notes:</h4>
                ${log.notes ? `<p>${escapeHtml(truncate(log.notes, 50))}</p>` : ''}
            </div>

            <div class="log-actions">
                <button class="btn primary details-btn small">
                    <i class="fas fa-eye"></i>
                    Details
                </button>

                <button class="btn primary edit-btn small">
                    <i class="fas fa-pen"></i>
                    Edit
                </button>

                <button class="btn cancel delete-btn small">
                    <i class="fas fa-trash"></i>
                    Delete
                </button>
            </div>`;
    card.querySelector('.details-btn').addEventListener('click', () => handlers.onView?.(log.id));
    card.querySelector('.edit-btn').addEventListener('click', () => handlers.onEdit?.(log.id));
    card.querySelector('.delete-btn').addEventListener('click', () => handlers.onDelete?.(log.id));
    return card;
}

function renderEmptyState(container) {
    const empty = document.createElement('div');
    empty.className = 'empty-state search-empty';
    empty.innerHTML = '<i class="fas fa-search fa-2x"></i><h3>No matching logs found</h3><p>Try different search terms</p>';
    container.append(empty);
}

function getProgress(log) {
    if (!log.totalPages) return 0;
    return Math.min(100, Math.max(0, Math.round((log.currentPage / log.totalPages) * 100)));
}

function truncate(value, length) {
    return value.length > length ? `${value.slice(0, length)}...` : value;
}

function formatDate(value) {
    if (!value) return 'N/A';
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString();
}

function escapeHtml(value) {
    const div = document.createElement('div');
    div.textContent = value ?? '';
    return div.innerHTML;
}
