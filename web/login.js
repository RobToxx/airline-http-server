const API_URL = 'http://10.60.0.187:8000';

// Seleccionamos los elementos del formulario de login
const loginForm = document.getElementById('login-form');
const emailInput = document.getElementById('email');
const passwordInput = document.getElementById('password');
const loginStatus = document.getElementById('login-status');

loginForm.addEventListener('submit', async (event) => {
    event.preventDefault(); // Evitamos que la página se recargue

    // Mostramos un mensaje de carga para que el usuario sepa que algo está pasando
    loginStatus.innerHTML = '<p>Verificando...</p>';

    const email = emailInput.value;
    const password = passwordInput.value;

    try {
        const response = await fetch(`${API_URL}/user/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: email,
                password: password
            })
        });

        if (!response.ok) {
            // Si el login no es correcto, lanzamos un error con nuestro mensaje personalizado.
            throw new Error('Usuario o contraseña incorrectos. Por favor, inténtalo de nuevo.');
        }
        
        const data = await response.json();
        const sessionId = data.id;
        
        localStorage.setItem('sessionId', sessionId);

        loginStatus.innerHTML = `<p style="color: green;">¡Login exitoso! Serás redirigido.</p>`;
        
        setTimeout(() => {
            window.location.href = 'index.html';
        }, 1000);

    } catch (error) {
        // El bloque 'catch' atrapa el error que lanzamos arriba y lo muestra en la página.
        console.error('Error en el login:', error);
        loginStatus.innerHTML = `<p style="color: red;">${error.message}</p>`;
    }
});