import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from '../components/layout/AppLayout'
import NotFoundPage from '../pages/NotFoundPage'
import OportunidadeDetailPage from '../pages/oportunidades/OportunidadeDetailPage'
import OportunidadeFormPage from '../pages/oportunidades/OportunidadeFormPage'
import OportunidadesListPage from '../pages/oportunidades/OportunidadesListPage'
import ParceiroDetailPage from '../pages/parceiros/ParceiroDetailPage'
import ParceiroFormPage from '../pages/parceiros/ParceiroFormPage'
import ParceirosListPage from '../pages/parceiros/ParceirosListPage'
import InstrumentosPage from '../pages/instrumentos/InstrumentosPage'
import DocumentosPage from '../pages/documentos/DocumentosPage'

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

        {/* Módulo Oportunidades */}
        <Route path="oportunidades" element={<OportunidadesListPage />} />
        <Route path="oportunidades/novo" element={<OportunidadeFormPage />} />
        <Route path="oportunidades/:id" element={<OportunidadeDetailPage />} />
        <Route path="oportunidades/:id/editar" element={<OportunidadeFormPage />} />

        {/* Módulo de instrumentos jurídicos */}
        <Route path="instrumentos" element={<InstrumentosPage />} />

        {/* Módulo de gestão documental */}
        <Route path="documentos" element={<DocumentosPage />} />

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}