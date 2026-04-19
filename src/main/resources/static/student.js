// Функция конвертации статуса и цвета
function formatStatus(status) {
    const map = {
        'CREATED': { text: 'На рассмотрении', color: 'orange' },
        'APPROVED': { text: 'Одобрено', color: 'green' },
        'REJECTED': { text: 'Отклонено', color: 'red' },
        'ACTIVE': { text: 'Активна', color: 'blue' },
        'COMPLETED': { text: 'Завершена', color: 'gray' }
    };
    return map[status] || { text: status, color: 'black' };
}

// Вспомогательная функция для парсинга даты из Java (ISO или LocalDateTime)
function parseJavaDate(dateStr) {
    if (!dateStr) return null;
    // Если пришло в формате "2026-04-18T14:00:00.000" или "2026-04-18 14:00"
    // Заменяем пробел на T для стандарта ISO
    const standardized = dateStr.replace(' ', 'T');
    const d = new Date(standardized);
    return isNaN(d.getTime()) ? null : d;
}

// Загрузка "Моих заявок" (для my_requests.html)
async function loadMyRequests() {
    const login = localStorage.getItem('currentUser');
    const response = await fetch(`/requests?login=${login}&action=list`);
    const requests = await response.json();

    const tableBody = document.getElementById('requestTableBody');
    tableBody.innerHTML = '';

    requests.forEach((req, index) => {
        const statusInfo = formatStatus(req.status);

        // Используем функцию парсинга
        const startDate = parseJavaDate(req.startTime);
        const endDate = parseJavaDate(req.endTime);

        let dateStr = "Ошибка даты";
        let timeRange = "--:-- - --:--";

        if (startDate && endDate) {
            dateStr = startDate.toLocaleDateString('ru-RU');
            const tStart = startDate.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
            const tEnd = endDate.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
            timeRange = `${tStart} - ${tEnd}`;
        }

        tableBody.innerHTML += `
            <tr>
                <td>${index + 1}</td>
                <td>${req.purpose || '—'}</td>
                <td>${dateStr}</td>
                <td>${timeRange}</td>
                <td><span style="color: ${statusInfo.color}; font-weight:bold;">${statusInfo.text}</span></td>
            </tr>
        `;
    });
}

// Отправка новой заявки (для create_request.html)
async function submitRequest() {
    const login = localStorage.getItem('currentUser');
    const purpose = document.getElementById('purpose').value;
    const date = document.getElementById('reqDate').value;
    const start = document.getElementById('startTime').value;
    const end = document.getElementById('endTime').value;

    const requestData = {
        userLogin: login,
        purpose: purpose,
        startTime: `${date}T${start}:00`,
        endTime: `${date}T${end}:00`,
        status: 'CREATED'
    };

    const response = await fetch('/requests', {
        method: 'POST',
        body: JSON.stringify(requestData),
        headers: { 'Content-Type': 'application/json' }
    });

    if (response.ok) {
        alert("Заявка успешно создана!");
        location.href = 'dashboard.html';
    } else {
        alert("Ошибка при создании заявки");
    }
}

// Проверка ближайшей заявки (для dashboard.html)
async function checkCurrentStatus() {
    const login = localStorage.getItem('currentUser');
    const response = await fetch(`/requests?login=${login}&action=checkStatus`);

    // Проверка: если сервер вернул пустой ответ (204) или ошибку
    if (response.status === 204 || !response.ok) {
        updateBadgeNoData();
        return;
    }

    const nearest = await response.json();
    const badge = document.getElementById('access-badge');
    const infoText = document.querySelector('.time-highlight');

    if (nearest && nearest.startTime) {
        const now = new Date();
        const start = parseJavaDate(nearest.startTime); // Используем парсер
        const end = parseJavaDate(nearest.endTime);

        if (!start || !end) {
            updateBadgeNoData();
            return;
        }

        infoText.innerText = `${start.toLocaleDateString('ru-RU')} ${start.toLocaleTimeString('ru-RU', {hour:'2-digit', minute:'2-digit'})} - ${end.toLocaleTimeString('ru-RU', {hour:'2-digit', minute:'2-digit'})}`;

        // Если заявка одобрена И время сейчас внутри интервала
        if (nearest.status === 'APPROVED' || nearest.status === 'ACTIVE') {
            if (now >= start && now <= end) {
                badge.innerText = "Доступ к системе: РАЗРЕШЕН";
                badge.className = "badge green";
            } else {
                badge.innerText = "Доступ к системе: Ожидание времени";
                badge.className = "badge orange";
            }
        } else {
            // Если статус CREATED
            badge.innerText = "Доступ к системе: Ожидает одобрения";
            badge.className = "badge orange";
        }
    } else {
        updateBadgeNoData();
    }
}

function updateBadgeNoData() {
    const badge = document.getElementById('access-badge');
    const infoText = document.querySelector('.time-highlight');
    badge.innerText = "Доступ к системе: Отсутствует";
    badge.className = "badge red";
    infoText.innerText = "Нет активных заявок";
}