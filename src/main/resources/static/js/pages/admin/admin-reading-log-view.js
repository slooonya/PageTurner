import { openModal } from './admin-modal.js';

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
    $('detailCurrentPage').textContent = log.currentPage ?? 'N/A';
    $('detailTotalPages').textContent = log.totalPages ?? 'N/A';
    $('detailProgress').textContent = log.totalPages ? `${getProgress(log)}%` : 'N/A';
    $('detailNotes').textContent = log.notes || 'None';
    $('deleteLogBtn').onclick = () => onDelete(log.id);
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
        <div class="log-card-inner">
            <div class="log-card-front">
                <h3>${escapeHtml(log.title)}</h3>
                <p>by ${escapeHtml(log.author)}</p>
                ${log.totalPages ? `
                    <div class="log-progress">
                        <div class="log-progress-bar" style="width: ${progress}%"></div>
                    </div>
                    <p>Progress: ${progress}% (${log.currentPage}/${log.totalPages})</p>` : ''}
                <div class="log-meta">
                    <span>${escapeHtml(log.userName || 'Unknown User')}</span>
                    <span>${formatDate(log.date)}</span>
                </div>
            </div>
            <div class="log-card-back">
                <div>
                    <h3>${escapeHtml(log.title)}</h3>
                    <p>by ${escapeHtml(log.author)}</p>
                    <p>User: ${escapeHtml(log.userName || 'Unknown User')}</p>
                    <p>Date: ${formatDate(log.date)}</p>
                    <p>Time: ${log.timeSpent} min</p>
                    ${log.notes ? `<p>${escapeHtml(truncate(log.notes, 50))}</p>` : ''}
                </div>
                <div class="log-actions">
                    <button class="view-btn btn"><i class="fas fa-eye"></i> Details</button>
                    <button class="edit-btn btn"><i class="fas fa-edit"></i> Edit</button>
                    <button class="delete-btn btn secondary"><i class="fas fa-trash"></i> Delete</button>
                </div>
            </div>
        </div>`;
    card.querySelector('.view-btn').addEventListener('click', () => handlers.onView?.(log.id));
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
