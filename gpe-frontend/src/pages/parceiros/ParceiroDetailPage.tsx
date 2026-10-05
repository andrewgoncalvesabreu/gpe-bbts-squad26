import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Button } from 'primereact/button'
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog'
import { TabPanel, TabView } from 'primereact/tabview'
import { useToast } from '../../components/common/toastContext'
import { TIPO_ORGANIZACAO_OPTIONS, labelFrom } from '../../constants/parceiro'
import { parceiroService } from '../../services/parceiroService'
import type { Parceiro } from '../../types/parceiro'
import { parseApiError } from '../../utils/apiError'
import { maskCnpj } from '../../utils/format'
import { HistoricoTab } from './components/HistoricoTab'
import { ParceiroInfoTab } from './components/ParceiroInfoTab'
import { ParceiroStatusTag } from './components/ParceiroStatusTag'

export default function ParceiroDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const toast = useToast()
  const [parceiro, setParceiro] = useState<Parceiro | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!id) return
    parceiroService
      .buscarPorId(id)
      .then(setParceiro)
      .catch((err) => {
        toast.error(parseApiError(err).message)
        navigate('/parceiros', { replace: true })
      })
      .finally(() => setLoading(false))
  }, [id, navigate, toast])

  const excluir = () => {
    if (!parceiro) return
    confirmDialog({
      message: `Excluir o parceiro "${parceiro.razaoSocial}"? Esta ação não pode ser desfeita.`,
      header: 'Confirmar exclusão',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptClassName: 'p-button-danger',
      accept: async () => {
        try {
          await parceiroService.excluir(parceiro.id)
          toast.success('Parceiro excluído.')
          navigate('/parceiros')
        } catch (err) {
          const { status, message } = parseApiError(err)
          toast.error(
            status === 500
              ? 'Não foi possível excluir: o parceiro possui registros vinculados (histórico/oportunidades). Altere o status para Inativo.'
              : message,
          )
        }
      },
    })
  }

  if (loading) return <p className="text-zinc-500">Carregando parceiro...</p>
  if (!parceiro) return null

  const iniciais = parceiro.razaoSocial.split(' ').filter((w) => w.length > 2).slice(0, 2).map((w) => w[0]).join('').toUpperCase()

  return (
    <div className="flex flex-col gap-5">
      <ConfirmDialog />

      <div className="flex flex-wrap items-center justify-between gap-4 rounded-xl border border-zinc-200 bg-white p-5">
        <div className="flex items-center gap-4">
          <div className="flex h-14 w-14 items-center justify-center rounded-lg bg-zinc-100 text-lg font-bold text-zinc-700">
            {iniciais || 'P'}
          </div>
          <div>
            <h1 className="text-xl font-bold">{parceiro.razaoSocial}</h1>
            <p className="mt-0.5 text-sm text-zinc-500">
              CNPJ: {maskCnpj(parceiro.cnpj)} · {labelFrom(TIPO_ORGANIZACAO_OPTIONS, parceiro.tipoOrganizacao)}
            </p>
            <div className="mt-1.5">
              <ParceiroStatusTag status={parceiro.status} />
            </div>
          </div>
        </div>
        <div className="flex gap-2">
          <Button type="button" icon="pi pi-arrow-left" label="Voltar" outlined onClick={() => navigate('/parceiros')} />
          <Button type="button" icon="pi pi-trash" label="Excluir" severity="danger" outlined onClick={excluir} />
          <Button type="button" icon="pi pi-pencil" label="Editar Perfil" onClick={() => navigate(`/parceiros/${parceiro.id}/editar`)} />
        </div>
      </div>

      <TabView>
        <TabPanel header="Informações">
          <ParceiroInfoTab parceiro={parceiro} />
        </TabPanel>
        <TabPanel header="Histórico">
          <HistoricoTab parceiroId={parceiro.id} />
        </TabPanel>
        {/* Abas previstas no protótipo, aguardando os módulos correspondentes */}
        <TabPanel header="Parcerias" disabled />
        <TabPanel header="Due Diligence" disabled />
        <TabPanel header="Documentos" disabled />
      </TabView>
    </div>
  )
}
