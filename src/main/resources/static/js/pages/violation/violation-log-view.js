import { openModal } from '../home/modal.js';


const $ = (id) => document.getElementById(id);


export function showLoading() {
    $('logs').innerHTML = '<div class="violation-loading">Loading violation logs...</div>';
}


export function showLoadError(message) {
    const container = $('logs');
    container.replaceChildren();

    const error = document.createElement('div');
    error.className = 'violation-empty-state';
    error.textContent = `Error loading violation logs: ${message}`;
    container.append(error);
}


export function renderEntries(logs, handlers = {}) {
    const container = $('logs');
    container.replaceChildren();

    if (!logs.length) {
        const empty = document.createElement('div');
        empty.className = 'violation-empty-state';
        empty.textContent = 'No violation logs found.';
        container.append(empty);
        return;
    }

    logs.forEach((log) => container.append(createLogEntry(log, handlers)));
}


export function showLogDetails(log) {
    $('detailId').textContent = log.id;
    $('detailTitleText').textContent = log.title;
    $('detailAuthor').textContent = log.author;
    $('detailDate').textContent = formatDate(log.createdAt || log.date);
    $('detailTime').textContent = `${log.timeSpent} minutes`;
    $('detailNotes').textContent = log.notes || 'None';
    $('detailUser').textContent = log.username || 'Unknown';
    openModal($('detailModal'));
}


export function openEditForm() {
    openModal($('formModal'));
}


function createLogEntry(log, handlers) {
    const progress = getProgress(log);
    const card = document.createElement('article');
    card.className = 'log-card violation-log-card';
    card.innerHTML = `
            <div class="log-book">
                <h3>${escapeHtml(log.title || 'Untitled')}</h3>
                <p>by ${escapeHtml(log.author || 'Unknown')}</p>
            </div>

            <span class="violation-badge">
                <i class="fas fa-exclamation-triangle"></i> Violation
            </span>

            ${log.totalPages ? `
                <div class="log-progress">
                    <div class="log-progress-bar" style="width: ${progress}%"></div>
                </div>
                <p>${progress}% (${log.currentPage}/${log.totalPages})</p>` : ''}

            <div class="log-meta">
                <span>
                    <i class="fas fa-user"></i>
                    ${escapeHtml(log.userName || 'Unknown User')}
                </span>

                <span>
                    <i class="fas fa-calendar"></i>
                    ${formatDate(log.date)}
                </span>
            </div>

            <div class="log-notes">
                <span class="notes-label">Notes</span>
                ${log.notes ? `<p>${escapeHtml(truncate(log.notes, 50))}</p>` : ''}
            </div>

            <div class="log-actions">
                <button class="btn details-btn">
                    <i class="fas fa-eye"></i>
                    Details
                </button>

                <button class="btn edit-btn">
                    <i class="fas fa-pen"></i>
                    Edit
                </button>

                <button class="btn delete-btn">
                    <i class="fas fa-trash"></i>
                    Delete
                </button>
            </div>`;

    card.querySelector('.details-btn').addEventListener('click', () => handlers.onView?.(log.id));
    card.querySelector('.edit-btn').addEventListener('click', () => handlers.onEdit?.(log.id));
    card.querySelector('.delete-btn').addEventListener('click', () => handlers.onRestore?.(log.id));

    return card;
}


function formatDate(value) {
    if (!value) return 'N/A';

    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString();
}


function getProgress(log) {
    if (!log.totalPages) return 0;

    const progress = (Number(log.currentPage || 0) / Number(log.totalPages)) * 100;
    return Math.min(100, Math.max(0, Math.round(progress)));
}


function truncate(value, length) {
    return value.length > length ? `${value.slice(0, length)}...` : value;
}


function escapeHtml(value) {
    const div = document.createElement('div');
    div.textContent = value ?? '';
    return div.innerHTML;
}
