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
        <div class="log-card-inner">
            <div class="log-card-front">
                <h3>${escapeHtml(log.title || 'Untitled')}</h3>
                <p>by ${escapeHtml(log.author || 'Unknown')}</p>
                <span class="violation-badge"><i class="fas fa-exclamation-triangle"></i> Violation</span>
                ${log.totalPages ? `<div class="log-progress"><div class="log-progress-bar" style="width: ${progress}%"></div></div><p>Progress: ${progress}% (${log.currentPage}/${log.totalPages})</p>` : ''}
                <div class="log-meta"><span>${escapeHtml(log.username || 'Unknown')}</span><span>${formatDate(log.date)}</span></div>
            </div>
            <div class="log-card-back">
                <div>
                    <h3>${escapeHtml(log.title || 'Untitled')}</h3>
                    <p>${escapeHtml(log.reason || 'Violation of content policy')}</p>
                    <p>User: ${escapeHtml(log.username || 'Unknown')}</p>
                    <p>Time: ${log.timeSpent} min</p>
                    ${log.notes ? `<p>${escapeHtml(truncate(log.notes, 50))}</p>` : ''}
                </div>
                <div class="log-actions">
                    <button class="view-btn btn">Details</button>
                    <button class="edit-btn btn">Edit</button>
                    <button class="restore-btn btn secondary">Restore</button>
                </div>
            </div>
        </div>`;

    card.querySelector('.view-btn').addEventListener('click', () => handlers.onView?.(log.id));
    card.querySelector('.edit-btn').addEventListener('click', () => handlers.onEdit?.(log.id));
    card.querySelector('.restore-btn').addEventListener('click', () => handlers.onRestore?.(log.id));

    return card;
}


function createAction(label, className, onClick) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = className;
    button.textContent = label;
    button.addEventListener('click', onClick);
    return button;
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
