let progressChart = null;

const $ = (id) => document.getElementById(id);

init();

function init() {
    initProgressSection();
    setupEventListeners();
    fetchAndDisplayBookProgress();
}

function initProgressSection() {
    const today = new Date();
    const oneMonthAgo = new Date();

    oneMonthAgo.setMonth(oneMonthAgo.getMonth() - 1);

    $('customStartDate').value = formatDateForInput(oneMonthAgo);
    $('customEndDate').value = formatDateForInput(today);

    fetchStatistics('total');
}

function setupEventListeners() {
    $('progressPeriod')?.addEventListener('change', handlePeriodChange);
    $('applyCustomRange')?.addEventListener('click', handleCustomRange);
}

function handlePeriodChange(event) {
    const period = event.target.value;
    const customContainer = $('customRangeContainer');

    const isCustom = period === 'custom';

    customContainer.style.display = isCustom ? 'flex' : 'none';

    if (!isCustom) {
        fetchStatistics(period);
    }
}

function handleCustomRange() {
    const startDate = $('customStartDate').value;
    const endDate = $('customEndDate').value;

    if (!startDate || !endDate) {
        return;
    }

    if (startDate > endDate) {
        showToastMessage('Start date cannot be after end date.');
        return;
    }

    fetchStatistics('custom', startDate, endDate);
}

function fetchStatistics(period, startDate = null, endDate = null) {
    const params = new URLSearchParams();

    if (period && period !== 'custom') {
        params.set('period', period);
    }

    if (startDate && endDate) {
        params.set('startDate', startDate);
        params.set('endDate', endDate);
    }

    const queryString = params.toString();

    const url = queryString
        ? `/api/reading-statistics?${queryString}`
        : '/api/reading-statistics';

    makeAuthenticatedRequest(url)
        .then(handleResponse)
        .then((data) => {
            updateStatsCards(data);
            renderProgressChart(data);
        })
        .catch((error) => {
            console.error('Error fetching reading statistics:', error);
            showChartError();
        });
}

function updateStatsCards(data) {
    $('statBookCount').textContent = data.bookCount ?? 0;

    $('statTotalTime').textContent =
        `${data.totalReadingTime ?? 0} mins`;

    $('statAvgTime').textContent =
        `${Number(data.avgDailyTime ?? 0).toFixed(1)} mins`;
}

function renderProgressChart(data) {
    const canvas = $('progressChart');

    if (!canvas) return;

    const ctx = canvas.getContext('2d');

    if (progressChart) {
        progressChart.destroy();
    }

    progressChart = new Chart(ctx, {
        type: 'bar',

        data: {
            labels: data.dates ?? [],
            datasets: [{
                label: 'Reading Time',
                data: data.readingTimes ?? [],
                backgroundColor: 'rgba(114, 47, 55, 0.55)',
                borderColor: '#722F37',
                borderWidth: 1,
                borderRadius: 6,
                borderSkipped: false
            }]
        },

        options: {
            responsive: true,
            maintainAspectRatio: false,

            plugins: {
                legend: {
                    display: false
                },

                tooltip: {
                    callbacks: {
                        label: (context) =>
                            `${context.raw} minutes`
                    }
                }
            },

            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(0, 0, 0, 0.06)'
                    },
                    border: {
                        display: false
                    },
                    title: {
                        display: true,
                        text: 'Minutes'
                    }
                },

                x: {
                    grid: {
                        display: false
                    },
                    border: {
                        display: false
                    }
                }
            }
        }
    });
}

function fetchAndDisplayBookProgress() {
    makeAuthenticatedRequest('/api/reading-statistics/book-progress')
        .then(handleResponse)
        .then(renderBookProgress)
        .catch((error) => {
            console.error('Failed to get book progress:', error);
        });
}

function renderBookProgress(data) {
    const container = $('booksProgressContainer');

    if (!container) return;

    container.replaceChildren();

    const bookProgress = data.bookProgress ?? {};
    const entries = Object.entries(bookProgress);

    if (!entries.length) {
        const empty = document.createElement('p');
        empty.className = 'progress-empty';
        empty.textContent = 'No reading progress available yet.';
        container.appendChild(empty);
        return;
    }

    entries.forEach(([bookTitle, progress]) => {
        const percentage = Math.min(
            Math.max(Number(progress) || 0, 0),
            100
        );

        const item = document.createElement('div');
        item.className = 'book-progress-item';

        item.innerHTML = `
            <div class="book-progress-header">
                <span class="book-title"></span>
                <span class="book-progress-value">
                    ${Math.round(percentage)}%
                </span>
            </div>

            <div class="progress-bar-container">
                <div
                    class="progress-bar"
                    style="width: ${percentage}%"
                ></div>
            </div>
        `;

        item.querySelector('.book-title').textContent = bookTitle;

        container.appendChild(item);
    });
}

function showChartError() {
    const container = document.querySelector('.chart-container');

    if (!container) return;

    container.innerHTML = `
        <div class="chart-error">
            <i class="fas fa-chart-column"></i>
            <p>Unable to load reading activity.</p>
            <span>Please try again later.</span>
        </div>
    `;
}

function showToastMessage(message) {
    alert(message);
}

function makeAuthenticatedRequest(url, options = {}) {
    options.credentials = 'include';

    const csrfToken =
        document.querySelector('meta[name="_csrf"]')?.content;

    const csrfHeader =
        document.querySelector('meta[name="_csrf_header"]')?.content;

    if (csrfToken && csrfHeader) {
        options.headers = {
            ...options.headers,
            [csrfHeader]: csrfToken
        };
    }

    return fetch(url, options)
        .then((response) => {
            if (response.status === 401) {
                window.location.href = '/login';
                return Promise.reject(
                    new Error('Unauthorized')
                );
            }

            return response;
        });
}

function handleResponse(response) {
    if (!response.ok) {
        return response.json().then((error) => {
            throw new Error(
                error.error || 'Request failed'
            );
        });
    }

    return response.json();
}

function formatDateForInput(date) {
    return date.toISOString().split('T')[0];
}