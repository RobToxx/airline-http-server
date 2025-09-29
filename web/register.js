const API_URL = 'http://localhost:8000';

const registerForm = document.getElementById('register-form');
const nameInput = document.getElementById('name');
const emailInput = document.getElementById('email');
const passwordInput = document.getElementById('password');
const registerStatus = document.getElementById('register-status');

registerForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    registerStatus.textContent = 'Registrando...';

    const name = nameInput.value;
    const email = emailInput.value;
    const password = passwordInput.value;

    try {
        const response = await fetch(`${API_URL}/user/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'No se pudo completar el registro.');
        }

        registerStatus.textContent = '¡Registro exitoso! Redirigiendo a login...';
        registerStatus.style.color = 'green';
        
        setTimeout(() => {
            window.location.href = 'login.html';
        }, 2000);

    } catch (error) {
        registerStatus.textContent = error.message;
        registerStatus.style.color = 'red';
    }
});