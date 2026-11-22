import {
    openModal
} from './modal.js';

const $ = (id) => document.getElementById(id);


export function renderEntries(entries, handlers = {}) {
    const container = $('logs'); 
    if (!container) return; 
    container.replaceChildren(); 

    if (!entries.length) { 
        renderEmptyState(container, handlers); 
        return; 
    } 

    entries.forEach((log) => { 
        container.appendChild( 
            createLogCard(log, handlers)
        ); 
    }); 
}


function createLogCard(log, handlers) {
    const progress = getProgress(log);

    const card = document.createElement('article');

    card.className = "log-card";
    card.innerHTML = `
        <div class="log-card-inner">
          <div class="log-card-front">
            <h3>${log.title}</h3>
            <em><p>by ${log.author}</p></em>

            ${log.totalPages ? `
              <div class="log-progress">
                <div class="log-progress-bar" style="width: ${progress}%"></div>
              </div>
              <p>${progress}% (${log.currentPage}/${log.totalPages})</p>
            ` : ''}

            <div class="log-meta">
              <div class="log-date">
                <i class="fas fa-calendar"></i>
                <span>${formatDate(log.date)}</span>
              </div>

              <div class="log-time">
                <i class="fas fa-clock"></i>
                <span>${log.timeSpent} min</span>
              </div>
            </div>
          </div>

          <div class="log-card-back">
            <div class="log-card-header">
              <h3>${log.title}</h3>
              <em><p>by ${log.author}</p></em>
            </div>

            <div class="log-meta-back">
                <div class="log-date">
                    <i class="fas fa-calendar"></i>
                    <span>${formatDate(log.date)}</span>
                </div>

                <div class="log-time">
                    <i class="fas fa-clock"></i>
                    <span>${log.timeSpent} min</span>
                </div>
            </div>

            <div class="log-notes">
                <h4>Notes:</h4>
                ${log.notes ? `
                <p>
                    ${log.notes.substring(0, 50)}
                    ${log.notes.length > 50 ? '...' : ''}
                </p>
                ` : ''}
            </div>

            <div class="log-actions">
              <button class="view-btn btn small secondary" data-id="${log.id}">
                <i class="fas fa-eye"></i> Details
              </button>

              <button class="edit-btn btn small secondary" data-id="${log.id}">
                <i class="fas fa-edit"></i> Edit
              </button>

              <button class="delete-btn btn small transparent" data-id="${log.id}">
                <i class="fas fa-trash"></i> Delete
              </button>
            </div>
          </div>
        </div>
    `;

    card.querySelector(".view-btn")
        .addEventListener("click", () => handlers.onView(log.id));

    card.querySelector(".edit-btn")
        .addEventListener("click", () => handlers.onEdit(log.id));

    card.querySelector(".delete-btn")
        .addEventListener("click", () => handlers.onDelete(log.id));

    return card;
}


function renderEmptyState(container, handlers = {}) {
    const empty = document.createElement('div');

    empty.className = 'empty-state';
    empty.innerHTML = `
        <p>No reading logs found.</p>
        <button class="btn" id="addFirstLog" type="button">
          <i class="fas fa-plus"></i>
          Add Log
        </button>
    `;

    const button = empty.querySelector('button');

    if (button && handlers.onAdd) {
        button.addEventListener('click', handlers.onAdd);
    }

    container.appendChild(empty);
}


export function showLogDetails(log, history = []) {
    $('detailTitle').textContent = log.title;
    $('detailAuthor').textContent = log.author;
    $('detailDate').textContent = new Date(log.date).toLocaleDateString();
    $('detailTime').textContent = `${log.timeSpent} minutes`;
    $('detailCurrentPage').textContent = log.currentPage ?? 'N/A';
    $('detailTotalPages').textContent = log.totalPages ?? 'N/A';
    $('detailNotes').textContent = log.notes || 'No notes';

    if (log.currentPage && log.totalPages) {
        const progress = Math.round(
            (log.currentPage / log.totalPages) * 100
        );

        $('detailProgress').textContent = `${progress}%`;
    } else {
        $('detailProgress').textContent = 'N/A';
    }

    renderHistoryList(history, log.id);

    openModal($('detailModal'));
}

export function getBookHistory(log, logs) {
    return groupLogsByBook(logs).get(getBookKey(log)) || [];
}


export function openEntryForm() {
    const form = $('logForm');
    if (form) form.reset(); 

    const logId = $('logId'); 
    if (logId) logId.value = '';

    $('formTitle').textContent = 'New Reading Log';

    const submitButton = form?.querySelector( 'button[type="submit"]' ); 
    if (submitButton) submitButton.textContent = 'Add Log';
    
    openModal($('formModal'));
}

export function renderHistoryList(history, currentLogId, historyList = $('historyList')) {
    if (!historyList) return; 

    historyList.replaceChildren(); 
    if (!history.length) { 
        const message = document.createElement('p'); 
        message.className = 'no-history'; 
        message.textContent = 'No reading history found.';

        historyList.appendChild(message); return; 
    } 
    
    history.forEach((log) => { 
        const item = document.createElement('div'); 
        item.className = 'history-item'; 

        if (Number(log.id) === Number(currentLogId)) item.classList.add('current'); 
        
        const date = document.createElement('span'); 
        date.className = 'history-date'; 
        date.textContent = formatDate(log.date); 
        
        const content = document.createElement('div'); 
        content.className = 'history-item-content'; 
        content.append(date); 
        
        if (Number(log.id) === Number(currentLogId)) { 
            const badge = document.createElement('span'); 
            badge.className = 'badge current'; 
            badge.textContent = 'Current'; 
            
            content.appendChild(badge); 
        } 

        const meta = document.createElement('div');
        meta.className = 'history-meta';

        const time = document.createElement('span');
        time.className = 'history-stat';
        time.innerHTML = `<i class="fas fa-clock" aria-hidden="true"></i> ${log.timeSpent ?? 0} min`;

        const pages = document.createElement('span');
        pages.className = 'history-stat';
        pages.innerHTML = `<i class="fas fa-book-open" aria-hidden="true"></i> ${log.currentPage ?? 'N/A'} / ${log.totalPages ?? 'N/A'} pages`;

        meta.append(time, pages);
        item.append(content, meta);
        historyList.appendChild(item); 
    }); 
}

function groupLogsByBook(logs) {
    const historyByBook = new Map();

    logs.forEach((log) => {
        const key = getBookKey(log);
        const history = historyByBook.get(key) || [];
        history.push(log);
        historyByBook.set(key, history);
    });

    historyByBook.forEach((history) => {
        history.sort((a, b) =>
            String(b.date || '').localeCompare(String(a.date || '')) ||
            Number(b.id) - Number(a.id)
        );
    });

    return historyByBook;
}

function getBookKey(log) {
    return [log.title, log.author]
        .map((value) => String(value || '').trim().toLowerCase())
        .join('\u0000');
}


function getProgress(log) {
    if (!log.totalPages) return 0;

    const progress = (Number(log.currentPage || 0) / Number(log.totalPages)) * 100; 
    return Math.min(100, Math.max(0, Math.round(progress))); 
} 


function formatDate(date) { 
    if (!date) return 'N/A'; 
    return new Date(date).toLocaleDateString(); 
} 


function escapeHtml(value) {
    const div = document.createElement('div'); 
    div.textContent = value ?? ''; 
    return div.innerHTML; 
}