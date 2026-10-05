import { BrowserRouter } from 'react-router-dom'
import { ToastProvider } from './components/common/ToastProvider'
import { AppRoutes } from './routes'

export default function App() {
  return (
    <BrowserRouter>
      <ToastProvider>
        <AppRoutes />
      </ToastProvider>
    </BrowserRouter>
  )
}
