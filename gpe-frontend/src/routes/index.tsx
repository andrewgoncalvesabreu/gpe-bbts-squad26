import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from '../components/layout/AppLayout'
import NotFoundPage from '../pages/NotFoundPage'
import ParceiroDetailPage from '../pages/parceiros/ParceiroDetailPage'
import ParceiroFormPage from '../pages/parceiros/ParceiroFormPage'
import ParceirosListPage from '../pages/parceiros/ParceirosListPage'

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<Navigate to="/parceiros" replace />} />

        {/* Módulo Parceiros */}
        <Route path="parceiros" element={<ParceirosListPage />} />
        <Route path="parceiros/novo" element={<ParceiroFormPage />} />
        <Route path="parceiros/:id" element={<ParceiroDetailPage />} />
        <Route path="parceiros/:id/editar" element={<ParceiroFormPage />} />

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
