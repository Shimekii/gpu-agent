async function handleLogin() {
    const loginField = document.getElementById('login');
    const passField = document.getElementById('password');
    const msg = document.getElementById('message');

    const login = loginField.value;
    const pass = passField.value;

    try {
        const response = await fetch('/users', {
            method: 'POST',
            body: JSON.stringify({ login: login, password: pass }),
            headers: { 'Content-Type': 'application/json' }
        });

        if (response.ok) {
            const data = await response.json();

            // Сохраняем данные о сессии в браузере
            localStorage.setItem('currentUser', data.user);

            // Определяем роль. В нашей системе админ — это всегда "admin"
            const role = (data.user === 'admin') ? 'ADMIN' : 'USER';
            localStorage.setItem('userRole', role);

            // Переходим на страницу личного кабинета
            window.location.href = 'dashboard.html';
        } else {
            msg.innerText = "Неверный логин или пароль";
            msg.style.display = 'block';
        }
    } catch (error) {
        console.error("Ошибка при входе:", error);
        msg.innerText = "Ошибка соединения с сервером";
    }
}