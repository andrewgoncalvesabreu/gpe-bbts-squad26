import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { AuthErrorPage } from './auth/AuthErrorPage'
import { keycloak } from './auth/keycloak'

async function bootstrap() {
  try {
    const authenticated = await keycloak.init({
      onLoad: 'login-required',
      pkceMethod: 'S256',
      checkLoginIframe: false,
    })

    if (!authenticated) {
      window.location.reload()
      return
    }

    createRoot(document.getElementById('root')!).render(
      <StrictMode>
        <App />
      </StrictMode>,
    )
  } catch (error) {
    console.error('Falha ao inicializar o Keycloak', error)
    createRoot(document.getElementById('root')!).render(<AuthErrorPage />)
  }
}

void bootstrap()
