import {
    openModal,
    closeModal
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

    const card = document.createElement("div");

    card.className = "log-card";
    card.innerHTML = `
        <div class="log-card-inner">
          <div class="log-card-front">
            <h3>${log.title}</h3>
            <p>by ${log.author}</p>

            ${log.totalPages ? `
              <div class="log-progress">
                <div class="log-progress-bar" style="width: ${progress}%"></div>
              </div>
              <p>Progress: ${progress}% (${log.currentPage}/${log.totalPages})</p>
            ` : ''}

            <div class="log-meta">
              <span>${formatDate(log.date)}</span>
              <span>${log.timeSpent} min</span>
            </div>
          </div>

          <div class="log-card-back">
            <div>
              <h3>${log.title}</h3>
              <p>by ${log.author}</p>
              <p>Date: ${formatDate(log.date)}</p>
              <p>Time: ${log.timeSpent} min</p>

              ${log.notes ? `
                <p>
                  ${log.notes.substring(0, 50)}
                  ${log.notes.length > 50 ? '...' : ''}
                </p>
              ` : ''}
            </div>

            <div class="log-actions">
              <button class="view-btn btn" data-id="${log.id}">
                <i class="fas fa-eye"></i> Details
              </button>

              <button class="edit-btn btn" data-id="${log.id}">
                <i class="fas fa-edit"></i> Edit
              </button>

              <button class="delete-btn btn" data-id="${log.id}">
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


export function showLogDetails(log) {
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

    const historyButton = $('viewHistoryBtn'); 
    if (historyButton) { 
        historyButton.dataset.logId = String(log.id); 
    }

    openModal($('detailModal'));
}


export function openEntryForm() {
    const form = $('logForm');
    if (form) form.reset(); 

    const logId = $('logId'); 
    if (logId) logId.value = '';

    $('formTitle').textContent = 'Add New Reading Log';

    const submitButton = form?.querySelector( 'button[type="submit"]' ); 
    if (submitButton) submitButton.textContent = 'Add Log';
    
    openModal($('formModal'));
}


export function showHistoryLoading() { 
    const button = $('viewHistoryBtn'); 
    if (!button) return; 
    
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Loading...'; 
}


export function showHistory() { 
    const container = $('historyContainer'); 
    if (container) container.style.display = 'block';

    const button = $('viewHistoryBtn'); 
    if (button) button.innerHTML = '<i class="fas fa-history"></i> Hide History'; 
}


export function hideHistory() { 
    const container = $('historyContainer'); 
    if (container) container.style.display = 'none';

    const button = $('viewHistoryBtn'); 
    if (button) button.innerHTML = '<i class="fas fa-history"></i> View History';
}


export function clearHistory() { 
    const historyList = $('historyList'); 
    if (historyList) historyList.replaceChildren();
}


export function showHistoryError(message) { 
    const historyList = $('historyList'); 
    if (historyList) { 
        historyList.replaceChildren(); 
        
        const error = document.createElement('p'); 
        error.className = 'no-history'; 
        error.textContent = message; 

        historyList.appendChild(error); 
    } 
    
    hideHistory();
}


export function renderHistoryList(history, currentLogId, handlers = {}) { 
    const historyList = $('historyList'); 
    if (!historyList) return; 

    historyList.replaceChildren(); 
    if (!history.length) { 
        const message = document.createElement('p'); 
        message.className = 'no-history'; 
        message.textContent = 'No previous reading history found.'; 

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
        
        const viewButton = document.createElement('button'); 
        viewButton.type = 'button';
        viewButton.className = 'history-view-btn btn small'; 
        viewButton.innerHTML = '<i class="fas fa-eye"></i> View'; 
        viewButton.addEventListener('click', () => handlers.onView?.(log.id)); 
        
        item.append(content, viewButton); 
        historyList.appendChild(item); 
    }); 
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